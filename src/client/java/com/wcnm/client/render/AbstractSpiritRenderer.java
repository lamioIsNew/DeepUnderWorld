package com.wcnm.client.render;

import com.wcnm.entity.UnderworldSpirit;
import net.minecraft.client.model.monster.vex.VexModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** Renders the floating spirits with the vanilla vex model and custom textures. */
abstract class AbstractSpiritRenderer<T extends UnderworldSpirit>
	extends MobRenderer<T, VexRenderState, VexModel> {
	private final Identifier texture;
	private final float scale;

	protected AbstractSpiritRenderer(EntityRendererProvider.Context context, Identifier texture, float shadow, float scale) {
		super(context, new VexModel(context.bakeLayer(ModelLayers.VEX)), shadow);
		this.texture = texture;
		this.scale = scale;
	}

	@Override
	public VexRenderState createRenderState() {
		return new VexRenderState();
	}

	@Override
	public void extractRenderState(T entity, VexRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.isCharging = false;
		state.scale = this.scale;
	}

	@Override
	public Identifier getTextureLocation(VexRenderState state) {
		return this.texture;
	}

	@Override
	protected int getBlockLightLevel(T entity, BlockPos pos) {
		return 15;
	}
}
