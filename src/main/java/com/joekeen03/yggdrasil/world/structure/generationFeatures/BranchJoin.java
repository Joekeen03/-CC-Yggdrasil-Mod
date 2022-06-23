package com.joekeen03.yggdrasil.world.structure.generationFeatures;

import com.joekeen03.yggdrasil.util.*;
import com.joekeen03.yggdrasil.world.structure.tree.TreeBranch;
import com.joekeen03.yggdrasil.world.structure.tree.TreeSegment;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

/**
 * Feature for smoothing out where a child branch splits off its parent.
 * Would it be better to integrate this into the first segment of the child branch?
 * What about where the join involves a "tight knot"?
 *      (From https://homeguides.sfgate.com/causes-knot-form-tree-trunk-67275.html)
 */
public class BranchJoin implements GenerationFeature {

    private static final double CUTOFF = 1.2;

    private final TreeSegment baseSegment;
    private final TreeBranch branch;
    private final Vec3d baseClosestPoint, branchClosestPoint, closestUnitVector;
    private final boolean generate;

    public BranchJoin(TreeSegment baseSegment, TreeBranch branch) {
        this.baseSegment = baseSegment;
        this.branch = branch;
        this.generate = baseSegment.zUnit.equalsWithTolerance(branch.firstSegment.zUnit);
        this.closestUnitVector = branch.firstSegment.zUnit.crossProduct(baseSegment.zUnit).toMCVector().normalize();
        this.baseClosestPoint = Helpers.findClosestPoint(baseSegment.origin.toMCVector(), baseSegment.zUnit.toMCVector(),
                branch.firstSegment.origin.toMCVector(), branch.firstSegment.zUnit.toMCVector());
        this.branchClosestPoint = Helpers.findClosestPoint(branch.firstSegment.origin.toMCVector(), branch.firstSegment.zUnit.toMCVector(),
                baseSegment.origin.toMCVector(), branch.firstSegment.zUnit.toMCVector());
    }

    @Nonnull
    @Override
    public IntegerMinimumAABB getMinimumBoundingBox() {
        return new IntegerMinimumAABB(this,
                getBoundingBoxForSegment(false),
                getBoundingBoxForSegment(true));
    }

    private IntegerMinimumAABB getBoundingBoxForSegment(boolean computeBranch) {
        TreeSegment base = this.baseSegment;
        TreeSegment other = this.branch.firstSegment;
        Vec3d baseClosestPoint = this.baseClosestPoint;
        Vec3d otherClosestPoint = this.branchClosestPoint;
        Vec3d perpendicularUnitVector = this.closestUnitVector;
        if (computeBranch) {
            base = this.branch.firstSegment;
            other = this.baseSegment;
            baseClosestPoint = this.branchClosestPoint;
            otherClosestPoint = this.baseClosestPoint;
            perpendicularUnitVector = this.closestUnitVector.scale(-1);
        }

        Vec3d otherZUnit = other.zUnit.toMCVector();

        // We want to identify the point furthest from the origin axis for our generation logic
        //  (baseClosestPoint-branchClosestPoint), for the current "other" segment.
        //  The distance to that point, from the "other" segment's generation-local origin (otherClosestPoint), is the
        //  length of the smallest cylinder that can completely contain the "other" segment's generation.
        // This point occurs right at the "other" segment's boundary (otherRadius/otherRadius), at the furthest from
        //  the "base" segment it could occur, as far from the generation origin as possible along the "other" segment's axis.

        double peakBaseRadius = getPeakBaseRadius(base.baseRadius); // Largest possible distance from the "base" segment's axis.
        Vec3d baseShiftVector = base.zUnit.toMCVector().crossProduct(perpendicularUnitVector).normalize();
        // Line parallel to the "base" segment's origin, shifted out "peakBaseRadius" distance towards the "other" segment.
        Vec3d shiftedBaseOrigin = baseClosestPoint.add(baseShiftVector.scale(peakBaseRadius));

        Vec3d otherShiftVector = otherZUnit.crossProduct(perpendicularUnitVector);
        if (base.zUnit.dotProduct(other.zUnit) > 0) { // OtherShiftVector should point towards the "base" segment.
            otherShiftVector = otherShiftVector.scale(-1);
        }
        Vec3d shiftedOtherOrigin = otherClosestPoint.add(otherShiftVector.scale(other.baseRadius));
        Vec3d targetPoint = Helpers.findClosestPoint(shiftedOtherOrigin, otherZUnit,
                shiftedBaseOrigin, base.zUnit.toMCVector());
        double distance = targetPoint.subtract(shiftedOtherOrigin).dotProduct(otherZUnit);
        return Helpers.cylinderBoundingBox(this, distance*2*1.3, getPeakBaseRadius(other.baseRadius),
                otherZUnit, shiftedOtherOrigin.subtract(other.zUnit.scale(distance).toMCVector()));
    }

    private double getPeakBaseRadius(double radius) {
        return radius/(CUTOFF-1);
    }

    @Override
    public boolean intersectsCube(CubePos pos) {
        return false;
    }

    @Override
    public void generate(CubePrimer cubePrimer, CubePos pos) {
        int bufferSize = ICube.SIZE+2;
        // Buffer is 2 units bigger on all sides than the actual cube.
        Vec3d cubeRay = new Vec3d(pos.getMinBlockPos()).subtract(baseClosestPoint).subtract(1, 1, 1);
        // Stores what should be at this position.
        byte[][][] buffer = new byte[bufferSize][bufferSize][bufferSize];
        // TODO Use int[][] buffers instead, with the int value for an x-z position storing the value of a flag for all
        //  y positions, and eac
        for (int x = 0; x < bufferSize; x++) {
            double dx = x+cubeRay.x;
            double dx2 = dx*dx;
            for (int y = 0; y < bufferSize; y++) {
                double dy = y+cubeRay.y;
                double dy2 = dy*dy;
                for (int z = 0; z < bufferSize; z++) {
                    double dz = z+cubeRay.z;
                    double dz2 = dz*dz;
                    double length2 = dx2+dy2+dz2;
                    // Relative radial distance from each branch, squared.
                    double baseFactor = length2-Helpers.dotProduct(this.baseSegment.zUnit.toMCVector(), dx, dy, dz)
                            /Helpers.square(baseSegment.baseRadius);
                    double branchFactor = length2-Helpers.dotProduct(this.branch.firstSegment.zUnit.toMCVector(), dx, dy, dz)
                            /Helpers.square(branch.firstSegment.baseRadius);
                    if (baseFactor >= 1 && branchFactor >= 1) {
                        // How far out the current block is from both branches (along their mutually perpendicular axis),
                        //  in terms of the base's (larger segment) radius
                        double f = Math.abs(Helpers.dotProduct(this.closestUnitVector, dx, dy, dz)/baseSegment.baseRadius);
                        if (1/baseFactor + 1/branchFactor > ((2.0-CUTOFF)*f+CUTOFF)) {
                            buffer[x][y][z] = 0;
                        }
                    }
                }
            }
        }
    }
}
