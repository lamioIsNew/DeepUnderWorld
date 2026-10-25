package com.wcnm.client;

import net.fabricmc.api.ClientModInitializer;

public class WcnmModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WcnmClientConfig.load();
		WcnmClientNetworking.init();
	}
}
