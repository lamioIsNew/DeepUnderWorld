package com.wcnm.world;

import com.wcnm.registry.WcnmBlocks;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

public final class UnderworldTeleporter {
	public static final double ENTRY_X = 0.5;
	public static final double ENTRY_Y = 81.0;
	public static final double ENTRY_Z = 0.5;

	private UnderworldTeleporter() {
	}

	public static boolean teleportIn(ServerPlayer player) {
		ServerLevel target = player.level().getServer().getLevel(WcnmWorldgen.UNDERWORLD);
		if (target == null) {
			return false;
		}

		prepareEntryPad(target);
		player.teleportTo(target, ENTRY_X, ENTRY_Y, ENTRY_Z, Set.of(), 180.0F, 0.0F, true);
		target.playSound(null, BlockPos.containing(ENTRY_X, ENTRY_Y, ENTRY_Z),
			SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 0.65F);
		return true;
	}

	public static boolean teleportOut(ServerPlayer player) {
		ServerLevel overworld = player.level().getServer().overworld();
		BlockPos spawn = overworld.getRespawnData().pos();
		BlockPos safe = findSafeSpawn(overworld, spawn);
		player.teleportTo(overworld, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, Set.of(),
			player.getYRot(), player.getXRot(), true);
		overworld.playSound(null, safe, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.1F);
		return true;
	}

	static BlockPos findSafeSpawn(ServerLevel overworld, BlockPos spawn) {
		BlockPos base = spawn;

		// getHeight() returns minY for a chunk that has never been generated, so force the
		// chunk first.  This was the cause of players being teleported into the void.
		overworld.getChunk(base.getX() >> 4, base.getZ() >> 4);
		int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());

		if (y <= overworld.getMinY() + 1) {
			// A broken/empty spawn position: fall back to the world origin.
			base = new BlockPos(0, overworld.getMinY() + 2, 0);
			overworld.getChunk(0, 0);
			y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
		}

		if (y <= overworld.getMinY() + 1) {
			// Last resort for a void world: build a small landing platform instead of dropping
			// the player into nothing.
			BlockPos platform = new BlockPos(0, Math.max(80, overworld.getMinY() + 16), 0);
			overworld.getChunk(0, 0);

			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					overworld.setBlockAndUpdate(platform.offset(dx, -1, dz),
						WcnmBlocks.SOUL_BLOCK.defaultBlockState());
				}
			}

			return platform;
		}

		for (int offset = 0; offset < 4; offset++) {
			BlockPos candidate = new BlockPos(base.getX(), y + offset, base.getZ());

			if (overworld.isEmptyBlock(candidate) && overworld.isEmptyBlock(candidate.above())) {
				return candidate;
			}
		}

		return new BlockPos(base.getX(), Math.max(y, overworld.getMinY() + 2), base.getZ());
	}

	public static boolean isInUnderworld(ServerPlayer player) {
		return player.level().dimension().equals(WcnmWorldgen.UNDERWORLD);
	}

	/**
	 * The generator already makes the spawn cell a flat plaza at y=72.  This method makes the
	 * arrival absolutely safe even if the player used the lantern before that chunk was generated.
	 */
	private static void prepareEntryPad(ServerLevel level) {
		// Force generation of the spawn chunk first.
		level.getChunk(0, 0);

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = -4; dx <= 4; dx++) {
			for (int dz = -4; dz <= 4; dz++) {
				pos.set(dx, UnderworldPostProcess.SPAWN_Y, dz);
				level.setBlock(pos, WcnmBlocks.SOUL_BLOCK.defaultBlockState(), 3);

				for (int dy = 1; dy <= 4; dy++) {
					pos.set(dx, UnderworldPostProcess.SPAWN_Y + dy, dz);
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
				}
			}
		}

		// A single guiding lamp marks the arrival point without blocking the player.
		pos.set(0, UnderworldPostProcess.SPAWN_Y + 1, -3);
		level.setBlock(pos, WcnmBlocks.SOUL_LAMP.defaultBlockState(), 3);
	}
}
