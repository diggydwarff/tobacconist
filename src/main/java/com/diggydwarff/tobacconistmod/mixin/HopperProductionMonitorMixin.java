package com.diggydwarff.tobacconistmod.mixin;

import com.diggydwarff.tobacconistmod.util.ProductionMonitorTransferHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Counts the actual inventory delta when a vanilla Hopper successfully ejects items. */
@Mixin(HopperBlockEntity.class)
public abstract class HopperProductionMonitorMixin {
    @Unique
    private static final ThreadLocal<Deque<tobacconist$HopperSnapshot>> TOBACCONIST$HOPPER_SNAPSHOTS =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "ejectItems", at = @At("HEAD"))
    private static void tobacconist$captureHopperOutput(Level level, BlockPos pos, HopperBlockEntity hopper,
                                                        CallbackInfoReturnable<Boolean> cir) {
        List<ItemStack> before = new ArrayList<>(hopper.getContainerSize());
        for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
            before.add(hopper.getItem(slot).copy());
        }
        TOBACCONIST$HOPPER_SNAPSHOTS.get().push(new tobacconist$HopperSnapshot(hopper, before));
    }

    @Inject(method = "ejectItems", at = @At("RETURN"))
    private static void tobacconist$recordHopperOutput(Level level, BlockPos pos, HopperBlockEntity hopper,
                                                       CallbackInfoReturnable<Boolean> cir) {
        Deque<tobacconist$HopperSnapshot> snapshots = TOBACCONIST$HOPPER_SNAPSHOTS.get();
        if (snapshots.isEmpty()) return;
        tobacconist$HopperSnapshot snapshot = snapshots.pop();
        if (snapshots.isEmpty()) TOBACCONIST$HOPPER_SNAPSHOTS.remove();

        if (!cir.getReturnValue() || level == null || level.isClientSide) return;

        HopperBlockEntity capturedHopper = snapshot.hopper();
        int slots = Math.min(snapshot.before().size(), capturedHopper.getContainerSize());
        for (int slot = 0; slot < slots; slot++) {
            ItemStack before = snapshot.before().get(slot);
            if (before.isEmpty()) continue;

            ItemStack after = capturedHopper.getItem(slot);
            int moved;
            if (after.isEmpty()) {
                moved = before.getCount();
            } else if (ItemStack.isSameItemSameComponents(before, after)) {
                moved = before.getCount() - after.getCount();
            } else {
                // ejectItems does not normally replace a slot with another item, but if another
                // integration does so during the same call, the original stack left this Hopper.
                moved = before.getCount();
            }

            if (moved > 0) {
                ProductionMonitorTransferHooks.notifySuccessfulTransfer(
                        level, capturedHopper.getBlockPos(), before.copyWithCount(moved));
            }
        }
    }

    @Unique
    private record tobacconist$HopperSnapshot(HopperBlockEntity hopper, List<ItemStack> before) {}
}
