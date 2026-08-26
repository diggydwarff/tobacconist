package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.util.ProductionMonitorTransferHooks;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Observes successful Create Funnel transfers without changing Funnel behavior. */
@Mixin(value = FunnelBlockEntity.class, remap = false)
public abstract class FunnelProductionMonitorMixin {
    @Unique
    private boolean tobacconist$handlingDirectInput;
    @Unique
    private ItemStack tobacconist$directInputBefore = ItemStack.EMPTY;

    /**
     * Ordinary extraction paths call onTransfer with the exact extracted stack. Direct belt input
     * is handled separately below because Create calls onTransfer before it knows the remainder.
     */
    @Inject(method = "onTransfer", at = @At("TAIL"))
    private void tobacconist$recordFunnelTransfer(ItemStack stack, CallbackInfo ci) {
        if (tobacconist$handlingDirectInput) return;
        FunnelBlockEntity funnel = (FunnelBlockEntity) (Object) this;
        ProductionMonitorTransferHooks.notifySuccessfulTransfer(funnel.getLevel(), funnel.getBlockPos(), stack);
    }

    @Inject(
            method = "handleDirectBeltInput(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD")
    )
    private void tobacconist$captureDirectInput(TransportedItemStack transported, Direction side, boolean simulate,
                                                 CallbackInfoReturnable<ItemStack> cir) {
        tobacconist$handlingDirectInput = !simulate;
        tobacconist$directInputBefore = !simulate && transported != null && !transported.stack.isEmpty()
                ? transported.stack.copy()
                : ItemStack.EMPTY;
    }

    @Inject(
            method = "handleDirectBeltInput(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private void tobacconist$recordDirectInput(TransportedItemStack transported, Direction side, boolean simulate,
                                                CallbackInfoReturnable<ItemStack> cir) {
        ItemStack before = tobacconist$directInputBefore;
        tobacconist$handlingDirectInput = false;
        tobacconist$directInputBefore = ItemStack.EMPTY;
        if (simulate || before.isEmpty()) return;

        ItemStack remainder = cir.getReturnValue();
        int remainderCount = remainder == null || remainder.isEmpty() ? 0 : remainder.getCount();
        int moved = before.getCount() - remainderCount;
        if (moved <= 0) return;

        FunnelBlockEntity funnel = (FunnelBlockEntity) (Object) this;
        ProductionMonitorTransferHooks.notifySuccessfulTransfer(
                funnel.getLevel(), funnel.getBlockPos(), before.copyWithCount(moved));
    }
}
