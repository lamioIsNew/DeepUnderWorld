package com.wcnm.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.datafixers.util.Pair;
import com.wcnm.network.EffectsPayload;
import com.wcnm.network.FogPayload;
import com.wcnm.registry.WcnmItems;
import com.wcnm.world.BuildingCatalog;
import com.wcnm.world.UnderworldChunkGenerator;
import com.wcnm.world.UnderworldPostProcess;
import com.wcnm.world.UnderworldTeleporter;
import com.wcnm.world.WcnmWorldgen;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.Heightmap;

public final class WcnmCommands {
	private static final int MAX_LISTED_BUILDINGS = 200;
	private static final int STRUCTURE_SEARCH_RADIUS = 8192;

	private WcnmCommands() {
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
			dispatcher.register(
				Commands.literal("wcnm")
					.then(Commands.literal("tp")
						.executes(WcnmCommands::teleportIn))
					.then(Commands.literal("return")
						.executes(WcnmCommands::teleportOut))
					.then(Commands.literal("find")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.executes(context -> findNearest(context.getSource(), 256))
						.then(Commands.literal("radius")
							.then(Commands.argument("radius", IntegerArgumentType.integer(1, 4096))
								.executes(context -> findRadius(context.getSource(),
									IntegerArgumentType.getInteger(context, "radius")))))
						.then(Commands.literal("structure")
							.then(Commands.argument("structure", StringArgumentType.greedyString())
								.suggests((context, builder) -> {
									for (BuildingCatalog.Building building : BuildingCatalog.all()) {
										builder.suggest(BuildingCatalog.structureId(building.number()));
									}

									return builder.buildFuture();
								})
								.executes(context -> findStructure(context.getSource(),
									StringArgumentType.getString(context, "structure")))))
						.then(Commands.literal("biome")
							.then(Commands.argument("biome", StringArgumentType.greedyString())
								.suggests((context, builder) -> {
									ServerLevel level = context.getSource().getServer().getLevel(WcnmWorldgen.UNDERWORLD);

									if (level != null) {
										for (Holder<Biome> biome : level.getChunkSource().getGenerator()
											.getBiomeSource().possibleBiomes()) {
											String id = biomeId(biome);

											if (id != null) {
												builder.suggest(id);
											}
										}
									}

									return builder.buildFuture();
								})
								.executes(context -> findBiome(context.getSource(),
									StringArgumentType.getString(context, "biome")))))
						.then(Commands.argument("radius", IntegerArgumentType.integer(1, 4096))
							.executes(context -> findNearest(context.getSource(),
								IntegerArgumentType.getInteger(context, "radius")))))
					.then(Commands.literal("ls")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.executes(context -> listChunk(context.getSource())))
					.then(Commands.literal("kit")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.executes(WcnmCommands::kit))
					.then(Commands.literal("tud")
						.then(Commands.literal("visibleDistance")
							.then(Commands.argument("radius",
									FloatArgumentType.floatArg(8.0F, 512.0F))
								.executes(context -> setFog(context.getSource(),
									FloatArgumentType.getFloat(context, "radius"))))
							.then(Commands.literal("reset")
								.executes(context -> resetFog(context.getSource()))))
						.then(Commands.literal("effects")
							.then(Commands.literal("on")
								.executes(context -> setEffects(context.getSource(), true)))
							.then(Commands.literal("off")
								.executes(context -> setEffects(context.getSource(), false)))))
			)
		);
	}

	private static int teleportIn(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		try {
			ServerPlayer player = source.getPlayerOrException();

			if (UnderworldTeleporter.teleportIn(player)) {
				source.sendSuccess(() -> Component.literal("已进入冥界 (0.5, 81, 0.5)"), true);
				return 1;
			}

			source.sendFailure(Component.literal("冥界维度未加载"));
			return 0;
		} catch (Exception exception) {
			source.sendFailure(Component.literal("该命令只能由玩家执行"));
			return 0;
		}
	}

	private static int teleportOut(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		try {
			ServerPlayer player = source.getPlayerOrException();
			UnderworldTeleporter.teleportOut(player);
			source.sendSuccess(() -> Component.literal("已返回主世界"), false);
			return 1;
		} catch (Exception exception) {
			source.sendFailure(Component.literal("该命令只能由玩家执行"));
			return 0;
		}
	}

	private static int kit(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		try {
			ServerPlayer player = source.getPlayerOrException();
			player.getInventory().add(new ItemStack(WcnmItems.GUIDING_LANTERN));
			player.getInventory().add(new ItemStack(WcnmItems.YIN_STONE, 64));
			player.getInventory().add(new ItemStack(WcnmItems.YIN_STONE_BRICKS, 64));
			player.getInventory().add(new ItemStack(WcnmItems.CHISELED_YIN_STONE, 32));
			player.getInventory().add(new ItemStack(WcnmItems.SOUL_LAMP, 16));
			player.getInventory().add(new ItemStack(WcnmItems.BIANHUA, 16));
			source.sendSuccess(() -> Component.literal("已发放冥界建造材料"), false);
			return 1;
		} catch (Exception exception) {
			source.sendFailure(Component.literal("该命令只能由玩家执行"));
			return 0;
		}
	}

	private static int setFog(CommandSourceStack source, float radius) {
		try {
			ServerPlayer player = source.getPlayerOrException();

			if (!ServerPlayNetworking.canSend(player, FogPayload.TYPE)) {
				source.sendFailure(Component.literal("客户端未安装 WCNM，无法调节雾距"));
				return 0;
			}

			ServerPlayNetworking.send(player, new FogPayload(radius));
			source.sendSuccess(() -> Component.literal(String.format(
				"冥界可视距离已设为 %.0f 格（只影响你自己）", radius
			)), false);
			return 1;
		} catch (Exception exception) {
			source.sendFailure(Component.literal("该命令只能由玩家执行"));
			return 0;
		}
	}

	private static int resetFog(CommandSourceStack source) {
		try {
			ServerPlayer player = source.getPlayerOrException();

			if (!ServerPlayNetworking.canSend(player, FogPayload.TYPE)) {
				source.sendFailure(Component.literal("客户端未安装 WCNM，无法调节雾距"));
				return 0;
			}

			ServerPlayNetworking.send(player, new FogPayload(-1.0F));
			source.sendSuccess(() -> Component.literal("冥界雾距已恢复维度默认值"), false);
			return 1;
		} catch (Exception exception) {
			source.sendFailure(Component.literal("该命令只能由玩家执行"));
			return 0;
		}
	}

	private static int setEffects(CommandSourceStack source, boolean enabled) {
		try {
			ServerPlayer player = source.getPlayerOrException();

			if (!ServerPlayNetworking.canSend(player, EffectsPayload.TYPE)) {
				source.sendFailure(Component.literal("客户端未安装 WCNM，无法切换冥界特效"));
				return 0;
			}

			ServerPlayNetworking.send(player, new EffectsPayload(enabled));
			source.sendSuccess(() -> Component.literal(
				enabled ? "冥界特效已开启" : "冥界特效已关闭（移除雾与暗色滤镜）"
			), false);
			return 1;
		} catch (Exception exception) {
			source.sendFailure(Component.literal("该命令只能由玩家执行"));
			return 0;
		}
	}

	private static int findNearest(CommandSourceStack source, int radiusBlocks) {
		ServerLevel level = source.getServer().getLevel(WcnmWorldgen.UNDERWORLD);

		if (level == null) {
			source.sendFailure(Component.literal("冥界维度未加载"));
			return 0;
		}

		if (!(level.getChunkSource().getGenerator() instanceof UnderworldChunkGenerator generator)) {
			source.sendFailure(Component.literal("冥界生成器类型不匹配"));
			return 0;
		}

		ServerPlayer player = source.getPlayer();
		int centerX = player == null ? (int) Math.floor(source.getPosition().x) : player.getBlockX();
		int centerZ = player == null ? (int) Math.floor(source.getPosition().z) : player.getBlockZ();
		UnderworldPostProcess.Site site = generator.findNearestBuilding(level, centerX, centerZ, radiusBlocks);

		if (site == null) {
			source.sendFailure(Component.literal("半径内没有找到冥筑"));
			return 0;
		}

		source.sendSuccess(() -> Component.literal("最近冥筑（半径 " + radiusBlocks + " 格）")
			.withStyle(ChatFormatting.AQUA), false);
		source.sendSuccess(() -> buildingLine(site, centerX, centerZ), false);
		return 1;
	}

	private static int findRadius(CommandSourceStack source, int radiusBlocks) {
		ServerLevel level = source.getServer().getLevel(WcnmWorldgen.UNDERWORLD);

		if (level == null) {
			source.sendFailure(Component.literal("冥界维度未加载"));
			return 0;
		}

		if (!(level.getChunkSource().getGenerator() instanceof UnderworldChunkGenerator generator)) {
			source.sendFailure(Component.literal("冥界生成器类型不匹配"));
			return 0;
		}

		ServerPlayer player = source.getPlayer();
		int centerX = player == null ? (int) Math.floor(source.getPosition().x) : player.getBlockX();
		int centerZ = player == null ? (int) Math.floor(source.getPosition().z) : player.getBlockZ();
		List<UnderworldPostProcess.Site> sites = generator.findBuildingsInRadius(level, centerX, centerZ, radiusBlocks);

		if (sites.isEmpty()) {
			source.sendFailure(Component.literal("半径 " + radiusBlocks + " 格内没有冥筑"));
			return 0;
		}

		int shown = Math.min(sites.size(), MAX_LISTED_BUILDINGS);
		int total = sites.size();
		String header = total > MAX_LISTED_BUILDINGS
			? String.format("半径 %d 格内共有 %d 座冥筑（点击绿色坐标传送，最多显示 %d 条）",
				radiusBlocks, total, MAX_LISTED_BUILDINGS)
			: String.format("半径 %d 格内共有 %d 座冥筑（点击绿色坐标传送）", radiusBlocks, total);
		source.sendSuccess(() -> Component.literal(header).withStyle(ChatFormatting.AQUA), false);

		for (int index = 0; index < shown; index++) {
			UnderworldPostProcess.Site site = sites.get(index);
			source.sendSuccess(() -> buildingLine(site, centerX, centerZ), false);
		}

		if (total > shown) {
			int hidden = total - shown;
			source.sendSuccess(() -> Component.literal("还有 " + hidden + " 座未显示，请缩小半径")
				.withStyle(ChatFormatting.GRAY), false);
		}

		return 1;
	}

	private static int findStructure(CommandSourceStack source, String query) {
		BuildingCatalog.Building building = BuildingCatalog.find(query);

		if (building == null) {
			source.sendFailure(Component.literal("未知结构：" + query
				+ "。请使用英文 ID（Tab 可补全），例如 ghost-gate、naihe-bridge、fengdu-city"));
			return 0;
		}

		ServerLevel level = source.getServer().getLevel(WcnmWorldgen.UNDERWORLD);

		if (level == null) {
			source.sendFailure(Component.literal("冥界维度未加载"));
			return 0;
		}

		if (!(level.getChunkSource().getGenerator() instanceof UnderworldChunkGenerator generator)) {
			source.sendFailure(Component.literal("冥界生成器类型不匹配"));
			return 0;
		}

		ServerPlayer player = source.getPlayer();
		int centerX = player == null ? (int) Math.floor(source.getPosition().x) : player.getBlockX();
		int centerZ = player == null ? (int) Math.floor(source.getPosition().z) : player.getBlockZ();
		UnderworldPostProcess.Site site = generator.findNearestBuilding(level, centerX, centerZ,
			building.number(), STRUCTURE_SEARCH_RADIUS);

		if (site == null) {
			source.sendFailure(Component.literal(String.format(
				"半径 %d 格内没有找到第%d号 %s (%s)",
				STRUCTURE_SEARCH_RADIUS, building.number(), building.name(), BuildingCatalog.englishName(building.number())
			)));
			return 0;
		}

		source.sendSuccess(() -> Component.literal(String.format(
			"已定位结构：第%d号 %s / %s（搜索半径 %d 格）",
			building.number(), building.name(), BuildingCatalog.englishName(building.number()), STRUCTURE_SEARCH_RADIUS
		)).withStyle(ChatFormatting.AQUA), false);
		source.sendSuccess(() -> buildingLine(site, centerX, centerZ), false);
		return 1;
	}

	private static int findBiome(CommandSourceStack source, String query) {
		ServerLevel level = source.getServer().getLevel(WcnmWorldgen.UNDERWORLD);

		if (level == null) {
			source.sendFailure(Component.literal("冥界维度未加载"));
			return 0;
		}

		BiomeSource biomeSource = level.getChunkSource().getGenerator().getBiomeSource();
		List<Holder<Biome>> matches = biomeSource.possibleBiomes().stream()
			.filter(holder -> biomeMatches(holder, query))
			.toList();

		if (matches.isEmpty()) {
			String available = biomeSource.possibleBiomes().stream()
				.map(WcnmCommands::biomeId)
				.filter(id -> id != null)
				.sorted()
				.collect(Collectors.joining(", "));
			source.sendFailure(Component.literal("冥界没有群系 " + query + "。可用群系：" + available));
			return 0;
		}

		ServerPlayer player = source.getPlayer();
		int centerX = player == null ? 0 : player.getBlockX();
		int centerZ = player == null ? 0 : player.getBlockZ();
		int centerY = player == null ? 80 : player.getBlockY();
		Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(
			holder -> biomeMatches(holder, query),
			new BlockPos(centerX, centerY, centerZ),
			6400, 32, 64
		);

		if (found == null) {
			source.sendFailure(Component.literal("6400 格内没有找到群系 " + query));
			return 0;
		}

		BlockPos pos = found.getFirst();
		String label = biomeLabel(found.getSecond());
		int distance = (int) Math.round(Math.sqrt(
			(double) (pos.getX() - centerX) * (pos.getX() - centerX)
				+ (double) (pos.getZ() - centerZ) * (pos.getZ() - centerZ)
		));
		int surfaceY = surfaceHeight(level, pos.getX(), pos.getZ());
		int teleportY = Math.max(level.getMinY() + 1, surfaceY);

		source.sendSuccess(() -> Component.literal(String.format(
			"最近群系：%s | 距离 %d 格", label, distance
		)).withStyle(ChatFormatting.AQUA), false);
		source.sendSuccess(() -> Component.literal("坐标：")
			.append(coordinates(pos.getX(), teleportY, pos.getZ()))
			.append(Component.literal(" "))
			.append(copyButton(pos.getX() + " " + teleportY + " " + pos.getZ())), false);
		return 1;
	}

	private static int listChunk(CommandSourceStack source) {
		ServerLevel level = source.getLevel();
		ServerPlayer player = source.getPlayer();
		int centerX = player == null ? (int) Math.floor(source.getPosition().x) : player.getBlockX();
		int centerZ = player == null ? (int) Math.floor(source.getPosition().z) : player.getBlockZ();
		int chunkX = centerX >> 4;
		int chunkZ = centerZ >> 4;
		int minX = chunkX << 4;
		int minZ = chunkZ << 4;
		int[][] samples = {{8, 8}, {2, 2}, {13, 2}, {2, 13}, {13, 13}};
		Set<String> biomes = new LinkedHashSet<>();
		int sampledMinY = Integer.MAX_VALUE;
		int sampledMaxY = Integer.MIN_VALUE;

		for (int[] sample : samples) {
			int x = minX + sample[0];
			int z = minZ + sample[1];
			int surfaceY = surfaceHeight(level, x, z);
			sampledMinY = Math.min(sampledMinY, surfaceY);
			sampledMaxY = Math.max(sampledMaxY, surfaceY);
			Holder<Biome> biome = level.getBiome(new BlockPos(x, Math.max(level.getMinY(), surfaceY - 1), z));
			biomes.add(biomeLabel(biome));
		}

		String dimensionId = level.dimension().identifier().toString();
		String biomeText = String.join(" / ", biomes);
		int finalMinY = sampledMinY;
		int finalMaxY = sampledMaxY;
		source.sendSuccess(() -> Component.literal(String.format(
			"[%s] 区块 [%d, %d] | 区域 [%d, %d]",
			dimensionId, chunkX, chunkZ, chunkX >> 5, chunkZ >> 5
		)).withStyle(ChatFormatting.AQUA), false);
		source.sendSuccess(() -> Component.literal("群系：" + biomeText
			+ " | 采样地表 Y=" + finalMinY + ".." + finalMaxY), false);

		boolean underworld = level.getServer().getLevel(WcnmWorldgen.UNDERWORLD) == level;

		if (underworld && level.getChunkSource().getGenerator() instanceof UnderworldChunkGenerator generator) {
			boolean river = UnderworldPostProcess.chunkIntersectsRiver(minX, minZ);
			boolean lake = generator.chunkIntersectsLake(level, chunkX, chunkZ);
			source.sendSuccess(() -> Component.literal("河流：" + (river ? "是" : "否")
				+ " | 湖泊：" + (lake ? "是" : "否")), false);

			List<UnderworldPostProcess.Site> sites = generator.findBuildingsInChunk(level, chunkX, chunkZ);

			if (sites.isEmpty()) {
				source.sendSuccess(() -> Component.literal("区块内冥筑：无").withStyle(ChatFormatting.GRAY), false);
			} else {
				source.sendSuccess(() -> Component.literal("区块内冥筑：" + sites.size() + " 座")
					.withStyle(ChatFormatting.GOLD), false);

				for (UnderworldPostProcess.Site site : sites) {
					source.sendSuccess(() -> buildingLine(site, centerX, centerZ), false);
				}
			}
		} else {
			source.sendSuccess(() -> Component.literal("区块内冥筑：非冥界维度").withStyle(ChatFormatting.GRAY), false);
		}

		return 1;
	}

	private static Component buildingLine(UnderworldPostProcess.Site site, int centerX, int centerZ) {
		BuildingCatalog.Building building = BuildingCatalog.get(site.number());
		int distance = (int) Math.round(Math.sqrt(
			(double) (site.centerX() - centerX) * (site.centerX() - centerX)
				+ (double) (site.centerZ() - centerZ) * (site.centerZ() - centerZ)
		));
		int signZ = site.centerZ() + building.depth() / 2 + 3;
		int teleportY = site.baseY() + 1;
		int teleportZ = signZ + 1;
		String coords = site.centerX() + " " + teleportY + " " + teleportZ;

		MutableComponent line = Component.literal("#" + building.number() + " ")
			.withStyle(ChatFormatting.GOLD)
			.append(Component.literal(building.name()).withStyle(ChatFormatting.WHITE))
			.append(Component.literal(" / " + BuildingCatalog.englishName(building.number()))
				.withStyle(ChatFormatting.GRAY))
			.append(Component.literal("  " + distance + "格  ").withStyle(ChatFormatting.DARK_GRAY))
			.append(coordinates(site.centerX(), teleportY, teleportZ))
			.append(Component.literal(" "))
			.append(copyButton(coords));
		return line;
	}

	private static MutableComponent coordinates(int x, int y, int z) {
		String command = "/execute in wcnm:underworld run tp @s " + x + " " + y + " " + z;
		return Component.literal("(" + x + ", " + y + ", " + z + ")").withStyle(style -> style
			.withColor(ChatFormatting.GREEN)
			.withUnderlined(true)
			.withClickEvent(new ClickEvent.RunCommand(command))
			.withHoverEvent(new HoverEvent.ShowText(
				Component.literal("点击传送到 " + x + " " + y + " " + z)
			)));
	}

	private static MutableComponent copyButton(String value) {
		return Component.literal("[复制]").withStyle(style -> style
			.withColor(ChatFormatting.YELLOW)
			.withClickEvent(new ClickEvent.CopyToClipboard(value))
			.withHoverEvent(new HoverEvent.ShowText(
				Component.literal("复制坐标：" + value)
			)));
	}

	/**
	 * The command may be run in a dimension whose chunks are not loaded yet.  Force the chunk
	 * before reading its heightmap so we never report a fake {@code minY} surface.
	 */
	private static int surfaceHeight(ServerLevel level, int x, int z) {
		level.getChunk(x >> 4, z >> 4);
		return level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
	}

	private static boolean biomeMatches(Holder<Biome> holder, String query) {
		String id = biomeId(holder);

		if (id == null) {
			return false;
		}

		String normalized = query.trim().toLowerCase(Locale.ROOT).replace(' ', '_');

		if (normalized.contains(":")) {
			return id.equalsIgnoreCase(normalized);
		}

		int separator = id.indexOf(':');
		String path = separator >= 0 ? id.substring(separator + 1) : id;
		return path.equalsIgnoreCase(normalized)
			|| id.equalsIgnoreCase("wcnm:" + normalized)
			|| id.equalsIgnoreCase("minecraft:" + normalized);
	}

	private static String biomeId(Holder<Biome> holder) {
		return holder.unwrapKey().map(key -> key.identifier().toString()).orElse(null);
	}

	private static String biomeLabel(Holder<Biome> holder) {
		String id = biomeId(holder);

		if (id == null) {
			return "unknown";
		}

		int separator = id.indexOf(':');
		String path = separator >= 0 ? id.substring(separator + 1) : id;
		StringBuilder pretty = new StringBuilder();

		for (String part : path.split("_")) {
			if (part.isEmpty()) {
				continue;
			}

			if (!pretty.isEmpty()) {
				pretty.append(' ');
			}

			pretty.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}

		return id + " (" + pretty + ")";
	}
}
