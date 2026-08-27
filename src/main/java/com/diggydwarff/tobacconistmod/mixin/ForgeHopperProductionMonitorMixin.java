package com.diggydwarff.tobacconistmod.mixin;

import com.diggydwarff.tobacconistmod.util.ProductionMonitorTransferHooks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraftforge.items.VanillaInventoryCodeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Tracks Forge capability-backed hopper outputs.
 *
 * Forge's patched HopperBlockEntity#ejectItems delegates capability destinations to
 * VanillaInventoryCodeHooks#insertHook. This target is Forge-owned and unobfuscated, avoiding the
 * patched ejectItems descriptor/remapping problem while preserving exact successful-transfer counts.
 */
@Mixin(value = VanillaInventoryCodeHooks.class, remap = false)
public abstract class ForgeHopperProductionMonitorMixin {
    @Unique
    private static final ThreadLocal<Deque<tobacconist$HopperSnapshot>> TOBACCONIST$HOPPER_SNAPSHOTS =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "insertHook", at = @At("HEAD"))
    private static void tobacconist$captureCapabilityOutput(HopperBlockEntity hopper,
                                                            CallbackInfoReturnable<Boolean> cir) {
        List<ItemStack> before = new ArrayList<>(hopper.getContainerSize());
        for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
            before.add(hopper.getItem(slot).copy());
        }
        TOBACCONIST$HOPPER_SNAPSHOTS.get().push(new tobacconist$HopperSnapshot(hopper, before));
    }

    @Inject(method = "insertHook", at = @At("RETURN"))
    private static void tobacconist$recordCapabilityOutput(HopperBlockEntity hopper,
                                                           CallbackInfoReturnable<Boolean> cir) {
        Deque<tobacconist$HopperSnapshot> snapshots = TOBACCONIST$HOPPER_SNAPSHOTS.get();
        if (snapshots.isEmpty()) return;
        tobacconist$HopperSnapshot snapshot = snapshots.pop();
        if (snapshots.isEmpty()) TOBACCONIST$HOPPER_SNAPSHOTS.remove();

        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;

        HopperBlockEntity capturedHopper = snapshot.hopper();
        Level level = capturedHopper.getLevel();
        if (level == null || level.isClientSide) return;

        int slots = Math.min(snapshot.before().size(), capturedHopper.getContainerSize());
        for (int slot = 0; slot < slots; slot++) {
            ItemStack before = snapshot.before().get(slot);
            if (before.isEmpty()) continue;

            ItemStack after = capturedHopper.getItem(slot);
            int moved;
            if (after.isEmpty()) {
                moved = before.getCount();
            } else if (ItemStack.isSameItemSameTags(before, after)) {
                moved = before.getCount() - after.getCount();
            } else {
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
