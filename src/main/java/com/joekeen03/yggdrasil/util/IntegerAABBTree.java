package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;

import java.util.function.Consumer;

public class IntegerAABBTree {
    public final IntegerAABBTreeNode root;

    public IntegerAABBTree(GenerationFeature[] shapes) {
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
        if ((sectionStart-sectionStop) == 0) {
            return new IntegerAABBTreeNode(boundingBoxes[sectionStart], null, null);
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
                partitionPoint = Helpers.arraySectionPartition(boundingBoxes, sectionStart, sectionStop,
                        (boundingBox.lengthX/2+boundingBox.minX),
                        IntegerAABB::getMinX);
            }
            // Y-axis is the longest dimension
            else if (boundingBox.lengthY > boundingBox.lengthZ) {
                partitionPoint = Helpers.arraySectionPartition(boundingBoxes, sectionStart, sectionStop,
                        (boundingBox.lengthY/2+boundingBox.minY),
                        IntegerAABB::getMinY);
            }
            // Z-axis is the longest dimension
            else {
                partitionPoint = Helpers.arraySectionPartition(boundingBoxes, sectionStart, sectionStop,
                        (boundingBox.lengthZ/2+boundingBox.minZ),
                        IntegerAABB::getMinZ);
            }
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
