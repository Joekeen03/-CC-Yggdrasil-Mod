package com.joekeen03.yggdrasil.world.structure.generationFeatures;

import com.joekeen03.yggdrasil.util.Constants;
import com.joekeen03.yggdrasil.util.IntegerMinimumAABB;
import com.joekeen03.yggdrasil.util.InvalidValueException;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

public class TaperedCylinder implements GenerationFeature {
    public final double radius1, radius2, length, cubeDistance, coneSlope;
    public final Vec3d origin, unit;
    public static final double sqrt2 = Math.sqrt(2);

    public TaperedCylinder(Vec3d origin, double radius1, double radius2, double length, Vec3d unit) {
        if (radius2 < 0) {
            throw new InvalidValueException("Cylinder received negative second radius.");
        }
        if (radius1 <= radius2) {
            throw new InvalidValueException(
                    "Cylinder received second radius which was greater than/equal to first radius.");
        }

        if (length < 0) {
            throw new InvalidValueException("Cylinder received negative length.");
        }

        this.radius1 = radius1;
        this.radius2 = radius2;
        this.length = length;
        this.origin = origin;
        this.unit = unit;
        // cubeDiagonal/2 (cubesize/2*sqrt(3)) / cos(coneAngle); closest point on a conic frustum is not necessarily
        //  in-line with the closest point on the cone's axis. Exact relation, and why I divide by cos(coneAngle) has
        //  to do with the similarity between the triangular cross-section of the cone, and the triangle formed by the
        //  cube's point, the actual closest point on the cone, and the point where the radial vector to the point
        //  intersects the cone. Radial vector just being the vector perpendicular to the cone's axis which intersects
        //  the cube's point.
        this.coneSlope = length/(radius1-radius2); // Slope = rise (length) over run (radius)
        double coneAngle = Math.atan2(radius1-radius2, length);
        this.cubeDistance = 8 * Math.sqrt(3) / Math.cos(coneAngle);
    }

    @Override
    public @Nonnull
    IntegerMinimumAABB getMinimumBoundingBox() {
        // Compute theta and phi so I can use the basic cylinder code.
        double theta = Math.PI/2-Math.atan2(unit.y, Math.sqrt(unit.z*unit.z+unit.x*unit.x));
        double phi = Math.atan2(unit.x, unit.z);

        // Same code as the original cylinder.
        int ySign = (theta <= Math.PI/2) ? 1 : -1;
        double lengthYComponent = length*unit.y;
        double lengthHorizontalComponent = Math.sin(theta)*length;
        double xSign = (phi >= 0) ? 1 : -1;
        double zSign = (Math.abs(phi) < Math.PI/2) ? 1 : -1;
        double a = radius1;
        double b = Math.abs(radius1*Math.cos(theta));
        double ellipsePhi = phi-Math.PI/2;
        double ellipseZt = Math.atan2(-b*Math.sin(ellipsePhi),a*Math.cos(ellipsePhi));
        double ellipseXt = Math.atan2(b*Math.cos(ellipsePhi),a*Math.sin(ellipsePhi));
        double ellipseExtentZ = zSign * Math.abs(a*Math.cos(ellipsePhi)*Math.cos(ellipseZt)
                - b*Math.sin(ellipsePhi)*Math.sin(ellipseZt));
        double ellipseExtentX = xSign * Math.abs(a*Math.sin(ellipsePhi)*Math.cos(ellipseXt)
                + b*Math.cos(ellipsePhi)*Math.sin(ellipseXt));

        // Just use the larger radius for both ends - not too inaccurate for our purposes.
        double y1 = lengthYComponent+Math.sin(theta)*radius1*ySign+origin.y;
        double y2 = Math.sin(theta)*radius1*-ySign+origin.y;
        double x1 = Math.cos(phi)*lengthHorizontalComponent+ellipseExtentX+origin.x;
        double x2 = -ellipseExtentX+origin.x;
        double z1 = Math.sin(phi)*lengthHorizontalComponent+ellipseExtentZ+origin.z;
        double z2 = -ellipseExtentZ+origin.z;

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
        double dot = unit.dotProduct(cubeVector); // cube vector's length along the cylinder's main axis
        if ((dot >= 0) && (dot <= length)) {
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
                    double dot = dx*unit.x+dy*unit.y+dz*unit.z;
                    double currRadius = radius1-dot/coneSlope;
                    if ((dx2+dy2+dz2)-dot*dot > currRadius*currRadius) {
                        buffer[x][y][z] = AIR;
                    }
                    else if (dot < 0 || dot > length) {
                        buffer[x][y][z] = IGNORE; // Don't want it covering the cylinder ends in bark (0b00|0b10 -> 0b10)
                    }
                    else {
                        buffer[x][y][z] = WOOD;
                    }
                }
            }
        }
        // Whichever direction the branch is going in.
        BlockLog.EnumAxis axis = (Math.abs(unit.y) > sqrt2/2) ? BlockLog.EnumAxis.Y
                : (Math.abs(unit.z) > Math.abs(unit.x)) ? BlockLog.EnumAxis.Z : BlockLog.EnumAxis.X;
        IBlockState[] blockArray = new IBlockState[] {null, null,
                Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
                        .withProperty(BlockLog.LOG_AXIS, axis),
                Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
                        .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.NONE)};
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
}
