package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server start/stop input for the Curios mouth smoking key. */
public record SmokeMouthItemPayload(boolean drawing) implements CustomPacketPayload {
    public static final SmokeMouthItemPayload START = new SmokeMouthItemPayload(true);
    public static final SmokeMouthItemPayload STOP = new SmokeMouthItemPayload(false);
    public static final Type<SmokeMouthItemPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "smoke_mouth_item")
    );
    public static final StreamCodec<ByteBuf, SmokeMouthItemPayload> STREAM_CODEC = ByteBufCodecs.BOOL.map(SmokeMouthItemPayload::new, SmokeMouthItemPayload::drawing);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
