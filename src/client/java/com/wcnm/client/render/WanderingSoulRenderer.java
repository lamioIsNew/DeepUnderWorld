package com.wcnm.client.render;

import com.wcnm.WcnmMod;
import com.wcnm.entity.WanderingSoul;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class WanderingSoulRenderer extends AbstractSpiritRenderer<WanderingSoul> {
	public WanderingSoulRenderer(EntityRendererProvider.Context context) {
		super(context, WcnmMod.id("textures/entity/wandering_soul.png"), 0.35F, 1.0F);
	}
}
