package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ProductionMonitorResetPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ProductionMonitorResetPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "production_monitor_reset"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProductionMonitorResetPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ProductionMonitorResetPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ProductionMonitorResetPayload(buf.readBlockPos());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ProductionMonitorResetPayload payload) {
                    buf.writeBlockPos(payload.pos());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
