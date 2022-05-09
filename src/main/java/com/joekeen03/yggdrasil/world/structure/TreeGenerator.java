package com.joekeen03.yggdrasil.world.structure;

import com.joekeen03.yggdrasil.ModYggdrasil;
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
    private static final int XZ_RANGE = 4;
    private static final int Y_RANGE = 65;
    private static final int TREE_RATE = 5; // On average, 1 out of this many structure origins will spawn a tree.
    private static final IBlockState OAK_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState OAK_BARK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
            .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.NONE);
    public void generate(World world, CubePrimer cubePrimer, CubePos cubePos) {
        this.generate(world, cubePrimer, cubePos, this::generate, XZ_RANGE, Y_RANGE, 1, 0);
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
                trunkXCenter, trunkZCenter) > (trunkRadius + ICube.SIZE)*(trunkRadius + ICube.SIZE)) {
            return;
        }
        if ((generatedCubePos.getMaxBlockY() < trunkYCenter) ||
                (generatedCubePos.getMinBlockY() > (trunkYCenter+trunkHeight))) {
            return;
        }
        ModYggdrasil.logger.info("Attempting to generate tree centered @ "+structureX+","+structureY+","+structureZ);

        // Generate the trunk
        // Offset of the cube origin (min coords) from the trunk's origin
        int cubeXOffset = generatedCubePos.getMinBlockX()-trunkXCenter;
        int cubeYOffset = generatedCubePos.getMinBlockY()-trunkYCenter;
        int cubeZOffset = generatedCubePos.getMinBlockZ()-trunkZCenter;
        // Trunk-local coords
        int minX = Math.max(-trunkRadius, cubeXOffset);
        int minY = Math.max(0, cubeYOffset);
        int minZ = Math.max(-trunkRadius, cubeZOffset);

        int maxX = Math.min(trunkRadius, generatedCubePos.getMaxBlockX()-trunkXCenter);
        int maxY = Math.min(trunkHeight, generatedCubePos.getMaxBlockY()-trunkYCenter);
        int maxZ = Math.min(trunkRadius, generatedCubePos.getMaxBlockZ()-trunkZCenter);
        final int radiusSquared = trunkRadius*trunkRadius;
        int blockCount = 0;
        for (int x = minX; x <= maxX; x++) { // Trunk-local
            int cubeX = x-cubeXOffset; // Cube local
            for (int z = minZ; z <= maxZ; z++) { // Trunk-local
                if ((x*x+z*z) > radiusSquared) { // Outside of circle
                    continue;
                }
                int cubeZ = z-cubeZOffset; // Cube local
                for (int y = minY; y <= maxY; y++) { // Trunk-local
                    cube.setBlockState(cubeX, y-cubeYOffset, cubeZ, OAK_LOG);
                    blockCount++;
                }
            }
        }

        // Generate the trunk's bark
        for (double theta = 0; theta < Math.PI*2; theta += 0.01) {
            int x = (int)(Math.cos(theta)*trunkRadius); // Trunk-local
            int cubeX = x-cubeXOffset; // Cube local
            int z = (int)(Math.sin(theta)*trunkRadius); // Trunk-local
            int cubeZ = z-cubeZOffset; // Cube local
            if (((cubeX | 0xF) != 0xF) || (cubeZ | 0xF) != 0xF) { // If the cube-local coordinates are out of bounds [0, 15]
                continue;
            }
            for (int y = minY; y <= maxY; y++) { // Trunk local
                cube.setBlockState(cubeX, y-cubeYOffset, cubeZ, OAK_BARK);
                blockCount++;
            }
        }
        ModYggdrasil.logger.info("Tree generated @ "+structureX+","+structureY+","+structureZ+", for cube at "+generatedCubePos+". "+blockCount+" blocks placed.");
    }

    public static int getDistSquared(int xA, int zA, int xB, int zB) {
        int xDiff = xA-xB;
        int zDiff = zA-zB;
        return xDiff*xDiff+zDiff*zDiff;
    }
}
