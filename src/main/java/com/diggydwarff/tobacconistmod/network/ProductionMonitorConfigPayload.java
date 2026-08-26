package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import com.diggydwarff.tobacconistmod.screen.ProductionMonitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server Production Monitor configuration commit. */
public record ProductionMonitorConfigPayload(BlockPos pos, int target, int countMode, int atTarget,
                                             int output, boolean externalReset, ItemStack filter) {
    public static void encode(ProductionMonitorConfigPayload payload, FriendlyByteBuf buf) {
        buf.writeBlockPos(payload.pos());
        buf.writeVarInt(payload.target());
        buf.writeByte(payload.countMode());
        buf.writeByte(payload.atTarget());
        buf.writeByte(payload.output());
        buf.writeBoolean(payload.externalReset());
        buf.writeItem(payload.filter());
    }

    public static ProductionMonitorConfigPayload decode(FriendlyByteBuf buf) {
        return new ProductionMonitorConfigPayload(
                buf.readBlockPos(), buf.readVarInt(), buf.readUnsignedByte(), buf.readUnsignedByte(),
                buf.readUnsignedByte(), buf.readBoolean(), buf.readItem());
    }

    public static void handle(ProductionMonitorConfigPayload payload, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null
                    || !(player.containerMenu instanceof ProductionMonitorMenu menu)
                    || !menu.getBlockPos().equals(payload.pos())
                    || player.distanceToSqr(payload.pos().getX() + 0.5D, payload.pos().getY() + 0.5D,
                    payload.pos().getZ() + 0.5D) > 64.0D) {
                return;
            }
            if (player.serverLevel().getBlockEntity(payload.pos()) instanceof ProductionMonitorBlockEntity monitor) {
                monitor.applyConfiguration(payload.target(), payload.countMode(), payload.atTarget(),
                        payload.output(), payload.externalReset(), payload.filter());
            }
        });
        context.setPacketHandled(true);
    }
}
