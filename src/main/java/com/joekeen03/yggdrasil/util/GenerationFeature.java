package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;

import javax.annotation.Nonnull;

public interface GenerationFeature {
    @Nonnull
    public IntegerMinimumAABB getMinimumBoundingBox();

    public boolean intersectsCube(CubePos pos);

    public void generate(CubePrimer cubePrimer, CubePos pos);
}
