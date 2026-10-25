package com.wcnm.block;

import com.wcnm.registry.WcnmBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** 阴草：生长在冥界松软地表上的细草。 */
public class SoulGrassBlock extends BushBlock {
	public SoulGrassBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(WcnmBlocks.SOUL_BLOCK)
			|| state.is(WcnmBlocks.ASHEN_SOIL)
			|| state.is(WcnmBlocks.SOUL_GRAVEL)
			|| state.is(WcnmBlocks.BONE_SOIL)
			|| state.is(WcnmBlocks.BLOOD_MOSS)
			|| state.is(WcnmBlocks.SOUL_STONE);
	}
}
