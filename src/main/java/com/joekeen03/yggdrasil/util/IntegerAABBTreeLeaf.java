package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;

import java.util.function.Consumer;

public class IntegerAABBTreeLeaf extends IntegerAABBTreeNode {
    public IntegerAABBTreeLeaf(IntegerMinimumAABB box) {
        super(box, null, null);
    }

    @Override
    public void forEachLeaf(CubePos pos, Consumer<GenerationFeature> consumer) {
        if (((IntegerMinimumAABB)this.box).isInBoundingBox(pos)) {
            consumer.accept(((IntegerMinimumAABB)this.box).wrappedShape);
            // TODO Do something.
        }
    }
}
