package com.wcnm.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Shared floating, no-gravity behaviour for the underworld's ambient spirits. */
public abstract class UnderworldSpirit extends PathfinderMob {
	protected UnderworldSpirit(EntityType<? extends UnderworldSpirit> type, Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl(this, 20, true);
		this.setNoGravity(true);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new FlyingPathNavigation(this, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new HoverWanderGoal(this));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	@Override
	public void travel(Vec3 movementInput) {
		Vec3 motion = this.getDeltaMovement();

		if (motion.y < -0.12) {
			this.setDeltaMovement(motion.x, -0.12, motion.z);
		}

		super.travel(movementInput);
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageMultiplier,
								   net.minecraft.world.damagesource.DamageSource damageSource) {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return distanceToClosestPlayer > 64.0;
	}
}
