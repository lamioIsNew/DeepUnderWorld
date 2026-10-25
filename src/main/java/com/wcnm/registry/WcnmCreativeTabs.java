package com.wcnm.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public final class WcnmCreativeTabs {
	private WcnmCreativeTabs() {
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(tab("building_blocks")).register(output -> {
			output.accept(WcnmItems.YIN_STONE);
			output.accept(WcnmItems.YIN_STONE_BRICKS);
			output.accept(WcnmItems.CHISELED_YIN_STONE);
			output.accept(WcnmItems.SOUL_LAMP);
			output.accept(WcnmItems.SOUL_STONE);
		});

		CreativeModeTabEvents.modifyOutputEvent(tab("natural_blocks")).register(output -> {
			output.accept(WcnmItems.SOUL_BLOCK);
			output.accept(WcnmItems.ASHEN_SOIL);
			output.accept(WcnmItems.SOUL_GRAVEL);
			output.accept(WcnmItems.BONE_SOIL);
			output.accept(WcnmItems.BLOOD_MOSS);
			output.accept(WcnmItems.SOUL_GRASS);
			output.accept(WcnmItems.BIANHUA);
		});

		CreativeModeTabEvents.modifyOutputEvent(tab("tools_and_utilities")).register(output -> {
			output.accept(WcnmItems.GUIDING_LANTERN);
		});
	}

	private static ResourceKey<CreativeModeTab> tab(String path) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(path));
	}
}
