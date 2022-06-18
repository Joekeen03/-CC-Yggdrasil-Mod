package com.joekeen03.yggdrasil.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class BlockHelpers {
    public static final IBlockState blockOakX = fetchLog(BlockPlanks.EnumType.OAK, BlockLog.EnumAxis.X);
    public static final IBlockState blockOakY = fetchLog(BlockPlanks.EnumType.OAK, BlockLog.EnumAxis.Y);
    public static final IBlockState blockOakZ = fetchLog(BlockPlanks.EnumType.OAK, BlockLog.EnumAxis.Z);
    public static final IBlockState blockOakNone = fetchLog(BlockPlanks.EnumType.OAK, BlockLog.EnumAxis.NONE);

    public static IBlockState fetchLog(BlockPlanks.EnumType woodType, BlockLog.EnumAxis axis) {
        return Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, woodType)
                .withProperty(BlockLog.LOG_AXIS, axis);
    }
}
