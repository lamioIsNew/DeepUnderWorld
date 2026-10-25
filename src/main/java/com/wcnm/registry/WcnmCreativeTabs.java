package com.wcnm.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class WcnmCreativeTabs {
	public static final ResourceKey<CreativeModeTab> UNDERWORLD_TAB =
		ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("wcnm", "underworld"));

	private WcnmCreativeTabs() {
	}

	public static void init() {
		Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			UNDERWORLD_TAB,
			FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.wcnm.underworld"))
				.icon(() -> new ItemStack(WcnmItems.GUIDING_LANTERN))
				.displayItems((context, output) -> {
					output.accept(WcnmItems.SOUL_BLOCK);
					output.accept(WcnmItems.ASHEN_SOIL);
					output.accept(WcnmItems.SOUL_GRAVEL);
					output.accept(WcnmItems.BONE_SOIL);
					output.accept(WcnmItems.BLOOD_MOSS);
					output.accept(WcnmItems.SOUL_STONE);
					output.accept(WcnmItems.SOUL_GRASS);
					output.accept(WcnmItems.BIANHUA);
					output.accept(WcnmItems.YIN_STONE);
					output.accept(WcnmItems.YIN_STONE_BRICKS);
					output.accept(WcnmItems.CHISELED_YIN_STONE);
					output.accept(WcnmItems.SOUL_LAMP);
					output.accept(WcnmItems.GUIDING_LANTERN);
					output.accept(WcnmItems.WANDERING_SOUL_SPAWN_EGG);
					output.accept(WcnmItems.GHOST_WISP_SPAWN_EGG);
					output.accept(WcnmItems.GHOST_SOLDIER_SPAWN_EGG);
					output.accept(WcnmItems.UNDERWORLD_HOUND_SPAWN_EGG);
				})
				.build()
		);

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
