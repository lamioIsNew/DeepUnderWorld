package com.wcnm.world;

import com.wcnm.WcnmMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class WcnmWorldgen {
	public static final ResourceKey<Level> UNDERWORLD =
		ResourceKey.create(Registries.DIMENSION, WcnmMod.id("underworld"));

	private WcnmWorldgen() {
	}

	public static void init() {
		Registry.register(
			BuiltInRegistries.CHUNK_GENERATOR,
			WcnmMod.id("underworld"),
			UnderworldChunkGenerator.CODEC
		);
	}
}
