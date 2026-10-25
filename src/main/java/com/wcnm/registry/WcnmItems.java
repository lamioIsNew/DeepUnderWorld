package com.wcnm.registry;

import com.wcnm.WcnmMod;
import com.wcnm.item.GuidingLanternItem;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

public final class WcnmItems {
	public static final Item SOUL_BLOCK = blockItem("soul_block", WcnmBlocks.SOUL_BLOCK);
	public static final Item YIN_STONE = blockItem("yin_stone", WcnmBlocks.YIN_STONE);
	public static final Item YIN_STONE_BRICKS = blockItem("yin_stone_bricks", WcnmBlocks.YIN_STONE_BRICKS);
	public static final Item CHISELED_YIN_STONE = blockItem("chiseled_yin_stone", WcnmBlocks.CHISELED_YIN_STONE);
	public static final Item SOUL_LAMP = blockItem("soul_lamp", WcnmBlocks.SOUL_LAMP);
	public static final Item BIANHUA = blockItem("bianhua", WcnmBlocks.BIANHUA);

	/** 引魂灯：右键在冥界与主世界之间往返。 */
	public static final Item GUIDING_LANTERN = register(
		"guiding_lantern",
		properties -> new GuidingLanternItem(properties.stacksTo(1).rarity(Rarity.EPIC))
	);

	private WcnmItems() {
	}

	public static void init() {
	}

	private static Item blockItem(String name, Block block) {
		return register(name, properties -> new BlockItem(block, properties.useBlockDescriptionPrefix()));
	}

	private static Item register(String name, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, WcnmMod.id(name));
		Item item = factory.apply(new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
