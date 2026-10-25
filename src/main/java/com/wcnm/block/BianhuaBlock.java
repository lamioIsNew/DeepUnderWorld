package com.wcnm.block;

import com.wcnm.registry.WcnmBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** 彼岸花：不需要泥土，直接扎根在黑曜石/冥石上。 */
public class BianhuaBlock extends BushBlock {
	public BianhuaBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(Blocks.OBSIDIAN)
			|| state.is(Blocks.CRYING_OBSIDIAN)
			|| state.is(WcnmBlocks.YIN_STONE)
			|| state.is(WcnmBlocks.YIN_STONE_BRICKS)
			|| state.is(WcnmBlocks.CHISELED_YIN_STONE)
			|| state.is(WcnmBlocks.SOUL_BLOCK)
			|| state.is(WcnmBlocks.ASHEN_SOIL)
			|| state.is(WcnmBlocks.SOUL_GRAVEL)
			|| state.is(WcnmBlocks.BONE_SOIL)
			|| state.is(WcnmBlocks.BLOOD_MOSS);
	}
}
