package com.joekeen03.yggdrasil.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;

public class StemVec3d {
    // FIXME should probably just copy Vec3d's code, not inherit it - avoid accidental confusion between them.
    public static final StemVec3d ZERO = new StemVec3d(0.0D, 0.0D, 0.0D);
    public final double x;
    public final double y;
    public final double z;

    public StemVec3d(double xIn, double yIn, double zIn)
    {
        if (xIn == -0.0D)
        {
            xIn = 0.0D;
        }

        if (yIn == -0.0D)
        {
            yIn = 0.0D;
        }

        if (zIn == -0.0D)
        {
            zIn = 0.0D;
        }

        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
    }

    public StemVec3d(Vec3d mcVector) {
        this.x = mcVector.z;
        this.y = mcVector.x;
        this.z = mcVector.y;
    }

    public StemVec3d subtractReverse(StemVec3d vec)
    {
        return new StemVec3d(vec.x - this.x, vec.y - this.y, vec.z - this.z);
    }

    public StemVec3d normalize()
    {
        double d0 = (double) MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return d0 < 1.0E-4D ? ZERO : new StemVec3d(this.x / d0, this.y / d0, this.z / d0);
    }

    public double dotProduct(StemVec3d vec)
    {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public StemVec3d crossProduct(StemVec3d vec)
    {
        return new StemVec3d(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
    }

    public StemVec3d subtract(StemVec3d vec)
    {
        return this.subtract(vec.x, vec.y, vec.z);
    }

    public StemVec3d subtract(double x, double y, double z)
    {
        return this.add(-x, -y, -z);
    }

    public StemVec3d add(StemVec3d vec)
    {
        return this.add(vec.x, vec.y, vec.z);
    }

    public StemVec3d add(double x, double y, double z)
    {
        return new StemVec3d(this.x + x, this.y + y, this.z + z);
    }

    public double distanceTo(StemVec3d vec)
    {
        double d0 = vec.x - this.x;
        double d1 = vec.y - this.y;
        double d2 = vec.z - this.z;
        return (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public double squareDistanceTo(StemVec3d vec)
    {
        double d0 = vec.x - this.x;
        double d1 = vec.y - this.y;
        double d2 = vec.z - this.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double squareDistanceTo(double xIn, double yIn, double zIn)
    {
        double d0 = xIn - this.x;
        double d1 = yIn - this.y;
        double d2 = zIn - this.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public StemVec3d scale(double factor)
    {
        return new StemVec3d(this.x * factor, this.y * factor, this.z * factor);
    }

    public double length()
    {
        return (double)MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double lengthSquared()
    {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    @Nullable
    public StemVec3d getIntermediateWithXValue(StemVec3d vec, double x)
    {
        double d0 = vec.x - this.x;
        double d1 = vec.y - this.y;
        double d2 = vec.z - this.z;

        if (d0 * d0 < 1.0000000116860974E-7D)
        {
            return null;
        }
        else
        {
            double d3 = (x - this.x) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new StemVec3d(this.x + d0 * d3, this.y + d1 * d3, this.z + d2 * d3) : null;
        }
    }

    @Nullable
    public StemVec3d getIntermediateWithYValue(StemVec3d vec, double y)
    {
        double d0 = vec.x - this.x;
        double d1 = vec.y - this.y;
        double d2 = vec.z - this.z;

        if (d1 * d1 < 1.0000000116860974E-7D)
        {
            return null;
        }
        else
        {
            double d3 = (y - this.y) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new StemVec3d(this.x + d0 * d3, this.y + d1 * d3, this.z + d2 * d3) : null;
        }
    }

    @Nullable
    public StemVec3d getIntermediateWithZValue(StemVec3d vec, double z)
    {
        double d0 = vec.x - this.x;
        double d1 = vec.y - this.y;
        double d2 = vec.z - this.z;

        if (d2 * d2 < 1.0000000116860974E-7D)
        {
            return null;
        }
        else
        {
            double d3 = (z - this.z) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new StemVec3d(this.x + d0 * d3, this.y + d1 * d3, this.z + d2 * d3) : null;
        }
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof StemVec3d))
        {
            return false;
        }
        else
        {
            StemVec3d StemVec3d = (StemVec3d)p_equals_1_;

            if (Double.compare(StemVec3d.x, this.x) != 0)
            {
                return false;
            }
            else if (Double.compare(StemVec3d.y, this.y) != 0)
            {
                return false;
            }
            else
            {
                return Double.compare(StemVec3d.z, this.z) == 0;
            }
        }
    }

    public int hashCode()
    {
        long j = Double.doubleToLongBits(this.x);
        int i = (int)(j ^ j >>> 32);
        j = Double.doubleToLongBits(this.y);
        i = 31 * i + (int)(j ^ j >>> 32);
        j = Double.doubleToLongBits(this.z);
        i = 31 * i + (int)(j ^ j >>> 32);
        return i;
    }

    public String toString()
    {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public StemVec3d rotatePitch(float pitch)
    {
        float f = MathHelper.cos(pitch);
        float f1 = MathHelper.sin(pitch);
        double d0 = this.x;
        double d1 = this.y * (double)f + this.z * (double)f1;
        double d2 = this.z * (double)f - this.y * (double)f1;
        return new StemVec3d(d0, d1, d2);
    }

    public StemVec3d rotateYaw(float yaw)
    {
        float f = MathHelper.cos(yaw);
        float f1 = MathHelper.sin(yaw);
        double d0 = this.x * (double)f + this.z * (double)f1;
        double d1 = this.y;
        double d2 = this.z * (double)f - this.x * (double)f1;
        return new StemVec3d(d0, d1, d2);
    }

    public static StemVec3d fromPitchYaw(Vec2f p_189984_0_)
    {
        return fromPitchYaw(p_189984_0_.x, p_189984_0_.y);
    }

    public static StemVec3d fromPitchYaw(float p_189986_0_, float p_189986_1_)
    {
        float f = MathHelper.cos(-p_189986_1_ * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-p_189986_1_ * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-p_189986_0_ * 0.017453292F);
        float f3 = MathHelper.sin(-p_189986_0_ * 0.017453292F);
        return new StemVec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    public Vec3d toMCVector() {
        return new Vec3d(this.y, this.z, this.x);
    }


    /**
     * Rotates a unit vector about this unit vector, by an angle theta. Assumes the unit vectors are perpendicular.
     * @param zUnit
     * @param theta
     * @return
     */
    public StemVec3d rotateUnitVector(StemVec3d zUnit, double theta) {
        StemVec3d cross = this.crossProduct(zUnit);
        return zUnit.scale(Math.cos(theta)).add(cross.scale(Math.sin(theta))); // Rotate next z-vector
    }

    /**
     * Rotates another vector about this vector by a specified angle theta.
     * @param vector
     * @param theta
     * @return
     */
    public StemVec3d rotateAbout(StemVec3d vector, double theta) {
        double length2 = this.lengthSquared();
        StemVec3d parallel = this.scale(dotProduct(vector)/length2);
        StemVec3d perpendicular = vector.subtract(parallel);
        double perpendicularLength = perpendicular.length();
        StemVec3d w = crossProduct(perpendicular);
        double x1 = Math.cos(theta)/perpendicularLength;
        double x2 = Math.sin(theta)/w.length();
        StemVec3d x1Perpendicular = perpendicular.scale(x1);
        StemVec3d x2w = w.scale(x2);
        StemVec3d rotatedUnit = x1Perpendicular.add(x2w);
        StemVec3d perpendicularRotated = rotatedUnit.scale(perpendicularLength);
        return perpendicularRotated.add(parallel);
    }
}
