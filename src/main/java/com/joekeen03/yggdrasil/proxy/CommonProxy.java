package com.joekeen03.yggdrasil.proxy;

import com.joekeen03.yggdrasil.ModYggdrasil;
import com.joekeen03.yggdrasil.blocks.ModBlocks;
import com.joekeen03.yggdrasil.client.render.IHasModel;
import com.joekeen03.yggdrasil.items.ModItems;
import com.joekeen03.yggdrasil.world.WorldTypeYggdrasil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void registerItemRenderer(Item item, int meta, String id)
    {

    }


    public void preInit(FMLPreInitializationEvent event)
    {
        WorldTypeYggdrasil.create();
    }

    public void init(FMLInitializationEvent event)
    {
        // some example code
        ModYggdrasil.info(String.format("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName()));
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new Block[0]));
        ModYggdrasil.info("Registered blocks.");
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(ModItems.ITEMS.toArray(new Item[0]));
        ModYggdrasil.info("Registered items.");
    }

    public void registerModels(ModelRegistryEvent event)
    {
        for (Block block : ModBlocks.BLOCKS) {
            if (block instanceof IHasModel)
            {
                ((IHasModel) block).registerModels();
            }
        }
        for (Item item : ModItems.ITEMS) {
            if (item instanceof IHasModel)
            {
                ((IHasModel) item).registerModels();
            }
        }
    }
}
