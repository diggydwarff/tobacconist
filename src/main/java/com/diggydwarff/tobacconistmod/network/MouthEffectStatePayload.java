package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

/** Server heartbeat for the equipped cosmetic profile and liquid ratio. */
public record MouthEffectStatePayload(UUID player,int profile,int vegetable) implements CustomPacketPayload {
    public static final Type<MouthEffectStatePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID,"mouth_effect_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf,MouthEffectStatePayload> CODEC=StreamCodec.of(
            (buffer,payload) -> { buffer.writeUUID(payload.player);buffer.writeVarInt(payload.profile);buffer.writeVarInt(payload.vegetable); },
            buffer -> new MouthEffectStatePayload(buffer.readUUID(),buffer.readVarInt(),buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
