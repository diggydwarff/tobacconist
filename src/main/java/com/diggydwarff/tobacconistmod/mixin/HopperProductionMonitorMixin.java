package com.diggydwarff.tobacconistmod.mixin;

import com.diggydwarff.tobacconistmod.util.ProductionMonitorTransferHooks;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks hopper transfers into vanilla Container destinations through HopperBlockEntity#addItem.
 *
 * Forge patches HopperBlockEntity#ejectItems with a different descriptor that has no matching
 * vanilla obfuscation entry. Hooking addItem instead keeps this mixin normally remappable and
 * still observes the actual amount accepted by vanilla-container destinations.
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperProductionMonitorMixin {
    @Unique
    private static final ThreadLocal<Deque<tobacconist$TransferSnapshot>> TOBACCONIST$TRANSFERS =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD")
    )
    private static void tobacconist$captureHopperOutput(Container source, Container destination, ItemStack stack,
                                                        Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        if (source instanceof HopperBlockEntity hopper) {
            TOBACCONIST$TRANSFERS.get().push(new tobacconist$TransferSnapshot(hopper, stack.copy()));
        }
    }

    @Inject(
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private static void tobacconist$recordHopperOutput(Container source, Container destination, ItemStack stack,
                                                       Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        if (!(source instanceof HopperBlockEntity)) return;

        Deque<tobacconist$TransferSnapshot> transfers = TOBACCONIST$TRANSFERS.get();
        if (transfers.isEmpty()) return;
        tobacconist$TransferSnapshot snapshot = transfers.pop();
        if (transfers.isEmpty()) TOBACCONIST$TRANSFERS.remove();

        ItemStack before = snapshot.before();
        ItemStack remainder = cir.getReturnValue();
        int remaining = remainder == null || remainder.isEmpty() ? 0 : remainder.getCount();
        int moved = before.getCount() - remaining;
        if (moved <= 0) return;

        HopperBlockEntity hopper = snapshot.hopper();
        Level level = hopper.getLevel();
        if (level == null || level.isClientSide) return;

        ProductionMonitorTransferHooks.notifySuccessfulTransfer(
                level, hopper.getBlockPos(), before.copyWithCount(moved));
    }

    @Unique
    private record tobacconist$TransferSnapshot(HopperBlockEntity hopper, ItemStack before) {}
}
