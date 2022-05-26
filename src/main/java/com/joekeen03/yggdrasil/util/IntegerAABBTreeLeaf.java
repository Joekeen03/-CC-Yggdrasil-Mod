package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;

import java.util.function.Consumer;

public class IntegerAABBTreeLeaf extends IntegerAABBTreeNode {
    public IntegerAABBTreeLeaf(IntegerMinimumAABB box) {
        super(box, null, null);
    }

    @Override
    public void forEachLeaf(CubePos pos, Consumer<GenerationFeature> consumer) {
        if (this.box.isInBoundingBox(pos)) {
            // FIXME need to test the intersectsCube method.
            if (((IntegerMinimumAABB)this.box).wrappedShape.intersectsCube(pos) || true) {
                consumer.accept(((IntegerMinimumAABB)this.box).wrappedShape);
            }
        }
    }
}
