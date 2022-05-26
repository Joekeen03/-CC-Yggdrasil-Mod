package com.joekeen03.yggdrasil.util;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Random;
import java.util.function.ToIntFunction;

public class Helpers {
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
}
