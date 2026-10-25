package com.wcnm.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Allay-like hovering wander: pick a point a few blocks above the local surface and fly there.
 * It deliberately never targets the ground, so the spirit does not slam into terrain.
 */
public class HoverWanderGoal extends Goal {
	private final UnderworldSpirit spirit;
	private double targetX;
	private double targetY;
	private double targetZ;

	public HoverWanderGoal(UnderworldSpirit spirit) {
		this.spirit = spirit;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.spirit.getRandom().nextInt(45) != 0) {
			return false;
		}

		double angle = this.spirit.getRandom().nextDouble() * Math.PI * 2.0;
		double distance = 6.0 + this.spirit.getRandom().nextDouble() * 10.0;
		int x = this.spirit.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
		int z = this.spirit.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
		this.spirit.level().getChunk(x >> 4, z >> 4);
		int surface = this.spirit.level()
			.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		double minY = surface + 1.2;
		double maxY = surface + 10.0;
		double desiredY = minY + this.spirit.getRandom().nextDouble() * (maxY - minY);
		desiredY = Math.max(minY, Math.min(desiredY, this.spirit.getY() + 5.0));
		desiredY = Math.max(this.spirit.getY() - 4.0, desiredY);

		this.targetX = x + 0.5;
		this.targetY = Math.max(this.spirit.level().getMinY() + 2.0, desiredY);
		this.targetZ = z + 0.5;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.spirit.getNavigation().isDone();
	}

	@Override
	public void start() {
		this.spirit.getNavigation().moveTo(this.targetX, this.targetY, this.targetZ, 1.0);
	}

	@Override
	public void stop() {
		this.spirit.getNavigation().stop();
	}
}
