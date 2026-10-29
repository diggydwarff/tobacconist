package com.diggydwarff.tobacconistmod.novelty;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

/** Cosmetic only. The server validates the draw and spends resources before sending. */
public record NoveltyPuffPayload(UUID player, int kind, int draw, int color) implements CustomPacketPayload {
    public static final Type<NoveltyPuffPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "novelty_puff"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NoveltyPuffPayload> CODEC = StreamCodec.of(
            (buffer, puff) -> { buffer.writeUUID(puff.player); buffer.writeVarInt(puff.kind); buffer.writeVarInt(puff.draw); buffer.writeInt(puff.color); },
            buffer -> new NoveltyPuffPayload(buffer.readUUID(), buffer.readVarInt(), buffer.readVarInt(), buffer.readInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
