package com.farcr.swampexpansion.core.util;

import com.farcr.swampexpansion.core.registries.SwampExBlocks;
import com.farcr.swampexpansion.core.registries.SwampExItems;
import com.google.common.base.Supplier;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.RegistryObject;

public class RegistryUtils {
		
	public static <I extends Item> RegistryObject<I> createItem(String name, Supplier<? extends I> supplier) {
		RegistryObject<I> item = SwampExItems.ITEMS.register(name, supplier);
		return item;
	}
	
	public static BlockItem createSimpleItemBlock(Block block, ItemGroup itemGroup) {
        return (BlockItem) new BlockItem(block, new Item.Properties().group(itemGroup)).setRegistryName(block.getRegistryName());
    }
	
	public static <B extends Block> RegistryObject<B> createBlock(String name, Supplier<? extends B> supplier, ItemGroup itemGroup) {
        RegistryObject<B> block = SwampExBlocks.BLOCKS.register(name, supplier);
        SwampExItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().group(itemGroup)));
        return block;
    }
	
	public static <B extends Block> RegistryObject<B> createBlockCompat(String mod, String name, Supplier<? extends B> supplier, ItemGroup itemGroup) {
		ItemGroup determinedGroup = ModList.get().isLoaded(mod) || mod == "indev" ? itemGroup : null;
		RegistryObject<B> block = SwampExBlocks.BLOCKS.register(name, supplier);
		SwampExItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().group(determinedGroup)));
		return block;		
    }

    public static <B extends Block> RegistryObject<B> createBlockNoItem(String name, Supplier<? extends B> supplier) {
        RegistryObject<B> block = SwampExBlocks.BLOCKS.register(name, supplier);
        return block;
    }
    
    public static <I extends Item> RegistryObject<I> createCompatItem(String mod, String name, Supplier<? extends I> compat_supplier, Supplier<? extends I> supplier) {
    	Supplier<? extends I> determinedSupplier = ModList.get().isLoaded(mod) ? compat_supplier : supplier;
    	RegistryObject<I> item = SwampExItems.ITEMS.register(name, determinedSupplier);
		return item;
	}
}
