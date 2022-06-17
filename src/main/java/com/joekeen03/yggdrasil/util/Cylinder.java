package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;

public class Cylinder implements GenerationFeature {
    public final double radius, length, theta, phi;
    public final BlockPos origin;
    public final Vec3d unit;

    /**
     *
     * @param origin
     * @param radius
     * @param length
     * @param theta Angle of the cylinder relative to the +z axis, in the horizontal plane. Should be -PI<=theta<=PI
     * @param phi Angle of the cylinder relative to the +y axis. Should be 0<=phi<=PI
     * @throws InvalidValueException
     */
    public Cylinder(BlockPos origin, double radius, double length, double theta, double phi) throws InvalidValueException {
        this.origin = origin;
        if (radius < 0) {
            throw new InvalidValueException("Cylinder received negative radius.");
        }
        this.radius = radius;

        if (length < 0) {
            throw new InvalidValueException("Cylinder received negative length.");
        }
        this.length = length;

        if (theta < 0) {
            throw new InvalidValueException("Cylinder received negative theta.");
        } else if (theta > Math.PI) {
            throw new InvalidValueException("Cylinder received theta greater than PI.");
        }
        this.theta = theta;
        if (phi < -Math.PI) {
            throw new InvalidValueException("Cylinder received phi less than PI.");
        } else if (phi > Math.PI) {
            throw new InvalidValueException("Cylinder received phi greater than PI.");
        }
        this.phi = phi;
        unit = new Vec3d(
                Math.sin(this.theta)*Math.sin(this.phi),
                Math.cos(this.theta),
                Math.sin(this.theta)*Math.cos(phi));
    }

    /**
     * Note - returns a cube-scale bounding box - e.g. the cubes which just barely hold the cylinder
     * @return
     */
    @Override
    public @Nonnull IntegerMinimumAABB getMinimumBoundingBox() {
        // For Cartesian coords, I use Minecraft's coord system - y-axis is vertical, x-axis is west-east (+x = east),
        //      and z-axis is north-south (+z = south)
        // I use ISO spherical coordinate conventions:
        //      Theta as the polar angle (north-south position on a sphere), measuring the angle relative to the +y axis
        //          E.g. theta=0 lines up with the +y axis, theta=pi/2 lines up with the x-z plane (horizontal)
        //      Phi is the azimuthal angle, aka longitude (east-west position), measuring angle relative to the +z axis
        //          E.g. phi=0 lines up with the +z axis, phi=pi/2 lines up with the +x axis
        // Bounding box has to be at least the same size as the cylinder - so at at least outside the top/bottom edges,
        // outside the +x/-x and +z/-z edges as well.
        int ySign = (theta <= Math.PI/2) ? 1 : -1;
        double lengthYComponent = Math.cos(theta)*length;
        double lengthHorizontalComponent = Math.sin(theta)*length;
        // Project the circles at the cylinder's ends onto the x-z plane; find the x and z bounds of the subsequent
        //  rotated ellipse. This uses the rotated standard ellipse equations given in
        //  https://en.wikipedia.org/wiki/Ellipse#General_ellipse_2, just before the Polar Forms section. Note that, to
        //  maintain consistency with Minecraft's asinine coordinate system, the coordinate labels are different from
        //  that section - x in the original equation becomes z here, and y in the original equation becomes x here.
        //  It takes the derivative of each:
        //      z = a*cos(phi)*cos(t)-b*sin(phi)*sin(t)     ->      dz/dt = -a*cos(phi)*sin(t)-b*sin(phi)*cos(t)
        //      x = a*sin(phi)*cos(t)+b*cos(phi)*sin(t)     ->      dx/dt = -a*sin(phi)*sin(t)+b*cos(phi)*cos(t)
        //  Then setting a given derivative to zero, solving for t, and plugging that into the original equation to find
        //  the maximum x & z values.
        //      z: -a*cos(phi)*sin(t) = b*sin(phi)*cos(t)   ->      t = atan((b*sin(phi))/(-a*cos(phi)))
        //      x: a*sin(phi)*sin(t) = b*cos(phi)*cos(t)    ->      t = atan((b*cos(phi))/(a*sin(phi)))
        //  Note that for this I add pi/2 to phi, so that the ellipse is correctly oriented. I use a,b in their standard
        //  format, with a >= b; however, the ellipse projected onto the x-z plane by the cylinder's ends is perpendicular
        //  to the cylinder's primary direction (narrow side is in line with the cylinder).
        //  So if the cylinder is pointed towards phi=0 (long axis is along +x axis), the ellipse's a-axis will actually
        //  be aligned with the z-axis, which is pi/2 (90 degrees) off from the standard form. Hence, a pi/2 offset for
        //  phi
        double xSign = (phi >= 0) ? 1 : -1;
        double zSign = (Math.abs(phi) < Math.PI/2) ? 1 : -1;
        double a = radius;
        double b = Math.abs(radius*Math.cos(theta));
        double ellipsePhi = phi-Math.PI/2;
        double ellipseZt = Math.atan2(-b*Math.sin(ellipsePhi),a*Math.cos(ellipsePhi));
        double ellipseXt = Math.atan2(b*Math.cos(ellipsePhi),a*Math.sin(ellipsePhi));
        double ellipseExtentZ = zSign * Math.abs(a*Math.cos(ellipsePhi)*Math.cos(ellipseZt)
                - b*Math.sin(ellipsePhi)*Math.sin(ellipseZt));
        double ellipseExtentX = xSign * Math.abs(a*Math.sin(ellipsePhi)*Math.cos(ellipseXt)
                + b*Math.cos(ellipsePhi)*Math.sin(ellipseXt));

        double y1 = lengthYComponent+Math.sin(theta)*radius*ySign+origin.getY();
        double y2 = Math.sin(theta)*radius*-ySign+origin.getY();
        double x1 = Math.cos(phi)*lengthHorizontalComponent+ellipseExtentX+origin.getX();
        double x2 = -ellipseExtentX+origin.getX();
        double z1 = Math.sin(phi)*lengthHorizontalComponent+ellipseExtentZ+origin.getZ();
        double z2 = -ellipseExtentZ+origin.getZ();

        return new IntegerMinimumAABB(this,
                (int)Math.floor(Math.min(x1, x2)/16.0),
                (int)Math.floor(Math.min(y1, y2)/16.0),
                (int)Math.floor(Math.min(z1, z2)/16.0),
                (int)Math.ceil(Math.max(x1, x2)/16.0),
                (int)Math.ceil(Math.max(y1, y2)/16.0),
                (int)Math.ceil(Math.max(z1, z2)/16.0));
    }

    /**
     * Determines if the specified cube probably intersects the cylinder
     * @param pos
     * @return
     */
    @Override
    public boolean intersectsCube(CubePos pos) {
        Vec3d cubeVector = new Vec3d(
                pos.getXCenter()-origin.getX(),
                pos.getYCenter()-origin.getY(),
                pos.getZCenter()-origin.getZ());
        double dot = unit.dotProduct(cubeVector); // cube vector's length along the cylinder's main axis
        double radial2 = cubeVector.lengthSquared()-dot*dot; // cube vector's radial distance, squared
        if ((dot >= 0) && (dot <= length) && (radial2 <= (radius+8)*(radius+8))) {
            return true;
        }
        return false;
    }

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
        Vec3i cubeRay = pos.getMinBlockPos().subtract(origin).add(-1, -1, -1);
        double radius2 = radius*radius;
        // Stores what should be at this position.
        byte[][][] buffer = new byte[bufferSize][bufferSize][bufferSize];
        // TODO Use int[][] buffers instead, with the int value for an x-z position storing the value of a flag for all
        //  y positions, and eac
        for (int x = 0; x < bufferSize; x++) {
            double dx = x+cubeRay.getX();
            double dx2 = dx*dx;
            for (int z = 0; z < bufferSize; z++) {
                double dz = z+cubeRay.getZ();
                double dz2 = dz*dz;
                for (int y = 0; y < bufferSize; y++) {
                    double dy = y+cubeRay.getY();
                    double dy2 = dy*dy;
                    double dot = dx*unit.x+dy*unit.y+dz*unit.z;
                    if ((dx2+dy2+dz2)-dot*dot > radius2) {
                        buffer[x][z][y] = AIR;
                    }
                    else if (dot < 0 || dot > length) {
                        buffer[x][z][y] = IGNORE; // Don't want it covering the cylinder ends in bark (0b00|0b10 -> 0b10)
                    }
                    else {
                        buffer[x][z][y] = WOOD;
                    }
                }
            }
        }
        BlockLog.EnumAxis axis = (theta < Math.PI/4 || theta > Math.PI*3/4) ? BlockLog.EnumAxis.Y
                : (Math.abs(phi) < Math.PI/4 || Math.abs(phi) > Math.PI*3/4) ? BlockLog.EnumAxis.Z : BlockLog.EnumAxis.X;
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
    }
}
