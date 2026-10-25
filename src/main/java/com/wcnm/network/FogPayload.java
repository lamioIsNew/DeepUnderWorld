package com.wcnm.network;

import com.wcnm.WcnmMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -> client: set the local underworld atmospheric fog distance.
 * A radius <= 0 means "restore the dimension default".
 */
public record FogPayload(float radius) implements CustomPacketPayload {
	public static final Type<FogPayload> TYPE = new Type<>(WcnmMod.id("fog_distance"));

	public static final StreamCodec<RegistryFriendlyByteBuf, FogPayload> CODEC = StreamCodec.of(
		(buffer, payload) -> buffer.writeFloat(payload.radius()),
		buffer -> new FogPayload(buffer.readFloat())
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
