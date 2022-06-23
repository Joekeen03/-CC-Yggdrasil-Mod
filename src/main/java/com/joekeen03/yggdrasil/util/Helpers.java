package com.joekeen03.yggdrasil.util;

import com.joekeen03.yggdrasil.ModYggdrasil;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class Helpers {
    public static final double epsilon = 1.0000000116860974E-7D;
    public static final double epsilon2 = epsilon*epsilon;

    public static <T> int arraySectionMin(T[] arr, int sectionStart, int sectionStop, ToIntFunction<T> extractor) {
        int min = Integer.MAX_VALUE;
        for (int i = sectionStart; i < sectionStop; i++) {
            int currVal = extractor.applyAsInt(arr[i]);
            if (currVal < min) {
                min = currVal;
            }
        }
        return min;
    }

    public static <T> int arraySectionMax(T[] arr, int sectionStart, int sectionStop, ToIntFunction<T> extractor) {
        int max = Integer.MIN_VALUE;
        for (int i = sectionStart; i < sectionStop; i++) {
            int currVal = extractor.applyAsInt(arr[i]);
            if (currVal > max) {
                max = currVal;
            }
        }
        return max;
    }

    public static <T> double arraySectionMax(T[] arr, int sectionStart, int sectionStop, ToDoubleFunction<T> extractor) {
        double max = Double.MIN_VALUE;
        for (int i = sectionStart; i < sectionStop; i++) {
            double currVal = extractor.applyAsDouble(arr[i]);
            if (currVal > max) {
                max = currVal;
            }
        }
        return max;
    }

    /**
     * Partitions an array subsection around a specified value, IN PLACE.
     * Subsection includes the elements starting at and including sectionStart, and up to but not including sectionStop.
     * I.e. the subsection includes elements [sectionStart, sectionStop)
     * @param arr
     * @param sectionStart Start of the array subsection
     * @param sectionStop End of the array subsection
     * @param partitionValue
     * @param extractor
     * @param <T>
     * @return The start index of the high half of the array.
     */
    public static <T> int arraySectionPartition(T[] arr, int sectionStart, int sectionStop,
                                                int partitionValue, ToIntFunction<T> extractor) {
        int highIndex = sectionStop;
        for (int lowIndex = sectionStart; lowIndex < highIndex; lowIndex++) {
            int leftVal = extractor.applyAsInt(arr[lowIndex]);
            if (leftVal >= partitionValue) { // Found a value that would go on the right side of the partition
                // Note: I use >= AND <= on purpose. This is to handle cases where you only have values exactly
                //  equal to the partition value; using equality to trigger swaps on both the high and low sides
                //  causes the low and high indices to converge towards the middle index of the set of identical values.
                //  If I only use equality for one of the value checks (low OR high), it runs into problems when the
                //  only values in the subsection are identical to the partition value - the partition index it returns
                //  is equal to either the sectionStart or the sectionStop, yielding partitions of size 0 and
                //  sectionStop-sectionStart; considering I use the partition index to subdivide the array for
                //  recursion in IntegerAABBTree::computeNode, that results in infinite recursion.
                highIndex--; // This value is either out of bounds (sectionStop), or an element we already swapped.
                for (; highIndex > lowIndex; highIndex--) {
                    // Find a value that would go on the left side of the partition, and swap it with the leftVal
                    // This works, because a correct partitioning has values lower than the partition value on the left,
                    // and vice versa for values larger than the partition value.
                    // We don't know where the partition point should be, but as long as we build the lower and higher
                    // halves up from their respective ends, we should end up with a correct partition.
                    int rightVal = extractor.applyAsInt(arr[highIndex]);
                    if (rightVal <= partitionValue) {
                        T swap = arr[highIndex];
                        arr[highIndex] = arr[lowIndex];
                        arr[lowIndex] = swap;
                        break;
                    }
                }
            }
        }
        // This is guaranteed to always be the start of the high section, or out of bounds if there isn't a high section.
        return highIndex;
    }

    public static int getDistSquared(int xA, int zA, int xB, int zB) {
        int xDiff = xA-xB;
        int zDiff = zA-zB;
        return xDiff*xDiff+zDiff*zDiff;
    }

    public static double safeACos(double a) {
        a = Math.max(-1.0, Math.min(1.0, a));
        return Math.acos(a);
    }

    public static double randDoubleSign(Random rand) {
        return (rand.nextBoolean()) ? 1.0 : -1.0;
    }

    public static int randIntRange(Random rand, int min, int max) {
        return rand.nextInt(max-min)+min;
    }

    public static double randDoubleRange(Random rand, double min, double max) {
        return rand.nextDouble()*(max-min)+min;
    }

    public static double dotProduct(Vec3d vec, double x, double y, double z) {
        return vec.x*x + vec.y*y + vec.z*z;
    }

    /**
     * Rotates a unit vector about another unit vector, by an angle theta. Assumes the unit vectors are perpendicular.
     * @param zUnit
     * @param xUnit
     * @param theta
     * @return
     */
    public static Vec3d rotateUnitVector(Vec3d zUnit, Vec3d xUnit, double theta) {
        Vec3d cross = xUnit.crossProduct(zUnit);
        return zUnit.scale(Math.cos(theta)).add(cross.scale(Math.sin(theta))); // Rotate next z-vector
    }

    public static boolean hasNaN(Vec3d v) {
        return (Double.isNaN(v.x) || Double.isNaN(v.y) || Double.isNaN(v.z));
    }

    public static boolean isZero(Vec3d vec) {
        return (vec == Vec3d.ZERO || vec.lengthSquared() < epsilon2);
    }

    public static void invalidValue(String message) {
        throw new InvalidValueException(message);
    }

    public static PrincipalAxis getMainAxis(Vec3d vec) {
        double xMag = Math.abs(vec.x);
        double yMag = Math.abs(vec.y);
        double zMag = Math.abs(vec.z);
        if (isZero(vec)) {
            return PrincipalAxis.NONE;
        }
        if (zMag > xMag && zMag > yMag) {
            return PrincipalAxis.Z;
        } else if (xMag > yMag) {
            return PrincipalAxis.X;
        } else {
            return PrincipalAxis.Y;
        }
    }

    public static double dist2(double x1, double y1, double x2, double y2) {
        double dx = x2-x1;
        double dy = y2-y1;
        return dx*dx+dy*dy;
    }

    public static IntegerMinimumAABB cylinderBoundingBox(GenerationFeature feature, double length, double radius,
                                                         Vec3d unit, Vec3d origin) {
        // For Cartesian coords, I use Minecraft's coord system - y-axis is vertical, x-axis is west-east (+x = east),
        //      and z-axis is north-south (+z = south)
        // I use ISO spherical coordinate conventions:
        //      Theta as the polar angle (north-south position on a sphere), measuring the angle relative to the +y axis
        //          E.g. theta=0 lines up with the +y axis, theta=pi/2 lines up with the x-z plane (horizontal)
        //      Phi is the azimuthal angle, aka longitude (east-west position), measuring angle relative to the +z axis
        //          E.g. phi=0 lines up with the +z axis, phi=pi/2 lines up with the +x axis
        // Bounding box has to be at least the same size as the cylinder - so at at least outside the top/bottom edges,
        // outside the +x/-x and +z/-z edges as well.
        double theta = Math.PI/2-Math.atan2(unit.y, Math.sqrt(unit.z*unit.z+unit.x*unit.x));
        double phi = Math.atan2(unit.x, unit.z);
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

        double y1 = lengthYComponent+Math.sin(theta)*radius*ySign+origin.y;
        double y2 = Math.sin(theta)*radius*-ySign+origin.y;
        double x1 = Math.cos(phi)*lengthHorizontalComponent+ellipseExtentX+origin.x;
        double x2 = -ellipseExtentX+origin.x;
        double z1 = Math.sin(phi)*lengthHorizontalComponent+ellipseExtentZ+origin.z;
        double z2 = -ellipseExtentZ+origin.z;

        return new IntegerMinimumAABB(feature,
                (int)Math.floor(Math.min(x1, x2)/16.0),
                (int)Math.floor(Math.min(y1, y2)/16.0),
                (int)Math.floor(Math.min(z1, z2)/16.0),
                (int)Math.ceil(Math.max(x1, x2)/16.0),
                (int)Math.ceil(Math.max(y1, y2)/16.0),
                (int)Math.ceil(Math.max(z1, z2)/16.0));
    }

    public static boolean effectivelyZero(double d) {
        return Math.abs(d) < epsilon2;
    }

    /**
     * Given two lines, each described by an origin point and a unit direction vector, finds the point on line 1 closest
     * to line 2.
     * @param vec1Origin
     * @param vec1Unit
     * @param vec2Origin
     * @param vec2Unit
     * @return
     */
    public static Vec3d findClosestPoint(Vec3d vec1Origin, Vec3d vec1Unit, Vec3d vec2Origin, Vec3d vec2Unit) {
        // Derived this based on looking at the lines from a "top-down" perspective, meaning we look at them from a direction
        //  perpendicular to both of them, and working with a triangle formed from the vector between origins, and the
        //  vectors from each origin to the closest point.
        // Can also derive this by recognizing the vector between the closest points on each vector must be perpendicular
        //  to both vectors, and setting up a system of linear equations and solving for the side length of each triangle.
        // Example of the latter implemented (taken from 'Real-Time Collision Detection' by Christer Ericson):
        //  https://gamedev.stackexchange.com/questions/9738/points-on-lines-where-the-two-lines-are-the-closest-together
        double d = vec1Unit.dotProduct(vec2Unit);
        double s = 1-d*d;
        if (effectivelyZero(s)) {
            ModYggdrasil.info("Attempted to compute closest point of two parallel vectors.");
            return vec1Origin;
        }
        Vec3d originVector = vec1Origin.subtract(vec2Origin);
        double distToClosestPoint = (originVector.dotProduct(vec2Unit)*d - originVector.dotProduct(vec1Unit)) / s;
        return vec1Origin.add(vec1Unit.scale(distToClosestPoint));
    }

    public static double square(double d) {
        return d*d;
    }

    public static boolean cubeIntersectsCylinder(Vec3d origin, Vec3d unit, double radius, double length,
                                  CubePos pos) {
        Vec3d cubeVector = new Vec3d(
                pos.getXCenter()-origin.x,
                pos.getYCenter()-origin.y,
                pos.getZCenter()-origin.z);
        double dot = unit.dotProduct(cubeVector); // cube vector's length along the cylinder's main axis
        double radial2 = cubeVector.lengthSquared()-dot*dot; // cube vector's radial distance, squared
        if ((dot >= 0) && (dot <= length) && (radial2 <= (radius+8)*(radius+8))) {
            return true;
        }
        return false;
    }

    public enum PrincipalAxis {
        X, Y, Z, NONE;
    }
}
