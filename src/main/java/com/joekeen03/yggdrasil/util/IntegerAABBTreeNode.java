package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;

import java.util.function.Consumer;

public class IntegerAABBTreeNode {
    public final IntegerAABB box;
    public final IntegerAABBTreeNode leftChild;
    public final IntegerAABBTreeNode rightChild;

    public IntegerAABBTreeNode(IntegerAABB box, IntegerAABBTreeNode leftChild, IntegerAABBTreeNode rightChild) {
        this.box = box;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    /**
     * Does something for each leaf the specified point is contained w/in.
     * @param pos
     * @param consumer
     */
    public void forEachLeaf(CubePos pos, Consumer<GenerationFeature> consumer) {
        if (box.isInBoundingBox(pos)) {
            // Could be refactored such that the bounding box check is done before the function call, preventing
            // excess function calls.
            this.leftChild.forEachLeaf(pos, consumer);
            this.rightChild.forEachLeaf(pos, consumer);
        }
    }
}
