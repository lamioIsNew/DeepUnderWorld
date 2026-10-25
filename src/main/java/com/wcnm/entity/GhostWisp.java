package com.wcnm.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** 鬼火：更小、更快的幽蓝色漂浮光点。 */
public class GhostWisp extends UnderworldSpirit {
	public GhostWisp(EntityType<? extends GhostWisp> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 6.0)
			.add(Attributes.FLYING_SPEED, 0.65)
			.add(Attributes.MOVEMENT_SPEED, 0.22)
			.add(Attributes.FOLLOW_RANGE, 20.0);
	}
}
