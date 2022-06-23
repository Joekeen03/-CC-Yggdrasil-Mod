package com.joekeen03.yggdrasil.util;

import com.joekeen03.yggdrasil.world.structure.generationFeatures.GenerationFeature;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class IntegerAABBTree {
    public final IntegerAABBTreeNode root;

    public IntegerAABBTree(@Nonnull GenerationFeature[] shapes) {
        IntegerMinimumAABB[] boundingBoxes = new IntegerMinimumAABB[shapes.length];
        for (int i = 0; i < shapes.length; i++) {
            boundingBoxes[i] = shapes[i].getMinimumBoundingBox();
        }
        root = computeNode(boundingBoxes, 0, boundingBoxes.length);
        /* Logic:
         *  At each level, take the current array of objects (array + subsection start/stop)
         *  If that array only contains one object, return a leaf node (object's bounding box, null children);
         *  Otherwise:
         *      Generate the bounding box for the array - 6*O(n)
         *      Partition the array along the bounding box's longest dimension - O(n)
         *          Probably take the average minC (where C is the axis to partition along), and partition around that.
         *          Or use the median minC?
         *      Compute the nodes for each of those partitions (recursive call)
         */
    }

    /**
     * For the specified subsection of AABBs, recursively builds the minimum AABB tree and returns the root node of it.
     * @param boundingBoxes Array of AABBs
     * @param sectionStart Index of the first element in the subsection
     * @param sectionStop Index of the last element in the subsection
     * @return
     */
    public IntegerAABBTreeNode computeNode(IntegerMinimumAABB[] boundingBoxes, int sectionStart, int sectionStop) {
        // Base case - the tree for a single AABB is
        if ((sectionStop-sectionStart) == 1) {
            return new IntegerAABBTreeLeaf(boundingBoxes[sectionStart]);
        }
        else {
            IntegerAABB boundingBox = new IntegerAABB(
                    Helpers.arraySectionMin(boundingBoxes, sectionStart, sectionStop, IntegerAABB::getMinX),
                    Helpers.arraySectionMin(boundingBoxes, sectionStart, sectionStop, IntegerAABB::getMinY),
                    Helpers.arraySectionMin(boundingBoxes, sectionStart, sectionStop, IntegerAABB::getMinZ),
                    Helpers.arraySectionMax(boundingBoxes, sectionStart, sectionStop, IntegerAABB::getMaxX),
                    Helpers.arraySectionMax(boundingBoxes, sectionStart, sectionStop, IntegerAABB::getMaxY),
                    Helpers.arraySectionMax(boundingBoxes, sectionStart, sectionStop, IntegerAABB::getMaxZ));
            int partitionPoint;
            // TODO Use the median minX/minY/minZ for the partition point, instead of the average - that should ensure
            //  the bounding boxes are evenly distributed between the two children, especially given that the tree will
            //  have more bounding boxes towards its exterior than its interior (smaller and smaller branches)
            //  Or, use Z-ordering to sort the bounding boxes, then
            // X-axis is the longest dimension
            if ((boundingBox.lengthX > boundingBox.lengthY) && (boundingBox.lengthX > boundingBox.lengthZ)) {
                // Want to use the average of the largest and smallest minX value, as that is guaranteed to split off
                //  at least one box; if I used the center of the box (minX+lengthX/2), there are cases where it would
                //  never partition boxes, leading to infinite recursion. Example would be two boxes that are both the
                //  same lengthX, but slightly different minX (position).
                int maxMinX = Helpers.arraySectionMax(boundingBoxes, sectionStart, sectionStop, IntegerAABB::getMinX);
                partitionPoint = Helpers.arraySectionPartition(boundingBoxes, sectionStart, sectionStop,
                        (maxMinX+boundingBox.minX)/2, IntegerAABB::getMinX);
            }
            // Y-axis is the longest dimension
            else if (boundingBox.lengthY > boundingBox.lengthZ) {
                int maxMinY = Helpers.arraySectionMax(boundingBoxes, sectionStart, sectionStop, IntegerAABB::getMinY);
                partitionPoint = Helpers.arraySectionPartition(boundingBoxes, sectionStart, sectionStop,
                        (maxMinY+boundingBox.minY)/2, IntegerAABB::getMinY);
            }
            // Z-axis is the longest dimension
            else {
                int maxMinZ = Helpers.arraySectionMax(boundingBoxes, sectionStart, sectionStop, IntegerAABB::getMinZ);
                partitionPoint = Helpers.arraySectionPartition(boundingBoxes, sectionStart, sectionStop,
                        (maxMinZ+boundingBox.minZ)/2, IntegerAABB::getMinZ);
            }
            // In case the array partition method returns sectionStart - prevent infinite recursion.
            //  Kinda a hacky way - perhaps a better way to handle this?
            partitionPoint = Math.max(sectionStart+1, partitionPoint);
            // Compute children and return current tree.
            return new IntegerAABBTreeNode(boundingBox,
                    computeNode(boundingBoxes, sectionStart, partitionPoint),
                    computeNode(boundingBoxes, partitionPoint, sectionStop));
        }
    }

    /**
     * Does something for each leaf the specified point is contained w/in
     * @param pos
     * @param consumer
     */
    public void forEachLeaf(CubePos pos, Consumer<GenerationFeature> consumer) {
        root.forEachLeaf(pos, consumer);
    }
}
