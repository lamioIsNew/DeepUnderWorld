package com.wcnm.client.render;

import com.wcnm.WcnmMod;
import com.wcnm.entity.UnderworldHound;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class UnderworldHoundRenderer
	extends MobRenderer<UnderworldHound, WolfRenderState, AdultWolfModel> {
	private static final Identifier TEXTURE = WcnmMod.id("textures/entity/underworld_hound.png");

	public UnderworldHoundRenderer(EntityRendererProvider.Context context) {
		super(context, new AdultWolfModel(context.bakeLayer(ModelLayers.WOLF)), 0.5F);
	}

	@Override
	public WolfRenderState createRenderState() {
		return new WolfRenderState();
	}

	@Override
	public void extractRenderState(UnderworldHound entity, WolfRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.isAngry = true;
		state.isSitting = false;
	}

	@Override
	public Identifier getTextureLocation(WolfRenderState state) {
		return TEXTURE;
	}

	@Override
	protected int getBlockLightLevel(UnderworldHound entity, BlockPos pos) {
		return 6;
	}
}
