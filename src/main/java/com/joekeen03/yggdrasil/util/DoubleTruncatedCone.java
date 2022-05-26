package com.joekeen03.yggdrasil.util;

import com.joekeen03.yggdrasil.world.structure.TreeMegaStructureGenerator;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import static com.joekeen03.yggdrasil.util.TaperedCylinder.sqrt2;

public class DoubleTruncatedCone implements GenerationFeature {
    private final Vec3d origin, coneEndOrigin, coneUnit, plane1Unit, plane2Unit;
    private final double radius1, radius2, length, coneSlope, cubeDistance;
    private static final boolean DEBUG = true;
    // Represents a cone truncated by two planes, which aren't necessarily parallel to
    //  one another, or perpendicular to the cone's axis
    public DoubleTruncatedCone(Vec3d origin, Vec3d coneUnit, Vec3d plane1Unit, Vec3d plane2Unit,
                               double radius1, double radius2, double length) {
        if (radius2 < 0) {
            throw new InvalidValueException("DoubleTruncatedCone received negative second radius.");
        }
        if (radius1 <= radius2) {
            throw new InvalidValueException("DoubleTruncatedCone received second radius"+
                    "which was greater than/equal to first radius.");
        }
        if (length < 0) {
            throw new InvalidValueException("DoubleTruncatedCone received negative length.");
        }
        // Max angle a plane's unit vector should be from the cone vector.
        this.coneSlope = length/(radius1-radius2);
        double coneAngle = Math.atan2(radius1-radius2, length);
        double minUnit1Angle = Math.PI/2-coneAngle;
        double maxUnit2Angle = Math.PI/2+coneAngle;
        if (coneUnit.dotProduct(plane1Unit) <= Math.cos(minUnit1Angle)) { // A dot B = A.length*B.length*cos(theta)
            throw new InvalidValueException("DoubleTruncatedCone received plane1Unit vector which was pi/2 radians or " +
                    "more away from the coneUnit vector.");
        }
        if (coneUnit.dotProduct(plane2Unit) >= Math.cos(maxUnit2Angle)) {
            throw new InvalidValueException("DoubleTruncatedCone received plane2Unit vector which was pi/2 radians or " +
                    "less away from the coneUnit vector.");
        }
        this.origin = origin; // Where the cone's axis intersects plane1
        this.coneEndOrigin = origin.add(coneUnit.scale(length)); // Where the cone's axis intersects plane2
        this.coneUnit = coneUnit;
        this.plane1Unit = plane1Unit;
        this.plane2Unit = plane2Unit;
        this.radius1 = radius1;
        this.radius2 = radius2;
        this.length = length;
        this.cubeDistance = 8 * Math.sqrt(3) / Math.cos(coneAngle);
    }

    @Override
    public IntegerMinimumAABB getMinimumBoundingBox() {
        double theta = Math.PI/2-Math.atan2(coneUnit.y, Math.sqrt(coneUnit.z*coneUnit.z+coneUnit.x*coneUnit.x));
        double phi = Math.atan2(coneUnit.x, coneUnit.z);

        // For creating the AABB, approximate the truncated cone as the smallest co-axial cylinder which wholly
        //  contains the cone segment. So, we need the "largest" radius - the largest radius of the cone at which plane1
        //  intersects it. Another way to look at it is that it's the radius of the lowest point on the cone (furthest
        //  from apex) at which plane1 intersects the cone.
        // Max slope of the plane, relative to plane perpendicular to coneUnit
        double plane1Slope = Math.tan(Math.acos(coneUnit.dotProduct(plane1Unit)));
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
        if ((plane1Unit.dotProduct(cubeVector) >= 0) && (plane2Unit.dotProduct(cubeVector2) >= 0)) {
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
                for (int z = 0; z < bufferSize; z++) {
                    double dz = z+cubeRay.z;
                    double dz2 = dz*dz;
                    for (int y = 0; y < bufferSize; y++) {
                        double dy = y+cubeRay.y;
                        double dy2 = dy*dy;
                        double dot = dx*coneUnit.x+dy*coneUnit.y+dz*coneUnit.z;
                        double currRadius = radius1-dot/coneSlope;
                        if ((dx2+dy2+dz2)-dot*dot > currRadius*currRadius) {
                            buffer[x][z][y] = AIR;
                        }
                        else if (Helpers.dotProduct(plane1Unit, dx, dy, dz) < 0
                                || (Helpers.dotProduct(plane2Unit, dx, dy, dz)-plane2Unit.dotProduct(coneUnit)*length) < 0) {
                            buffer[x][z][y] = IGNORE; // Don't want it covering the cylinder ends in bark (0b00|0b10 -> 0b10)
                        }
                        else {
                            buffer[x][z][y] = WOOD;
                        }
                    }
                }
            }
            // Whichever direction the branch is going in.
            BlockLog.EnumAxis axis = (Math.abs(coneUnit.y) > sqrt2/2) ? BlockLog.EnumAxis.Y
                    : (Math.abs(coneUnit.z) > Math.abs(coneUnit.x)) ? BlockLog.EnumAxis.Z : BlockLog.EnumAxis.X;
            IBlockState[] blockArray = new IBlockState[] {null, null,
                    Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
                            .withProperty(BlockLog.LOG_AXIS, axis),
                    Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
                            .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.NONE)};
            for (int x = 0; x < ICube.SIZE; x++) {
                for (int z = 0; z < ICube.SIZE; z++) {
                    for (int y = 0; y < ICube.SIZE; y++) {
                        int material = buffer[x+1][z+1][y+1]
                                | (AIR & (buffer[x][z+1][y+1] | buffer[x+2][z+1][y+1] | buffer[x+1][z][y+1]
                                | buffer[x+1][z+2][y+1] | buffer[x+1][z+1][y] | buffer[x+1][z+1][y+2]));
                        if (material >= WOOD) {
                            cubePrimer.setBlockState(x, y, z, blockArray[material]);
                        }
                    }
                }
            }
        if (DEBUG) {
            Vec3i debugCubeRay = pos.getMinBlockPos();
            IntegerMinimumAABB box = this.getMinimumBoundingBox();
            IntegerAABB blockBox = new IntegerAABB(Coords.cubeToMinBlock(box.minX), Coords.cubeToMinBlock(box.minY),
                    Coords.cubeToMinBlock(box.minZ), Coords.cubeToMaxBlock(box.maxX),
                    Coords.cubeToMaxBlock(box.maxY), Coords.cubeToMaxBlock(box.maxZ));
            for (int dx = 0; dx < ICube.SIZE; dx++) {
                int x = dx+debugCubeRay.getX();
                for (int dz = 0; dz < ICube.SIZE; dz++) {
                    int z = dz+debugCubeRay.getZ();
                    for (int dy = 0; dy < ICube.SIZE; dy++) {
                        int y = dy+debugCubeRay.getY();
                        int nMet = ((x == blockBox.minX || x == blockBox.maxX) ? 1 : 0)
                                + ((y == blockBox.minY || y == blockBox.maxY) ? 1 : 0)
                                + ((z == blockBox.minZ || z == blockBox.maxZ) ? 1 : 0);
                        if (nMet >= 2) { // Only fill edges
                            cubePrimer.setBlockState(dx, dy, dz, Blocks.IRON_BLOCK.getDefaultState());
                        }
                    }
                }
            }
        }
    }
}
