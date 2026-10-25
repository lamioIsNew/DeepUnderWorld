package com.wcnm.registry;

import com.wcnm.WcnmMod;
import com.wcnm.entity.GhostWisp;
import com.wcnm.entity.WanderingSoul;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

public final class WcnmEntities {
	public static final EntityType<WanderingSoul> WANDERING_SOUL = register(
		"wandering_soul",
		EntityType.Builder.of(WanderingSoul::new, MobCategory.AMBIENT)
			.sized(0.7F, 1.5F)
			.clientTrackingRange(10)
			.noLootTable()
	);

	public static final EntityType<GhostWisp> GHOST_WISP = register(
		"ghost_wisp",
		EntityType.Builder.of(GhostWisp::new, MobCategory.AMBIENT)
			.sized(0.45F, 0.45F)
			.clientTrackingRange(10)
			.noLootTable()
	);

	private WcnmEntities() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(WANDERING_SOUL, WanderingSoul.createAttributes());
		FabricDefaultAttributeRegistry.register(GHOST_WISP, GhostWisp.createAttributes());
	}

	private static <T extends Mob> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, WcnmMod.id(name));
		EntityType<T> type = builder.build(key);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
		return type;
	}
}
