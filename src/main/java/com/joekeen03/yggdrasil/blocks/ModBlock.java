package com.joekeen03.yggdrasil.blocks;

import com.joekeen03.yggdrasil.ModYggdrasil;
import com.joekeen03.yggdrasil.client.render.IHasModel;
import com.joekeen03.yggdrasil.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class ModBlock extends Block implements IHasModel {
    public ModBlock(String name, Material material)
    {
        super(material);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);

        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
    }

    @Override
    public void registerModels()
    {
        ModYggdrasil.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory");
    }
}
