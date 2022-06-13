package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;

public class LeafBranch implements GenerationFeature {
    private final long seed;

    public LeafBranch(long seed) {
        this.seed = seed;
    }

    @Override
    public IntegerMinimumAABB getMinimumBoundingBox() {
        return null;
    }

    @Override
    public boolean intersectsCube(CubePos pos) {
        return true;
    }

    @Override
    public void generate(CubePrimer cubePrimer, CubePos pos) {

    }
}
