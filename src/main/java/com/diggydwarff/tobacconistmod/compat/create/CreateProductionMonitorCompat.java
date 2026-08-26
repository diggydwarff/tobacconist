package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Create-side implementation for Production Monitor targets and ghost filters. */
public final class CreateProductionMonitorCompat {
    private CreateProductionMonitorCompat() {}

    public static void register() {
        CreateCompat.installProductionMonitorBridge(new CreateCompat.ProductionMonitorBridge() {
            @Override
            public boolean isSupportedTarget(Level level, BlockPos pos) {
                BlockEntity be = level.getBlockEntity(pos);
                return be instanceof BeltBlockEntity || be instanceof FunnelBlockEntity || be instanceof ChuteBlockEntity;
            }

            @Override
            public void observe(ProductionMonitorBlockEntity monitor, BlockPos targetPos) {
                Level level = monitor.getLevel();
                if (level == null || level.isClientSide) return;
                if (!(level.getBlockEntity(targetPos) instanceof BeltBlockEntity belt)) return;

                BeltInventory inventory = belt.getInventory();
                if (inventory == null) return;
                float center = belt.index + 0.5F;
                for (TransportedItemStack transported : inventory.getTransportedItems()) {
                    if (transported == null || transported.stack.isEmpty()) continue;
                    float previous = transported.prevBeltPosition;
                    float current = transported.beltPosition;
                    boolean crossed = previous < center && current >= center
                            || previous > center && current <= center;
                    if (crossed) monitor.recordTransfer(transported.stack);
                }
            }

            @Override
            public boolean isCreateFilter(ItemStack filter) {
                return filter.getItem() instanceof FilterItem;
            }

            @Override
            public boolean matchesFilter(Level level, ItemStack filter, ItemStack candidate) {
                return FilterItemStack.of(filter.copy()).test(level, candidate);
            }
        });
    }
}
