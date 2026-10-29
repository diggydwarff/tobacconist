package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.network.FriendlyByteBuf;
import com.diggydwarff.tobacconistmod.backport.StreamCodec;
import com.diggydwarff.tobacconistmod.backport.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

public record VaporPuffPayload(UUID player, int vegetable, int draw, int output) implements CustomPacketPayload {
    public static final Type<VaporPuffPayload> TYPE = new Type<>(new ResourceLocation(TobacconistMod.MODID, "vapor_puff"));
    public static final StreamCodec<FriendlyByteBuf, VaporPuffPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> { buffer.writeUUID(payload.player); buffer.writeVarInt(payload.vegetable); buffer.writeVarInt(payload.draw); buffer.writeVarInt(payload.output); },
            buffer -> new VaporPuffPayload(buffer.readUUID(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
