package com.joekeen03.yggdrasil.world.structure.generationFeatures;

import com.joekeen03.yggdrasil.util.*;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Represents a cone that is truncated by M planes on the larger end, and by N planes on the smaller end, where M>=1 and N>=1
 */
public class MNTruncatedCone implements GenerationFeature {
    private final Vec3d origin, coneEndOrigin, coneUnit, coneVector;
    private final Vec3d[] plane1Units, plane2Units;
    private final double radius1, radius2, length, coneSlope, cubeDistance;

    public MNTruncatedCone(Vec3d origin, Vec3d coneUnit, Vec3d[] plane1Units, Vec3d[] plane2Units,
                           double radius1, double radius2, double length) {
        if (radius2 < 0) {
            throw new InvalidValueException("MNTruncatedCone received negative second radius.");
        }
        if (radius1 <= radius2) {
            throw new InvalidValueException("MNTruncatedCone received second radius"+
                    "which was greater than/equal to first radius.");
        }
        if (length < 0) {
            throw new InvalidValueException("MNTruncatedCone received negative length.");
        }
        if (Helpers.hasNaN(origin)) {
            throw new InvalidValueException("MNTruncatedCone received origin with NaNs.");
        }
        // Max angle a plane's unit vector should be from the cone vector.
        this.coneSlope = length/(radius1-radius2);
        double coneAngle = Math.atan2(radius1-radius2, length);
        double minUnit1Angle = Math.PI/2-coneAngle;
        double maxUnit2Angle = Math.PI/2+coneAngle;
        // FIXME With multiple planes, it actually shouldn't matter what their angles are (< parallel to cone axis?)
        //  so long as they bound their end of the cone.
        for (Vec3d plane1Unit : plane1Units) {
            if (coneUnit.dotProduct(plane1Unit) <= Math.cos(minUnit1Angle)) {
                throw new InvalidValueException("MNTruncatedCone received plane1Unit vector which was pi/2 radians or " +
                        "more away from the coneUnit vector.");
            }
        }
        for (Vec3d plane2Unit : plane2Units) {
            if (coneUnit.dotProduct(plane2Unit) >= Math.cos(maxUnit2Angle)) {
                throw new InvalidValueException("MNTruncatedCone received plane2Unit vector which was pi/2 radians or " +
                        "less away from the coneUnit vector.");
            }
        }
        this.origin = origin; // Where the cone's axis intersects plane1
        this.coneVector = coneUnit.scale(length); // Vector from cone's start point to its end point.
        this.coneEndOrigin = origin.add(coneVector); // Where the cone's axis intersects plane2
        this.coneUnit = coneUnit;
        this.plane1Units = plane1Units;
        this.plane2Units = plane2Units;
        this.radius1 = radius1;
        this.radius2 = radius2;
        this.length = length;
        this.cubeDistance = Constants.cubeHalfDiagonal / Math.cos(coneAngle);
    }

    @Override
    public @Nonnull
    IntegerMinimumAABB getMinimumBoundingBox() {
        double theta = Math.PI/2-Math.atan2(coneUnit.y, Math.sqrt(coneUnit.z*coneUnit.z+coneUnit.x*coneUnit.x));
        double phi = Math.atan2(coneUnit.x, coneUnit.z);

        // For creating the AABB, approximate the truncated cone as the smallest co-axial cylinder which wholly
        //  contains the cone segment. So, we need the "largest" radius - the largest radius of the cone at which plane1
        //  intersects it. Another way to look at it is that it's the radius of the lowest point on the cone (furthest
        //  from apex) at which plane1 intersects the cone.
        // Max slope of the plane, relative to plane perpendicular to coneUnit

        double plane1Slope = Helpers.arraySectionMax(plane1Units, 0, plane1Units.length,
                (ToDoubleFunction<Vec3d>) plane1Unit -> Math.tan(Helpers.safeACos(coneUnit.dotProduct(plane1Unit))));
        // Can determine max length and max radius by finding the intersection of two lines - one is the line of max
        //  slope for plane1, and one is the side of the cone co-planar with that line of max slope.
        double maxRadius = -(coneSlope*radius1)/(plane1Slope-coneSlope);
        double maxLength = length + plane1Slope*maxRadius;
        Vec3d adjustedOrigin = origin.add(coneUnit.scale(-plane1Slope*maxRadius));

//        // Same code as the original cylinder.
        int ySign = (theta <= Math.PI/2) ? 1 : -1;
        double lengthYComponent = maxLength*coneUnit.y;
        double lengthHorizontalComponent = Math.sin(theta)*maxLength;
        double xSign = (phi >= 0) ? 1 : -1;
        double zSign = (Math.abs(phi) < Math.PI/2) ? 1 : -1;

        double a = maxRadius;
        double b = Math.abs(maxRadius*Math.cos(theta));
        double ellipsePhi = phi-Math.PI/2;
        double ellipseZt = Math.atan2(-b*Math.sin(ellipsePhi),a*Math.cos(ellipsePhi));
        double ellipseXt = Math.atan2(b*Math.cos(ellipsePhi),a*Math.sin(ellipsePhi));
        double ellipseExtentZ = zSign * Math.abs(a*Math.cos(ellipsePhi)*Math.cos(ellipseZt)
                - b*Math.sin(ellipsePhi)*Math.sin(ellipseZt));
        double ellipseExtentX = xSign * Math.abs(a*Math.sin(ellipsePhi)*Math.cos(ellipseXt)
                + b*Math.cos(ellipsePhi)*Math.sin(ellipseXt));

        double y1 = lengthYComponent+Math.sin(theta)*maxRadius*ySign+adjustedOrigin.y;
        double y2 = Math.sin(theta)*maxRadius*-ySign+adjustedOrigin.y;
        double x1 = Math.sin(phi)*lengthHorizontalComponent+ellipseExtentX+adjustedOrigin.x;
        double x2 = -ellipseExtentX+adjustedOrigin.x;
        double z1 = Math.cos(phi)*lengthHorizontalComponent+ellipseExtentZ+adjustedOrigin.z;
        double z2 = -ellipseExtentZ+adjustedOrigin.z;

        return new IntegerMinimumAABB(this,
                (int)Math.floor(Math.min(x1, x2)/16.0),
                (int)Math.floor(Math.min(y1, y2)/16.0),
                (int)Math.floor(Math.min(z1, z2)/16.0),
                (int)Math.ceil(Math.max(x1, x2)/16.0),
                (int)Math.ceil(Math.max(y1, y2)/16.0),
                (int)Math.ceil(Math.max(z1, z2)/16.0));
    }

    /**
     * Determines if the provided point is within all the provided planes. Provided point should be relative to the
     * planes' normals' origins (I.e. all planes should intersect at the same point).
     * @param planes
     * @param point
     * @param tolerance How far outside the plane the point can be before being actually being counted as outside the
     *                  plane. Or in other words, it shifts each plane back by this many units.
     * @return
     */
    private boolean isInAllPlanes(Vec3d[] planes, Vec3d point, double tolerance) {
        for (Vec3d planeNormal : planes) {
            if ((planeNormal.dotProduct(point) < -tolerance)) { // Outside the plane.
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if the provided point is within all the provided planes. Provided point should be relative to the
     * planes' normals' origins (I.e. all planes should intersect at the same point).
     * @param planes
     * @return
     */
    private boolean isInAllPlanes(Vec3d[] planes, double x, double y, double z, Vec3d offsetVector) {
        for (Vec3d planeNormal : planes) {
            if ((Helpers.dotProduct(planeNormal, x, y, z)-planeNormal.dotProduct(offsetVector)) < 0) { // Outside the plane.
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean intersectsCube(CubePos pos) {
        Vec3d cubeVector = new Vec3d(
                pos.getXCenter()-origin.x,
                pos.getYCenter()-origin.y,
                pos.getZCenter()-origin.z);
        Vec3d cubeVector2 = new Vec3d(
                pos.getXCenter()-coneEndOrigin.x,
                pos.getYCenter()-coneEndOrigin.y,
                pos.getZCenter()-coneEndOrigin.z);
        if (isInAllPlanes(plane1Units, cubeVector, Constants.cubeHalfDiagonal)
                && isInAllPlanes(plane2Units, cubeVector2, Constants.cubeHalfDiagonal)) {
            // Check if the point is outside any of the truncation planes on the smaller end
            double dot = coneUnit.dotProduct(cubeVector); // cube vector's length along the cylinder's main axis
            // cube vector's radial distance from cone's axis, squared
            double radial2 = cubeVector.lengthSquared()-dot*dot;
            double currRadius = radius1-dot/coneSlope;

            // Treat cube as a sphere which fully encompasses it.
            if (radial2 <= (currRadius+cubeDistance)*(currRadius+cubeDistance)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void generate(CubePrimer cubePrimer, CubePos pos) {
        final byte IGNORE = 0b00;
        final byte AIR = 0b01;
        final byte WOOD = 0b10;
        final byte BARK = AIR | WOOD;
        // Need to know what would be one block outside the cube, to know if a given block is touching air.
        // What about blocks that have already been generated, both in this cube and in adjacent ones?
        //  Specifically, what about tree generation in other cubes that impacts this one?
        //  I'll probably establish some kind of happens-after relationship, where different generation features store
        //  their predecessors - what has to happen before them. That could ensure its generation properly accounts
        //  for other gen. Maybe it'd make sense to instead have a buffer that's passed between the different
        //  generators, which accrues the expected generation, which is then passed through a final step which actually
        //  generates the blocks in the cubePrimer.
        int bufferSize = ICube.SIZE+2;
        // Buffer is 2 units bigger on all sides than the actual cube.
        Vec3d cubeRay = new Vec3d(pos.getMinBlockPos()).subtract(origin).subtract(1, 1, 1);
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
                    double dot = dx*coneUnit.x+dy*coneUnit.y+dz*coneUnit.z;
                    double currRadius = radius1-dot/coneSlope;
                    if ((dx2+dy2+dz2)-dot*dot > currRadius*currRadius) {
                        buffer[x][y][z] = AIR;
                    } else if (!(isInAllPlanes(plane1Units, dx, dy, dz, Vec3d.ZERO)
                                && isInAllPlanes(plane2Units, dx, dy, dz, coneVector))) {
                        buffer[x][y][z] = IGNORE; // Don't want it covering the cylinder ends in bark (0b00|0b10 -> 0b10)
                    }
                    else {
                        buffer[x][y][z] = WOOD;
                    }
                }
            }
        }
        // Whichever direction the branch is going in.
        Helpers.PrincipalAxis axis = Helpers.getMainAxis(coneUnit);
        IBlockState axisLog = (axis == Helpers.PrincipalAxis.Y) ? BlockHelpers.blockOakY
                : (axis == Helpers.PrincipalAxis.Z) ? BlockHelpers.blockOakZ : BlockHelpers.blockOakX;
        IBlockState[] blockArray = new IBlockState[] {null, null, axisLog, BlockHelpers.blockOakNone};
        for (int x = 0; x < ICube.SIZE; x++) {
            for (int y = 0; y < ICube.SIZE; y++) {
                for (int z = 0; z < ICube.SIZE; z++) {
                    int material = buffer[x+1][y+1][z+1]
                            | (AIR & (buffer[x][y+1][z+1] | buffer[x+2][y+1][z+1] | buffer[x+1][y][z+1]
                            | buffer[x+1][y+2][z+1] | buffer[x+1][y+1][z] | buffer[x+1][y+1][z+2]));
                    if (material >= WOOD) {
                        cubePrimer.setBlockState(x, y, z, blockArray[material]);
                    }
                }
            }
        }
        if (Constants.DEBUG) {
            generateDebugBoundingBox(cubePrimer, pos);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MNTruncatedCone that = (MNTruncatedCone) o;
        return Double.compare(that.radius1, radius1) == 0 && Double.compare(that.radius2, radius2) == 0
                && Double.compare(that.length, length) == 0 && Double.compare(that.coneSlope, coneSlope) == 0
                && Double.compare(that.cubeDistance, cubeDistance) == 0 && origin.equals(that.origin)
                && coneEndOrigin.equals(that.coneEndOrigin) && coneUnit.equals(that.coneUnit)
                && coneVector.equals(that.coneVector) && Arrays.equals(plane1Units, that.plane1Units)
                && Arrays.equals(plane2Units, that.plane2Units);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(origin, coneEndOrigin, coneUnit, coneVector, radius1, radius2, length, coneSlope, cubeDistance);
        result = 31 * result + Arrays.hashCode(plane1Units);
        result = 31 * result + Arrays.hashCode(plane2Units);
        return result;
    }
}
