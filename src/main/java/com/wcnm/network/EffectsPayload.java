package com.wcnm.network;

import com.wcnm.WcnmMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: enable or disable the local underworld fog/sky effect. */
public record EffectsPayload(boolean enabled) implements CustomPacketPayload {
	public static final Type<EffectsPayload> TYPE = new Type<>(WcnmMod.id("underworld_effects"));

	public static final StreamCodec<RegistryFriendlyByteBuf, EffectsPayload> CODEC = StreamCodec.of(
		(buffer, payload) -> buffer.writeBoolean(payload.enabled()),
		buffer -> new EffectsPayload(buffer.readBoolean())
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
