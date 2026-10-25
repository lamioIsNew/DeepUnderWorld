package com.wcnm.registry;

import com.wcnm.WcnmMod;
import com.wcnm.item.GuidingLanternItem;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;

public final class WcnmItems {
	public static final Item SOUL_BLOCK = blockItem("soul_block", WcnmBlocks.SOUL_BLOCK);
	public static final Item YIN_STONE = blockItem("yin_stone", WcnmBlocks.YIN_STONE);
	public static final Item YIN_STONE_BRICKS = blockItem("yin_stone_bricks", WcnmBlocks.YIN_STONE_BRICKS);
	public static final Item CHISELED_YIN_STONE = blockItem("chiseled_yin_stone", WcnmBlocks.CHISELED_YIN_STONE);
	public static final Item SOUL_LAMP = blockItem("soul_lamp", WcnmBlocks.SOUL_LAMP);
	public static final Item BIANHUA = blockItem("bianhua", WcnmBlocks.BIANHUA);
	public static final Item ASHEN_SOIL = blockItem("ashen_soil", WcnmBlocks.ASHEN_SOIL);
	public static final Item SOUL_GRAVEL = blockItem("soul_gravel", WcnmBlocks.SOUL_GRAVEL);
	public static final Item BONE_SOIL = blockItem("bone_soil", WcnmBlocks.BONE_SOIL);
	public static final Item BLOOD_MOSS = blockItem("blood_moss", WcnmBlocks.BLOOD_MOSS);
	public static final Item SOUL_STONE = blockItem("soul_stone", WcnmBlocks.SOUL_STONE);
	public static final Item SOUL_GRASS = blockItem("soul_grass", WcnmBlocks.SOUL_GRASS);

	public static final Item WANDERING_SOUL_SPAWN_EGG =
		spawnEgg("wandering_soul_spawn_egg", WcnmEntities.WANDERING_SOUL);
	public static final Item GHOST_WISP_SPAWN_EGG =
		spawnEgg("ghost_wisp_spawn_egg", WcnmEntities.GHOST_WISP);
	public static final Item GHOST_SOLDIER_SPAWN_EGG =
		spawnEgg("ghost_soldier_spawn_egg", WcnmEntities.GHOST_SOLDIER);
	public static final Item UNDERWORLD_HOUND_SPAWN_EGG =
		spawnEgg("underworld_hound_spawn_egg", WcnmEntities.UNDERWORLD_HOUND);

	/** 引魂灯：右键在冥界与主世界之间往返。 */
	public static final Item GUIDING_LANTERN = register(
		"guiding_lantern",
		properties -> new GuidingLanternItem(WcnmBlocks.GUIDING_LANTERN,
			properties.stacksTo(1).rarity(Rarity.EPIC))
	);

	private WcnmItems() {
	}

	public static void init() {
	}

	private static Item blockItem(String name, Block block) {
		return register(name, properties -> new BlockItem(block, properties.useBlockDescriptionPrefix()));
	}

	private static Item spawnEgg(String name, EntityType<?> type) {
		return register(name, properties -> new SpawnEggItem(properties
			.component(DataComponents.ENTITY_DATA, TypedEntityData.of(type, new CompoundTag()))));
	}

	private static Item register(String name, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, WcnmMod.id(name));
		Item item = factory.apply(new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
