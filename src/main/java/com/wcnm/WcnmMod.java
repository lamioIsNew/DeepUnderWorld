package com.wcnm;

import com.wcnm.command.WcnmCommands;
import com.wcnm.registry.WcnmBlocks;
import com.wcnm.registry.WcnmCreativeTabs;
import com.wcnm.registry.WcnmEntities;
import com.wcnm.registry.WcnmItems;
import com.wcnm.network.WcnmNetworking;
import com.wcnm.world.UnderworldAmbience;
import com.wcnm.world.UnderworldPostProcess;
import com.wcnm.world.WcnmSelfTest;
import com.wcnm.world.WcnmWorldgen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WcnmMod implements ModInitializer {
	public static final String MOD_ID = "wcnm";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WcnmBlocks.init();
		WcnmItems.init();
		WcnmEntities.init();
		WcnmCreativeTabs.init();
		WcnmWorldgen.init();
		UnderworldAmbience.init();
		WcnmNetworking.init();
		WcnmCommands.init();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (server.getLevel(WcnmWorldgen.UNDERWORLD) != null) {
				UnderworldPostProcess.repairSpawnGate(server.getLevel(WcnmWorldgen.UNDERWORLD));
			}
		});

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			ServerLifecycleEvents.SERVER_STARTED.register(WcnmSelfTest::run);
		}

		LOGGER.info("WCNM Alpha initialized: 冥界 dimension and underworld content registered.");
	}



	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
