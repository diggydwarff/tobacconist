package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Client -> server Production Monitor configuration commit. */
public record ProductionMonitorConfigPayload(BlockPos pos, int target, int countMode, int atTarget,
                                             int output, boolean externalReset, ItemStack filter)
        implements CustomPacketPayload {
    public static final Type<ProductionMonitorConfigPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "production_monitor_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProductionMonitorConfigPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ProductionMonitorConfigPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ProductionMonitorConfigPayload(
                            buf.readBlockPos(),
                            buf.readVarInt(),
                            buf.readUnsignedByte(),
                            buf.readUnsignedByte(),
                            buf.readUnsignedByte(),
                            buf.readBoolean(),
                            ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ProductionMonitorConfigPayload payload) {
                    buf.writeBlockPos(payload.pos());
                    buf.writeVarInt(payload.target());
                    buf.writeByte(payload.countMode());
                    buf.writeByte(payload.atTarget());
                    buf.writeByte(payload.output());
                    buf.writeBoolean(payload.externalReset());
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.filter());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
