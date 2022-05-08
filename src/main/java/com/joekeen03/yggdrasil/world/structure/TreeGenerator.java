package com.joekeen03.yggdrasil.world.structure;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.ICubicStructureGenerator;
import net.minecraft.world.World;

import java.util.Random;

public class TreeGenerator implements ICubicStructureGenerator {
    private static final int X_RANGE = 32;
    private static final int Z_RANGE = 32;
    private static final int TREE_RATE = 80; // On average, 1 out of this many structure origins will spawn a tree.
    public void generate(World world, CubePrimer cubePrimer, CubePos cubePos) {
        this.generate(world, cubePrimer, cubePos, this::generate, X_RANGE, Z_RANGE, 4, 4);
    }

    protected void generate(World world, Random rand, CubePrimer cube,
                  int structureX, int structureY, int structureZ,
                  CubePos generatedCubePos) {
        if (rand.nextInt(TREE_RATE) != 0) {
            return;
        }


    }


}
