package com.joekeen03.yggdrasil.world.structure.generationFeatures;

import com.joekeen03.yggdrasil.util.*;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

/**
 * Feature representing knots that form on the trunk (or other thick/old branches)
 * Can be hollow or not. Likely to be larger the further up the trunk (larger branches that broke off/died)
 * For upwards-facing knots (on large branches), maybe they would be ponds, collecting water from rainfall?
 * Necessary parameters: penetration depth (how far the center goes into the trunk from its surface), raised depth
 *  (how far the edges go out from the trunk from its surface), lengthwise and circumferential diameters for both the
 *  inner hole and the raised lip (assuming ellipsoidal knots - what about ellipses that aren't aligned with the
 *  segment's axis, or non-ellipsoidal knots?), parameters relating to how much the knot's raised edge should be bare...
 */
public class TreeKnot implements GenerationFeature {
    // Should this know about the segment(s) it's placed on, and account for that in its logic?
    //  What if this occurs at a split/other join between two branches? Should it be able to spawn there?
    // Start with a fairly "dumb" implementation - just takes a specified location, orientation, and dimensions,
    //  and generates according to that, with no knowledge of what the tree's like at that position.
    //  Cons: Pushes all the "shaping" logic - how it determines the knot's shape - into the master generation code.
    //      Also, with no knowledge of the tree, any small-scale irregularities can't be accounted for by the feature
    //      - so if you have branches w/ non-circular cross-sections, or joins between branches, it might look odd.

    // Assuming ellipsoidally shaped knots, with the lengthwise dimension being longer than the circumferential.
    // Cases not handled:
    //  Internal knots (due to dead branches being enveloped)
    //  Knots that aren't excavated - maybe the branch died off but didn't decay out of it?
    //      Or more generally, knots with some wood at the hole's bottom running parallel to the hole.
    //  Irregular decay depth
    //  Knots formed at splits
    //  General tree hollows.
    //  Irregularly shaped knots - ones where the external wall is non-ellipsoidal
    //  Non radial knots - ones where the knot's orientation is non-radial
    private final double holeLength, holeWidth, holeDepth, wallThickness, wallHeight, outsideSlope;
    // outsideSlope - Slope at which the knot's exterior slopes down to the base trunk.

    // Origin should be a point on the trunk's surface.
    // Orientation should point towards the knot's interior.
    private final Vec3d origin, orientationUnit, lengthwiseUnit;

    private static final int INVALID = 0b000; // 0
    private static final int EXPOSED = 0b001; // 1
    private static final int AIR_NOBARK = 0b010; // 2
    private static final int WOOD = 0b110; // 6
    private static final int BARK = 0b111; // 7
    private final double wallOutsideSemiMajor2;
    private final double wallOutsideSemiMinor2;
    private final double holeSemiMajor2;
    private final double holeSemiMinor2;
    private final double topRadius;
    private final double holeSemiMinor;
    private final double holeSemiMajor;

    public TreeKnot(double holeLength, double holeWidth, double holeDepth, double wallThickness, double wallHeight,
                    double outsideSlope, Vec3d origin, Vec3d orientationUnit, Vec3d lengthwiseUnit) {
        if (holeLength < holeWidth) {
            Helpers.invalidValue("TreeKnot received holeLength less than holeWidth.");
        }
        this.holeLength = holeLength;
        this.holeWidth = holeWidth;
        this.holeDepth = holeDepth;
        this.wallThickness = wallThickness;
        this.wallHeight = wallHeight;
        this.origin = origin;
        this.orientationUnit = orientationUnit;
        this.lengthwiseUnit = lengthwiseUnit;
        this.outsideSlope = outsideSlope;
        wallOutsideSemiMajor2 = (holeLength + wallThickness) * (holeLength + wallThickness) / 4;
        wallOutsideSemiMinor2 = (holeWidth + wallThickness) * (holeWidth + wallThickness) / 4;
        holeSemiMajor2 = holeLength * holeLength / 4;
        holeSemiMinor2 = holeWidth * holeWidth / 4;
        topRadius = wallThickness/2;
        holeSemiMinor = holeWidth / 2;
        holeSemiMajor = holeLength / 2;
    }

    @Nonnull
    @Override
    public IntegerMinimumAABB getMinimumBoundingBox() {
        return Helpers.cylinderBoundingBox(this, holeDepth+wallHeight,
                holeSemiMajor+wallThickness+(holeDepth+wallHeight)*outsideSlope,
                orientationUnit, origin);
    }

    @Override
    public boolean intersectsCube(CubePos pos) {
        return Helpers.cubeIntersectsCylinder(origin, orientationUnit,
                holeSemiMajor+wallThickness+(holeDepth+wallHeight)*outsideSlope, holeDepth+wallHeight, pos);
    }

    @Override
    public void generate(CubePrimer cubePrimer, CubePos pos) {
        // Buffer is 2 units bigger on all sides than the actual cube.
        int bufferSize = ICube.SIZE+2;
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
                    double dist2 = dx2+dy2+dz2;
                    double dot = Helpers.dotProduct(orientationUnit, dx, dy, dz);
                    double dot2 = dot*dot;
                    double distanceLengthwise = Helpers.dotProduct(orientationUnit, dx, dy, dz);
                    double distanceLengthwise2 = distanceLengthwise*distanceLengthwise;
                    double distanceCirc2 = (dist2-dot2)-distanceLengthwise2;
                    if ((distanceLengthwise2/holeSemiMajor2 + distanceCirc2/holeSemiMinor2) < 1) {
                        // Within the perimeter of the knot's central hole.
                        if (dot < holeDepth) { // Within the actual hole
                            buffer[x][y][z] = AIR_NOBARK;
                        }
                    } else if ((distanceLengthwise2/ wallOutsideSemiMajor2 + distanceCirc2/ wallOutsideSemiMinor2) < 1) {
                        // Within the knot's wall
                        if (-dot < wallHeight-topRadius) {
                            buffer[x][y][z] = WOOD;
                        } else if (-dot < wallHeight) {
                            // Define a local coordinate system, with w running parallel to the hole's orientation,
                            //  and u running along the vector from the current block point, to the point on the ellipse
                            //  closest to the current block point.
                            double t = findClosestDistanceNumerically(distanceLengthwise, Math.sqrt(distanceCirc2));
                            double w = dot-(wallHeight-topRadius);
                            double u = Math.abs(topRadius-t); // Distance along this
                            if (w*w + u*u < topRadius*topRadius) {
                                buffer[x][y][z] = WOOD;
                            }
                        }
                    } else { // Outside the main wall.
                        if (-dot < wallHeight-topRadius*(1-Math.sin(Math.atan(outsideSlope)))) {
                            double distVertical = topRadius*(1-Math.sin(Math.atan(outsideSlope)))-dot;
                            double currSemiMajor = holeSemiMajor+wallThickness+distVertical* outsideSlope;
                            double currSemiMinor = holeSemiMinor+wallThickness+distVertical* outsideSlope;
                            if (distanceLengthwise2/(currSemiMajor*currSemiMajor) // Within the exterior slope
                                    + distanceCirc2/(currSemiMinor*currSemiMinor) < 1) {
                                buffer[x][y][z] = WOOD;
                            }
                        } else {
                            buffer[x][y][z] = EXPOSED;
                        }
                        // TODO make the knot's exterior slope flatten out at the major axes (variable slope).
                    }
                }
            }
        }

        // Whichever direction the branch is going in.
        IBlockState axisLog = BlockHelpers.fetchOakLog(lengthwiseUnit);
        IBlockState[] blockArray = new IBlockState[8];
        blockArray[AIR_NOBARK] = Blocks.AIR.getDefaultState();
        blockArray[WOOD] = axisLog;
        blockArray[BARK] = BlockHelpers.blockOakNone;
        for (int x = 0; x < ICube.SIZE; x++) {
            for (int y = 0; y < ICube.SIZE; y++) {
                for (int z = 0; z < ICube.SIZE; z++) {
                    int material = buffer[x+1][y+1][z+1]
                            | (EXPOSED & (buffer[x][y+1][z+1] | buffer[x+2][y+1][z+1] | buffer[x+1][y][z+1]
                            | buffer[x+1][y+2][z+1] | buffer[x+1][y+1][z] | buffer[x+1][y+1][z+2]));
                    if (material >= AIR_NOBARK) {
                        cubePrimer.setBlockState(x, y, z, blockArray[material]);
                    }
                }
            }
        }
        if (Constants.DEBUG) {
            generateDebugBoundingBox(cubePrimer, pos);
        }
    }

    /**
     * Finds the approximate distance the provided point is from the ellipse.
     * Coordinates should be in the ellipse's coordinate system; x is the lengthwise, or longest axis, and y is
     * the circumferential, or shortest axis.
     * @param x Point's lengthwise coordinate
     * @param y Point's circumferential coordinate
     * @return
     */
    public double findClosestDistanceNumerically(double x, double y) {
        double theta = Math.atan2(y, x);
        double t2 = Helpers.dist2(x, y, holeSemiMajor*Math.cos(theta), holeSemiMinor*Math.sin(theta)); // FIXME correct?
        final int nIter = 5;
        double advanceStep = Math.PI/20;
        for (int i = 0; i < nIter; i++) {
            double newTheta = theta+advanceStep;
            double newT2 = Helpers.dist2(x, y, holeSemiMajor*Math.cos(theta), holeSemiMinor*Math.sin(theta));
            if (newT2 < t2) {
                theta = newTheta;
                t2 = newT2;
            } else { // Try advancing the opposite direction
                newTheta = theta-advanceStep;
                newT2 = Helpers.dist2(x, y, holeSemiMajor*Math.cos(theta), holeSemiMinor*Math.sin(theta));
                if (newT2 < t2) {
                    theta = newTheta;
                    t2 = newT2;
                    advanceStep *= -1; // Keep advancing in this direction
                } else {
                    advanceStep /= 2; // Need smaller steps.
                }
            }
        }
        return Math.sqrt(t2);
    }
}
