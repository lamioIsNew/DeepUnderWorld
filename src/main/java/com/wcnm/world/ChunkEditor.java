package com.wcnm.world;

import com.wcnm.mixin.SignBlockEntityAccessor;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Writes blocks into the chunk currently being generated and silently clips outside chunks. */
public final class ChunkEditor {
	private final ChunkAccess chunk;
	private final int minX;
	private final int minZ;
	private final int maxX;
	private final int maxZ;
	private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

	public ChunkEditor(ChunkAccess chunk) {
		this.chunk = chunk;
		this.minX = chunk.getPos().getMinBlockX();
		this.minZ = chunk.getPos().getMinBlockZ();
		this.maxX = this.minX + 15;
		this.maxZ = this.minZ + 15;
	}

	public boolean contains(int x, int z) {
		return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
	}

	public net.minecraft.world.level.LevelHeightAccessor heightAccessor() {
		return this.chunk;
	}

	public void set(int x, int y, int z, BlockState state) {
		if (!this.contains(x, z) || y < this.chunk.getMinY() || y >= this.chunk.getMaxY()) {
			return;
		}

		this.cursor.set(x, y, z);
		this.chunk.setBlockState(this.cursor, state);
	}

	public void setIfAir(int x, int y, int z, BlockState state) {
		if (!this.contains(x, z) || y < this.chunk.getMinY() || y >= this.chunk.getMaxY()) {
			return;
		}

		this.cursor.set(x, y, z);

		if (this.chunk.getBlockState(this.cursor).isAir()) {
			this.chunk.setBlockState(this.cursor, state);
		}
	}

	public BlockState get(int x, int y, int z) {
		if (!this.contains(x, z) || y < this.chunk.getMinY() || y >= this.chunk.getMaxY()) {
			return Blocks.AIR.defaultBlockState();
		}

		this.cursor.set(x, y, z);
		return this.chunk.getBlockState(this.cursor);
	}

	public void setSign(int x, int y, int z, Direction facing, boolean wall, int rotation, List<Component> lines) {
		if (!this.contains(x, z) || y < this.chunk.getMinY() || y >= this.chunk.getMaxY()) {
			return;
		}

		BlockState state = wall
			? Blocks.CRIMSON_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, facing)
			: Blocks.CRIMSON_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, rotation);

		this.cursor.set(x, y, z);
		this.chunk.setBlockState(this.cursor, state);

		SignText text = new SignText(lines, lines, DyeColor.WHITE, true);
		SignBlockEntity sign = new SignBlockEntity(this.cursor.immutable(), state);
		((SignBlockEntityAccessor) sign).wcnm$setFrontText(text);
		this.chunk.setBlockEntity(sign);
	}

	public static List<Component> signLines(int number, String name) {
		return List.of(
			Component.literal("第 " + number + " 号冥筑").withStyle(ChatFormatting.WHITE),
			Component.literal(name).withStyle(ChatFormatting.WHITE),
			Component.literal("阴司 · 酆都").withStyle(ChatFormatting.GRAY),
			Component.literal("")
		);
	}
}
