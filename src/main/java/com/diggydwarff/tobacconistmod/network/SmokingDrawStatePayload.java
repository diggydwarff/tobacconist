package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import io.netty.buffer.ByteBuf;
import com.diggydwarff.tobacconistmod.backport.ByteBufCodecs;
import com.diggydwarff.tobacconistmod.backport.StreamCodec;
import com.diggydwarff.tobacconistmod.backport.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Only cosmetic mouth-slot draw state; the server owns completion and tobacco consumption. */
public record SmokingDrawStatePayload(int entityId, int ticks) implements CustomPacketPayload {
    public static final Type<SmokingDrawStatePayload> TYPE = new Type<>(
            new ResourceLocation(TobacconistMod.MODID, "smoking_draw_state"));
    public static final StreamCodec<ByteBuf, SmokingDrawStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SmokingDrawStatePayload::entityId,
            ByteBufCodecs.VAR_INT, SmokingDrawStatePayload::ticks,
            SmokingDrawStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
