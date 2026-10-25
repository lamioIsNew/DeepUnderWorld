package com.wcnm.world;

import com.wcnm.entity.GhostSoldier;
import com.wcnm.entity.UnderworldHound;
import com.wcnm.entity.UnderworldSpirit;
import com.wcnm.registry.WcnmEntities;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.levelgen.Heightmap;

/** Keeps a small, sparse population of ambient spirits around players in the underworld. */
public final class UnderworldAmbience {
	private static final int SPAWN_INTERVAL_TICKS = 100;
	private static final int SPIRIT_RADIUS = 48;
	private static final int MAX_SPIRITS = 10;
	private static final int MAX_HOSTILES = 3;

	private UnderworldAmbience() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % SPAWN_INTERVAL_TICKS != 0) {
				return;
			}

			ServerLevel level = server.getLevel(WcnmWorldgen.UNDERWORLD);

			if (level == null) {
				return;
			}

			for (ServerPlayer player : level.players()) {
				trySpawnAround(level, player);
				trySpawnHostile(level, player);
			}
		});
	}

	private static void trySpawnAround(ServerLevel level, ServerPlayer player) {
		List<UnderworldSpirit> nearby = level.getEntitiesOfClass(
			UnderworldSpirit.class,
			player.getBoundingBox().inflate(SPIRIT_RADIUS),
			UnderworldSpirit::isAlive
		);

		if (nearby.size() >= MAX_SPIRITS) {
			return;
		}

		RandomSource random = level.getRandom();
		double angle = random.nextDouble() * Math.PI * 2.0;
		int distance = 10 + random.nextInt(18);
		int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
		int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
		level.getChunk(x >> 4, z >> 4);
		int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		BlockPos pos = new BlockPos(x, surface + 1 + random.nextInt(3), z);

		if (pos.getY() >= level.getMaxY() - 1
			|| !level.isEmptyBlock(pos)
			|| !level.isEmptyBlock(pos.above())
			|| !level.getFluidState(pos).isEmpty()) {
			return;
		}

		if (random.nextFloat() < 0.45F) {
			WcnmEntities.GHOST_WISP.spawn(level, pos, EntitySpawnReason.EVENT);
		} else {
			WcnmEntities.WANDERING_SOUL.spawn(level, pos, EntitySpawnReason.EVENT);
		}
	}

	private static void trySpawnHostile(ServerLevel level, ServerPlayer player) {
		int soldiers = level.getEntitiesOfClass(
			GhostSoldier.class,
			player.getBoundingBox().inflate(SPIRIT_RADIUS),
			GhostSoldier::isAlive
		).size();
		int hounds = level.getEntitiesOfClass(
			UnderworldHound.class,
			player.getBoundingBox().inflate(SPIRIT_RADIUS),
			UnderworldHound::isAlive
		).size();

		if (soldiers + hounds >= MAX_HOSTILES) {
			return;
		}

		RandomSource random = level.getRandom();

		if (random.nextFloat() > 0.3F) {
			return;
		}

		double angle = random.nextDouble() * Math.PI * 2.0;
		int distance = 18 + random.nextInt(14);
		int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
		int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
		level.getChunk(x >> 4, z >> 4);
		int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		BlockPos pos = new BlockPos(x, surface + 1, z);

		if (pos.getY() <= level.getMinY() + 1
			|| pos.getY() >= level.getMaxY() - 1
			|| !level.isEmptyBlock(pos)
			|| !level.isEmptyBlock(pos.above())
			|| !level.getFluidState(pos).isEmpty()) {
			return;
		}

		if (random.nextBoolean()) {
			WcnmEntities.GHOST_SOLDIER.spawn(level, pos, EntitySpawnReason.EVENT);
		} else {
			WcnmEntities.UNDERWORLD_HOUND.spawn(level, pos, EntitySpawnReason.EVENT);
		}
	}
}
