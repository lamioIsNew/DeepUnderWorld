package com.wcnm.client;

import com.wcnm.network.EffectsPayload;
import com.wcnm.network.FogPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class WcnmClientNetworking {
	private WcnmClientNetworking() {
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(FogPayload.TYPE, (payload, context) -> {
			if (payload.radius() <= 0.0F) {
				WcnmClientConfig.reset();
			} else {
				WcnmClientConfig.setVisibleDistance(payload.radius());
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(EffectsPayload.TYPE, (payload, context) ->
			WcnmClientConfig.setEffectsEnabled(payload.enabled()));
	}
}
