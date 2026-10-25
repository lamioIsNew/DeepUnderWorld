package com.wcnm.item;

import com.wcnm.world.UnderworldTeleporter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** 引魂灯：点燃后在阴阳两界之间引路。 */
public class GuidingLanternItem extends Item {
	public GuidingLanternItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer) {
			boolean wasInUnderworld = UnderworldTeleporter.isInUnderworld(serverPlayer);
			boolean success = wasInUnderworld
				? UnderworldTeleporter.teleportOut(serverPlayer)
				: UnderworldTeleporter.teleportIn(serverPlayer);

			if (success) {
				serverPlayer.sendOverlayMessage(Component.literal(
					wasInUnderworld ? "引魂灯已引你返回阳间" : "引魂灯已将你引入冥界"
				));
				return InteractionResult.SUCCESS_SERVER;
			}

			serverPlayer.sendOverlayMessage(Component.literal("冥界之门尚未开启"));
			return InteractionResult.FAIL;
		}

		return InteractionResult.SUCCESS;
	}
}
