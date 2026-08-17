package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server request to take one deliberate puff from the Curios mouth item. */
public record SmokeMouthItemPayload() implements CustomPacketPayload {
    public static final SmokeMouthItemPayload INSTANCE = new SmokeMouthItemPayload();
    public static final Type<SmokeMouthItemPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "smoke_mouth_item")
    );
    public static final StreamCodec<ByteBuf, SmokeMouthItemPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
