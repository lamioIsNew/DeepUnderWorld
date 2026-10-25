package com.wcnm.registry;

import com.wcnm.WcnmMod;
import com.wcnm.block.BianhuaBlock;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
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
