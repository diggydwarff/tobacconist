package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.util.ProductionMonitorTransferHooks;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Counts a stack when it actually enters an empty Chute, not when a remainder is updated. */
@Mixin(value = ChuteBlockEntity.class, remap = false)
public abstract class ChuteProductionMonitorMixin {
    @Shadow private ItemStack item;

    @Inject(method = "setItem(Lnet/minecraft/world/item/ItemStack;F)V", at = @At("HEAD"))
    private void tobacconist$recordChuteEntry(ItemStack stack, float insertionPos, CallbackInfo ci) {
        ChuteBlockEntity chute = (ChuteBlockEntity) (Object) this;
        if (item.isEmpty() && !stack.isEmpty()) {
            ProductionMonitorTransferHooks.notifySuccessfulTransfer(chute.getLevel(), chute.getBlockPos(), stack);
        }
    }
}
