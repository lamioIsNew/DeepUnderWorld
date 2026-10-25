package com.wcnm.item;

import com.wcnm.world.UnderworldTeleporter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * 引魂灯：右键空气或在潜行时右键方块可往返阴阳两界；普通右键方块则放置为灯。
 */
public class GuidingLanternItem extends BlockItem {
	public GuidingLanternItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer) {
			return teleport(serverPlayer);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();

		if (player != null && player.isShiftKeyDown()) {
			if (player instanceof ServerPlayer serverPlayer) {
				return teleport(serverPlayer);
			}

			return InteractionResult.SUCCESS;
		}

		return super.useOn(context);
	}

	private InteractionResult teleport(ServerPlayer player) {
		boolean wasInUnderworld = UnderworldTeleporter.isInUnderworld(player);
		boolean success = wasInUnderworld
			? UnderworldTeleporter.teleportOut(player)
			: UnderworldTeleporter.teleportIn(player);

		if (success) {
			player.sendOverlayMessage(Component.literal(
				wasInUnderworld ? "引魂灯已引你返回阳间" : "引魂灯已将你引入冥界"
			));
			return InteractionResult.SUCCESS_SERVER;
		}

		player.sendOverlayMessage(Component.literal("冥界之门尚未开启"));
		return InteractionResult.FAIL;
	}
}
