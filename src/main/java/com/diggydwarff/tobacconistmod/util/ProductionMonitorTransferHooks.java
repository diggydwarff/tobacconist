package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Shared transfer notification entry point for vanilla and optional transport integrations. */
public final class ProductionMonitorTransferHooks {
    private ProductionMonitorTransferHooks() {}

    public static void notifySuccessfulTransfer(Level level, BlockPos transportPos, ItemStack movedStack) {
        if (level == null || level.isClientSide || movedStack == null || movedStack.isEmpty()) return;

        for (Direction direction : Direction.values()) {
            BlockPos monitorPos = transportPos.relative(direction);
            if (level.getBlockEntity(monitorPos) instanceof ProductionMonitorBlockEntity monitor
                    && monitor.getMonitoredPos().equals(transportPos)) {
                monitor.recordTransfer(movedStack.copy());
            }
        }
    }
}
