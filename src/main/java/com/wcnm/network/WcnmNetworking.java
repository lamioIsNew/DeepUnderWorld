package com.wcnm.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class WcnmNetworking {
	private WcnmNetworking() {
	}

	public static void init() {
		PayloadTypeRegistry.clientboundPlay().register(FogPayload.TYPE, FogPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(EffectsPayload.TYPE, EffectsPayload.CODEC);
	}
}
