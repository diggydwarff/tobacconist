package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

public record VaporPuffPayload(UUID player, int vegetable, int draw, int output) implements CustomPacketPayload {
    public static final Type<VaporPuffPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "vapor_puff"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VaporPuffPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> { buffer.writeUUID(payload.player); buffer.writeVarInt(payload.vegetable); buffer.writeVarInt(payload.draw); buffer.writeVarInt(payload.output); },
            buffer -> new VaporPuffPayload(buffer.readUUID(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
