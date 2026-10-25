package com.wcnm.client.render;

import com.wcnm.WcnmMod;
import com.wcnm.entity.GhostSoldier;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class GhostSoldierRenderer
	extends MobRenderer<GhostSoldier, SkeletonRenderState, SkeletonModel<SkeletonRenderState>> {
	private static final Identifier TEXTURE = WcnmMod.id("textures/entity/ghost_soldier.png");

	public GhostSoldierRenderer(EntityRendererProvider.Context context) {
		super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.5F);
	}

	@Override
	public SkeletonRenderState createRenderState() {
		return new SkeletonRenderState();
	}

	@Override
	public void extractRenderState(GhostSoldier entity, SkeletonRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.isAggressive = true;
		state.isHoldingBow = false;
		state.isShaking = false;
	}

	@Override
	public Identifier getTextureLocation(SkeletonRenderState state) {
		return TEXTURE;
	}

	@Override
	protected int getBlockLightLevel(GhostSoldier entity, BlockPos pos) {
		return 10;
	}
}
