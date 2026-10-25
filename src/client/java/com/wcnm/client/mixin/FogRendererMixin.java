package com.wcnm.client.mixin;

import com.wcnm.client.WcnmClientConfig;
import com.wcnm.world.WcnmWorldgen;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the underworld's atmospheric fog distance with the value configured by
 * /wcnm tud visibleDistance.  This is client-only: it changes what the local player sees, not
 * the dimension data or other players' clients.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Inject(method = "setupFog", at = @At("RETURN"))
	private void wcnm$applyUnderworldVisibleDistance(
		Camera camera,
		int renderDistanceInChunks,
		DeltaTracker deltaTracker,
		float darkenWorldAmount,
		ClientLevel level,
		CallbackInfoReturnable<FogData> callback
	) {
		if (level == null || !level.dimension().equals(WcnmWorldgen.UNDERWORLD)) {
			return;
		}

		if (camera.getFluidInCamera() != FogType.NONE) {
			return;
		}

		FogData fog = callback.getReturnValue();

		if (fog == null) {
			return;
		}

		if (!WcnmClientConfig.isEffectsEnabled()) {
			// Remove the pink/red fog and the night-vision-brightened fog color entirely.
			fog.environmentalStart = 0.0F;
			fog.environmentalEnd = 1_000_000.0F;
			fog.skyEnd = 1_000_000.0F;
			fog.cloudEnd = 1_000_000.0F;
			fog.color.set(0.012F, 0.012F, 0.018F, 1.0F);
			return;
		}

		if (!WcnmClientConfig.isEnabled()) {
			return;
		}

		float radius = WcnmClientConfig.getVisibleDistance();
		fog.environmentalStart = Math.max(0.0F, radius * 0.35F);
		fog.environmentalEnd = radius;
		fog.skyEnd = radius;
		fog.cloudEnd = radius;
	}
}
