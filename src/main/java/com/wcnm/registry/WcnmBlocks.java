package com.wcnm.registry;

import com.wcnm.WcnmMod;
import com.wcnm.block.BianhuaBlock;
import com.wcnm.block.SoulGrassBlock;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public final class WcnmBlocks {
	/** 冥石：黑曜石系的建筑主材。 */
	public static final Block YIN_STONE = register(
		"yin_stone",
		Block::new,
		baseProperties()
			.strength(40.0F, 1200.0F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.STONE)
	);

	/** 冥石砖：道路与建筑台基。 */
	public static final Block YIN_STONE_BRICKS = register(
		"yin_stone_bricks",
		Block::new,
		baseProperties()
			.strength(42.0F, 1200.0F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.STONE)
	);

	/** 雕纹冥石：屋檐、门楣与回纹装饰。 */
	public static final Block CHISELED_YIN_STONE = register(
		"chiseled_yin_stone",
		Block::new,
		baseProperties()
			.strength(45.0F, 1200.0F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.STONE)
	);

	/** 幽冥灯：方块形态的长明灯，亮度 15。 */
	public static final Block SOUL_LAMP = register(
		"soul_lamp",
		Block::new,
		baseProperties()
			.strength(8.0F, 1200.0F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.GLASS)
			.lightLevel(state -> 15)
	);

	/** 引魂灯方块：可放置的灯，亮度 15。 */
	public static final Block GUIDING_LANTERN = register(
		"guiding_lantern",
		LanternBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_CYAN)
			.strength(3.5F)
			.sound(SoundType.LANTERN)
			.lightLevel(state -> 15)
			.noOcclusion()
	);

	/** 灵魂块：冥界表层，质感接近灵魂沙，但带国风鬼面纹样。 */
	public static final Block SOUL_BLOCK = register(
		"soul_block",
		Block::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.strength(0.6F)
			.sound(SoundType.SOUL_SAND)
	);

	/** 彼岸花：只生长在冥石/黑曜石上的阴间花。 */
	public static final Block BIANHUA = register(
		"bianhua",
		BianhuaBlock::new,
		baseProperties()
			.noCollision()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.noOcclusion()
			.lightLevel(state -> 2)
	);

	/** 冥土：松软的灰黑色腐殖土，成片覆盖在灵魂块之间。 */
	public static final Block ASHEN_SOIL = register(
		"ashen_soil",
		Block::new,
		baseProperties()
			.strength(0.55F)
			.sound(SoundType.SOUL_SAND)
	);

	/** 魂砂砾：冥河冲刷出的紫灰色砂砾。 */
	public static final Block SOUL_GRAVEL = register(
		"soul_gravel",
		Block::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.strength(0.65F)
			.sound(SoundType.GRAVEL)
	);

	/** 骨壤：混有碎骨的苍白土壤。 */
	public static final Block BONE_SOIL = register(
		"bone_soil",
		Block::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_GRAY)
			.strength(0.6F)
			.sound(SoundType.SOUL_SAND)
	);

	/** 血苔：暗红色的苔藓地衣，铺在地表低洼处。 */
	public static final Block BLOOD_MOSS = register(
		"blood_moss",
		Block::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_RED)
			.strength(0.5F)
			.sound(SoundType.GRASS)
	);

	/** 魂石：带幽蓝魂脉的坚硬岩石，形成小型露头。 */
	public static final Block SOUL_STONE = register(
		"soul_stone",
		Block::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_CYAN)
			.strength(32.0F, 1200.0F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.STONE)
			.lightLevel(state -> 2)
	);

	/** 阴草：从冥土和骨壤里长出的细长鬼草。 */
	public static final Block SOUL_GRASS = register(
		"soul_grass",
		SoulGrassBlock::new,
		baseProperties()
			.noCollision()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.noOcclusion()
			.lightLevel(state -> 1)
	);

	private WcnmBlocks() {
	}

	public static void init() {
		// Static initialisation is triggered by the first reference.
	}

	private static BlockBehaviour.Properties baseProperties() {
		return BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.instrument(NoteBlockInstrument.BASEDRUM);
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, WcnmMod.id(name));
		Block block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}
}
