package com.wcnm.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** 游魂：缓慢漂浮的冥界居民，不会攻击玩家。 */
public class WanderingSoul extends UnderworldSpirit {
	public WanderingSoul(EntityType<? extends WanderingSoul> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 12.0)
			.add(Attributes.FLYING_SPEED, 0.45)
			.add(Attributes.MOVEMENT_SPEED, 0.18)
			.add(Attributes.FOLLOW_RANGE, 24.0);
	}
}
