package com.joekeen03.yggdrasil.world.structure;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.ICubicStructureGenerator;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class TreeGenerator implements ICubicStructureGenerator {
    private static final int X_RANGE = 32;
    private static final int Z_RANGE = 32;
    private static final int TREE_RATE = 20; // On average, 1 out of this many structure origins will spawn a tree.
    private static final IBlockState OAK_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState OAK_BARK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
            .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.NONE);
    public void generate(World world, CubePrimer cubePrimer, CubePos cubePos) {
        this.generate(world, cubePrimer, cubePos, this::generate, X_RANGE, Z_RANGE, 4, 4);
    }

    protected void generate(World world, Random rand, CubePrimer cube,
                  int structureX, int structureY, int structureZ,
                  CubePos generatedCubePos) {
        if (rand.nextInt(TREE_RATE) != 0) {
            return;
        }

        // FIXME - for proper world gen, this might need to know where the ground is in its "origin" chunk,
        //  even if that chunk is not yet generated.

        if (structureY != 3) { // Don't generate anywhere except starting at ground level.
            return;
        }

        // TODO Randomize trunk's location w/in the origin chunk
        final int trunkXCenter = Coords.cubeToCenterBlock(structureX);
        final int trunkYCenter = Coords.cubeToMinBlock(structureY);
        final int trunkZCenter = Coords.cubeToCenterBlock(structureZ);
        int trunkHeight = rand.nextInt(512)+512; // Trunk height range: [512, 1024)
        int trunkRadius = rand.nextInt(16)+32; // Trunk's base radius range: [32, 48)
                                                    // - maybe should be influenced by height?

        // Ensure cube is w/in range of the trunk
        if (getDistSquared(generatedCubePos.getXCenter(), generatedCubePos.getZCenter(),
                trunkXCenter, trunkZCenter) > (trunkRadius + ICube.SIZE)) {
            return;
        }
        if ((generatedCubePos.getMaxBlockY() < trunkYCenter) ||
                (generatedCubePos.getMinBlockY() > (trunkYCenter+trunkHeight))) {
            return;
        }

        // Generate the trunk
        int minX = Math.max(trunkXCenter-trunkRadius, generatedCubePos.getMinBlockX());
        int minY = Math.max(trunkYCenter, generatedCubePos.getMinBlockY());
        int minZ = Math.max(trunkZCenter-trunkRadius, generatedCubePos.getMinBlockZ());

        int maxX = Math.min(trunkXCenter+trunkRadius, generatedCubePos.getMaxBlockX());
        int maxY = Math.min(trunkYCenter+trunkHeight, generatedCubePos.getMaxBlockY());
        int maxZ = Math.min(trunkZCenter+trunkRadius, generatedCubePos.getMaxBlockZ());
        final int radiusSquared = trunkRadius*trunkRadius;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if ((x*x+z*z) > radiusSquared) { // Outside of circle
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    cube.setBlockState(x, y, z, OAK_LOG);
                }
            }
        }

        // Generate the trunk's bark
        for (double theta = 0; theta < Math.PI*2; theta += 0.01) {
            int x = (int)(Math.cos(theta)*trunkRadius);
            int z = (int)(Math.sin(theta)*trunkRadius);
            for (int y = minY; y <= maxY; y++) {
                cube.setBlockState(x, y, z, OAK_BARK);
            }
        }
    }

    public static int getDistSquared(int xA, int zA, int xB, int zB) {
        int xDiff = xA-xB;
        int zDiff = zA-zB;
        return xDiff*xDiff+zDiff*zDiff;
    }
}
