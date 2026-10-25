package com.wcnm.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import com.wcnm.client.render.GhostWispRenderer;
import com.wcnm.client.render.WanderingSoulRenderer;
import com.wcnm.registry.WcnmEntities;

public class WcnmModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WcnmClientConfig.load();
		WcnmClientNetworking.init();
		EntityRendererRegistry.register(WcnmEntities.WANDERING_SOUL, WanderingSoulRenderer::new);
		EntityRendererRegistry.register(WcnmEntities.GHOST_WISP, GhostWispRenderer::new);
	}
}
