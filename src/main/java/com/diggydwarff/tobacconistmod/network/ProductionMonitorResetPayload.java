package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import com.diggydwarff.tobacconistmod.screen.ProductionMonitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ProductionMonitorResetPayload(BlockPos pos) {
    public static void encode(ProductionMonitorResetPayload payload, FriendlyByteBuf buf) {
        buf.writeBlockPos(payload.pos());
    }

    public static ProductionMonitorResetPayload decode(FriendlyByteBuf buf) {
        return new ProductionMonitorResetPayload(buf.readBlockPos());
    }

    public static void handle(ProductionMonitorResetPayload payload, Supplier<NetworkEvent.Context> supplier) {
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
                monitor.resetAccumulatedCount();
            }
        });
        context.setPacketHandled(true);
    }
}
