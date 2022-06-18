package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;

public interface GenerationFeature {
    @Nonnull
    public IntegerMinimumAABB getMinimumBoundingBox();

    public boolean intersectsCube(CubePos pos);

    public void generate(CubePrimer cubePrimer, CubePos pos);

    // FIXME Should this be handled this way?
    default void generateDebugBoundingBox(CubePrimer cubePrimer, CubePos pos) {
        Vec3i debugCubeRay = pos.getMinBlockPos();
        IntegerMinimumAABB box = this.getMinimumBoundingBox();
        IntegerAABB blockBox = new IntegerAABB(Coords.cubeToMinBlock(box.minX), Coords.cubeToMinBlock(box.minY),
                Coords.cubeToMinBlock(box.minZ), Coords.cubeToMaxBlock(box.maxX),
                Coords.cubeToMaxBlock(box.maxY), Coords.cubeToMaxBlock(box.maxZ));
        for (int dx = 0; dx < ICube.SIZE; dx++) {
            int x = dx + debugCubeRay.getX();
            for (int dy = 0; dy < ICube.SIZE; dy++) {
                int y = dy + debugCubeRay.getY();
                for (int dz = 0; dz < ICube.SIZE; dz++) {
                    int z = dz + debugCubeRay.getZ();
                    int nMet = ((x == blockBox.minX || x == blockBox.maxX) ? 1 : 0)
                            + ((y == blockBox.minY || y == blockBox.maxY) ? 1 : 0)
                            + ((z == blockBox.minZ || z == blockBox.maxZ) ? 1 : 0);
                    if (nMet >= 2) { // Only fill edges
                        cubePrimer.setBlockState(dx, dy, dz, Blocks.IRON_BLOCK.getDefaultState());
                    }
                }
            }
        }
    }
}
