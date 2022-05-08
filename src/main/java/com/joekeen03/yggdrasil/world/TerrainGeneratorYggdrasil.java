package com.joekeen03.yggdrasil.world;

import com.joekeen03.yggdrasil.world.structure.TreeGenerator;
import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.ICubicStructureGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.event.InitCubicStructureGeneratorEvent;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TerrainGeneratorYggdrasil implements ICubeGenerator {
    protected World world;
    protected long seed;
    private Biome[] columnBiomes;
    @Nonnull private ICubicStructureGenerator treeGenerator;

    public TerrainGeneratorYggdrasil(World world, long seed) {
        this.world = world;
        this.seed = seed;
        InitCubicStructureGeneratorEvent treeEvent = new InitCubicStructureGeneratorEvent(
                InitMapGenEvent.EventType.CUSTOM, new TreeGenerator(), world);

        MinecraftForge.TERRAIN_GEN_BUS.post(treeEvent);

        this.treeGenerator = treeEvent.getNewGen();
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
        return this.generateCube(cubeX, cubeY, cubeZ, new CubePrimer());
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ, CubePrimer cubePrimer) {
        generate(cubePrimer, cubeX, cubeY, cubeZ);
        generateStructures(cubePrimer, new CubePos(cubeX, cubeY, cubeZ));
        return cubePrimer;
    }

    @Override
    public void generateColumn(Chunk column) {
        this.columnBiomes = this.world.getBiomeProvider()
                .getBiomes(this.columnBiomes,
                        Coords.cubeToMinBlock(column.x),
                        Coords.cubeToMaxBlock(column.z),
                        ICube.SIZE, ICube.SIZE);

        // Copy ids to column internal biome array
        byte[] columnBiomeArray = column.getBiomeArray();
        for (int i = 0; i < columnBiomeArray.length; ++i) {
            columnBiomeArray[i] = (byte) Biome.getIdForBiome(this.columnBiomes[i]);
        }
    }

    private void generate(final CubePrimer cubePrimer, int cubeX, int cubeY, int cubeZ) {
        if (cubeY < 4)
        {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    cubePrimer.setBlockState(x, 0, z, Blocks.STONE.getDefaultState());
                }
            }
            cubePrimer.setBlockState(0, 0, 0, Blocks.GLOWSTONE.getDefaultState());
        }
    }

    @Override
    public void populate(ICube cube) {


    }

    @Override
    public void recreateStructures(ICube cube) {

    }

    @Override
    public void recreateStructures(Chunk column) {

    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        return world.getBiome(pos).getSpawnableList(type);
    }

    @Nullable
    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        return null;
    }

    @Override
    public Box getFullPopulationRequirements(ICube cube) {
        return RECOMMENDED_FULL_POPULATOR_REQUIREMENT;
    }

    @Override
    public Box getPopulationPregenerationRequirements(ICube cube) {
        return RECOMMENDED_GENERATE_POPULATOR_REQUIREMENT;
    }

    public void generateStructures(CubePrimer cubePrimer, CubePos cubePos) {
        /*
        TODO Possible ways to make the world-gen fast:
            Segment world into "sections" - areas around the size of a structure instance, which can hold up to one
            "origin" of a structure - then you just check if the chunk is within range of that origin. (?)
            For things like the giga-trees, check y-level before even trying to generate them - check if you're above
            the column's "ground-level" (what if you have trees generate underground?)
         */
        this.treeGenerator.generate(this.world, cubePrimer, cubePos);
    }
}
