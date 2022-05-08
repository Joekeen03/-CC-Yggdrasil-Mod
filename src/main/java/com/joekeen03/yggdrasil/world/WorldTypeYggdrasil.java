package com.joekeen03.yggdrasil.world;

import io.github.opencubicchunks.cubicchunks.api.util.IntRange;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldType;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;

import javax.annotation.Nullable;

public class WorldTypeYggdrasil extends WorldType implements ICubicWorldType {
    protected WorldTypeYggdrasil() {
        super("WorldYggdrasil");
    }

    public static WorldTypeYggdrasil create() {
        return new WorldTypeYggdrasil();
    }

    @Nullable
    @Override
    public ICubeGenerator createCubeGenerator(World world) {
        return new TerrainGeneratorYggdrasil(world, world.getSeed());
    }

    @Override
    public IntRange calculateGenerationHeightRange(WorldServer world) {
        return new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean hasCubicGeneratorForWorld(World object) {
        return true;
    }
}