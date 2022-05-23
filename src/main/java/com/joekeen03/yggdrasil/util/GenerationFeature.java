package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;

public interface GenerationFeature {
    public IntegerMinimumAABB getMinimumBoundingBox();

    public boolean intersectsCube(CubePos pos);

    public void generate(CubePrimer cubePrimer, CubePos pos);
}
