package com.joekeen03.yggdrasil.util;

import com.joekeen03.yggdrasil.world.structure.generationFeatures.GenerationFeature;

public class IntegerMinimumAABB extends IntegerAABB {
    public final GenerationFeature wrappedFeature;
    public IntegerMinimumAABB(GenerationFeature shape, int x1, int y1, int z1, int x2, int y2, int z2) {
        super (x1, y1, z1, x2, y2, z2);
        wrappedFeature = shape;
    }

    public IntegerMinimumAABB(GenerationFeature feature, IntegerMinimumAABB a, IntegerMinimumAABB b) {
        super(a, b);
        this.wrappedFeature = feature;
    }
}
