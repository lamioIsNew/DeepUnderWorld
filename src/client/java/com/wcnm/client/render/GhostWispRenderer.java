package com.wcnm.client.render;

import com.wcnm.WcnmMod;
import com.wcnm.entity.GhostWisp;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class GhostWispRenderer extends AbstractSpiritRenderer<GhostWisp> {
	public GhostWispRenderer(EntityRendererProvider.Context context) {
		super(context, WcnmMod.id("textures/entity/ghost_wisp.png"), 0.2F, 0.62F);
	}
}
