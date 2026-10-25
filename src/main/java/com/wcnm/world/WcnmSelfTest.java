package com.wcnm.world;

import com.wcnm.WcnmMod;
import com.wcnm.registry.WcnmBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;

/** Development-only smoke test for the overworld-noise + post-process underworld. */
public final class WcnmSelfTest {
	private WcnmSelfTest() {
	}

	public static void run(MinecraftServer server) {
		ServerLevel level = server.getLevel(WcnmWorldgen.UNDERWORLD);

		if (level == null) {
			WcnmMod.LOGGER.error("WCNM self-test FAIL: wcnm:underworld level is missing");
			return;
		}

		WcnmMod.LOGGER.info("WCNM self-test: dimension loaded, seed={}", level.getChunkSource().randomState().seed());
		level.getChunk(0, 0);
		checkBlock(level, "spawn pad", 0, UnderworldPostProcess.SPAWN_Y, 0, WcnmBlocks.SOUL_BLOCK);
		checkSign(level, "spawn gate", 0, UnderworldPostProcess.SPAWN_Y + 1, -5, "鬼门关");

		UnderworldChunkGenerator generator = (UnderworldChunkGenerator) level.getChunkSource().getGenerator();
		UnderworldPostProcess.Site site = generator.findNearestBuilding(level, 0, 0, 1024);

		if (site == null) {
			WcnmMod.LOGGER.error("WCNM self-test FAIL: no building site found within 1024 blocks");
			return;
		}

		BuildingCatalog.Building building = BuildingCatalog.get(site.number());
		int signZ = site.centerZ() + building.depth() / 2 + 3;

		level.getChunk(site.centerX() >> 4, site.centerZ() >> 4);
		level.getChunk(signZ >> 4, site.centerZ() >> 4);
		WcnmMod.LOGGER.info("WCNM self-test: building #{} {} at ({}, {}, {}) sign ({}, {}, {})",
			building.number(), building.name(), site.centerX(), site.baseY(), site.centerZ(),
			site.centerX(), site.baseY() + 1, signZ);

		checkBlock(level, "building platform", site.centerX(), site.baseY(), site.centerZ(), WcnmBlocks.YIN_STONE_BRICKS);
		checkBlock(level, "building pedestal", site.centerX(), site.baseY(), signZ, WcnmBlocks.SOUL_LAMP);
		checkSign(level, "building sign", site.centerX(), site.baseY() + 1, signZ, building.name());

		int h = generator.getBaseHeight(512, 512, net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
			level, level.getChunkSource().randomState());
		BlockState surface = level.getBlockState(new BlockPos(512, h - 1, 512));
		WcnmMod.LOGGER.info("WCNM self-test: natural column (512,512) height={} block={}", h, surface);

		int riverX = (int) Math.round(UnderworldPostProcess.riverCenterX(0));
		level.getChunk(riverX >> 4, 0);
		int riverWaterY = -999;

		for (int y = -60; y <= 300; y++) {
			if (level.getBlockState(new BlockPos(riverX, y, 0)).is(net.minecraft.world.level.block.Blocks.WATER)) {
				riverWaterY = y;
				break;
			}
		}

		WcnmMod.LOGGER.info("WCNM self-test: river sample at ({}, 0) waterY={}", riverX, riverWaterY);

		// Development smoke test for the new lookup commands (console source has permission level 4).
		var commandSource = server.createCommandSourceStack();
		server.getCommands().performPrefixedCommand(commandSource, "wcnm find radius 300");
		server.getCommands().performPrefixedCommand(commandSource, "wcnm find structure ghost-gate");
		server.getCommands().performPrefixedCommand(commandSource, "wcnm find biome underworld");
		server.getCommands().performPrefixedCommand(commandSource, "wcnm ls");
	}

	private static void checkBlock(ServerLevel level, String label, int x, int y, int z, Block expected) {
		BlockState state = level.getBlockState(new BlockPos(x, y, z));

		if (state.is(expected)) {
			WcnmMod.LOGGER.info("WCNM self-test OK: {} at ({},{},{}) = {}", label, x, y, z, state);
		} else {
			WcnmMod.LOGGER.error("WCNM self-test FAIL: {} at ({},{},{}) expected {} but got {}",
				label, x, y, z, expected, state);
		}
	}

	private static void checkSign(ServerLevel level, String label, int x, int y, int z, String expected) {
		BlockEntity blockEntity = level.getBlockEntity(new BlockPos(x, y, z));

		if (!(blockEntity instanceof SignBlockEntity sign)) {
			WcnmMod.LOGGER.error("WCNM self-test FAIL: {} at ({},{},{}) is not a sign ({})",
				label, x, y, z, blockEntity);
			return;
		}

		String line0 = sign.getText(SignTextSlot.FRONT).getMessages(false).get(0).getString();
		String line1 = sign.getText(SignTextSlot.FRONT).getMessages(false).get(1).getString();
		boolean glowing = sign.getText(SignTextSlot.FRONT).hasGlowingText();
		WcnmMod.LOGGER.info("WCNM self-test OK: {} text=[{} | {}] glowing={} (expected name={})",
			label, line0, line1, glowing, expected);
	}
}
