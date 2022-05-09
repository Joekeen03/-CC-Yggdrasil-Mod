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

public class TreeMegaStructureGenerator implements ICubicStructureGenerator {
    private static final int TREE_RATE = 5; // On average, 1 out of this many sectors will spawn a tree.
    final int treeWidth=128;
    final int treeHeight=2048;
    final int xzSectorSize = treeWidth/2;
    final int ySectorSize = treeHeight/2;
    private static final IBlockState OAK_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState OAK_BARK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
            .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.NONE);

    @Override
    public void generate(World world, CubePrimer cube, CubePos cubePos) {

        /*
        TODO Possible ways to make the world-gen fast:
            Segment world into "sections" - areas around the size of a structure instance, which can hold up to one
            "origin" of a structure - then you just check if the chunk is within range of that origin. (?)
            For things like the giga-trees, check y-level before even trying to generate them - check if you're above
            the column's "ground-level" (what if you have trees generate underground?)
         */
        // Prototype megastructure generator
        // Blocks
        // FIXME Maybe an offset so the sectors don't perfectly line up at the origin?
        // What about variable sized features - features that can come in a variety of different sizes?
        final int cubeSectorX = Math.floorDiv(cubePos.getX(), xzSectorSize/ICube.SIZE);
        final int cubeSectorY = Math.floorDiv(cubePos.getY(), ySectorSize/ICube.SIZE);
        final int cubeSectorZ = Math.floorDiv(cubePos.getZ(), xzSectorSize/ICube.SIZE);


        Random rand = new Random(world.getSeed());
        //used to randomize contribution of each coordinate to the cube seed
        //without these swapping x/y/z coordinates would result in the same seed
        //so structures would generate symmetrically
        long randXMul = rand.nextLong();
        long randYMul = rand.nextLong();
        long randZMul = rand.nextLong();

        long randSeed = world.getSeed();
        // Want the generation to always start in sectors with coords divisible by 3
        // So for cubeSectorX = 2, it should do X coords (3, 1, 2), in that order.
        // Determining the offset so that the first coord done is divisible by 3
        final int xOffset = 4-Math.floorMod(cubeSectorX, 3);
        final int yOffset = 4-Math.floorMod(cubeSectorY, 3);
        final int zOffset = 4-Math.floorMod(cubeSectorZ, 3);
        for (int x = xOffset; x < xOffset+3; x++) {
            int sectorX = cubeSectorX+Math.floorMod(x, 3)-1;
            long randX = sectorX * randXMul ^ randSeed;
            for (int y = yOffset; y < yOffset+3; y++) {
                int sectorY = cubeSectorY+Math.floorMod(y, 3)-1;
                long randY = sectorY * randYMul ^ randX;
                for (int z = zOffset; z < zOffset+3; z++) {
                    int sectorZ = cubeSectorZ+Math.floorMod(z, 3)-1;
                    long randZ = sectorZ * randZMul ^ randY;
                    rand.setSeed(randZ);
                              this.generate(world, rand, cube, sectorX, sectorY, sectorZ, cubePos);
                }
            }
        }
    }

    protected void generate(World world, Random rand, CubePrimer cube,
                            int sectorX, int sectorY, int sectorZ,
                            CubePos generatedCubePos) {

        if (rand.nextInt(TREE_RATE) != 0) {
            return;
        }

        // FIXME - for proper world gen, this might need to know where the ground is in its "origin" chunk (for
        //  vertical position), even if that chunk is not yet generated.

         if (sectorY != 0) { // Don't generate anywhere except starting at ground level.
            return;
        }

        final int trunkXCenter = rand.nextInt(xzSectorSize)+sectorX*xzSectorSize;
        final int trunkYCenter = 48;
        final int trunkZCenter = rand.nextInt(xzSectorSize)+sectorZ*xzSectorSize;
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
        ModYggdrasil.logger.info("Attempting to generate tree centered @ "+trunkXCenter+","+trunkYCenter+","+trunkZCenter
                +", for sector "+sectorX+","+sectorY+","+sectorZ);

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
        // TODO See if setting the whole cube to wood, and then carving out the air, might be more efficient for some
        //      cases.
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
        ModYggdrasil.logger.info("Tree generated @ "+trunkXCenter+","+trunkYCenter+","+trunkZCenter+", for cube at "+generatedCubePos
                +", for sector "+sectorX+","+sectorY+","+sectorZ+". "+blockCount+" blocks placed.");
    }

    public static int getDistSquared(int xA, int zA, int xB, int zB) {
        int xDiff = xA-xB;
        int zDiff = zA-zB;
        return xDiff*xDiff+zDiff*zDiff;
    }
}
