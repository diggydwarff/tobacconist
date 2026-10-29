package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.network.FriendlyByteBuf;
import com.diggydwarff.tobacconistmod.backport.StreamCodec;
import com.diggydwarff.tobacconistmod.backport.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

/** Server heartbeat for the equipped cosmetic profile and liquid ratio. */
public record MouthEffectStatePayload(UUID player,int profile,int vegetable) implements CustomPacketPayload {
    public static final Type<MouthEffectStatePayload> TYPE=new Type<>(new ResourceLocation(TobacconistMod.MODID,"mouth_effect_state"));
    public static final StreamCodec<FriendlyByteBuf,MouthEffectStatePayload> CODEC=StreamCodec.of(
            (buffer,payload) -> { buffer.writeUUID(payload.player);buffer.writeVarInt(payload.profile);buffer.writeVarInt(payload.vegetable); },
            buffer -> new MouthEffectStatePayload(buffer.readUUID(),buffer.readVarInt(),buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
