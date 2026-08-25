package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.compat.create.CreateTobaccoHomogenization;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinInventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Homogenizer-specific Basin inventory rules. */
@Mixin(value = BasinInventory.class, remap = false)
public abstract class BasinInventoryHomogenizationMixin {
    @Shadow
    private BasinBlockEntity blockEntity;

    /**
     * Create normally rejects a second stack with identical components anywhere else in a Basin.
     * Homogenizers need repeated same-quality leaf stacks to fill additional slots while waiting
     * for quality variation, so bypass that uniqueness check only for tobacco in a Mixer Basin.
     */
    @Redirect(
            method = "insertItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean tobacconist$allowDuplicateHomogenizerStacks(ItemStack incoming, ItemStack existing) {
        BasinInventory self = (BasinInventory) (Object) this;
        if (self == blockEntity.getInputInventory()
                && CreateTobaccoHomogenization.shouldAllowDuplicateInputStacks(blockEntity, incoming, existing)) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(incoming, existing);
    }

    /** Prevent generic output automation from stealing unprocessed homogenizer input. */
    @Inject(method = "extractItem", at = @At("HEAD"), cancellable = true)
    private void tobacconist$protectHomogenizerInput(int slot, int amount, boolean simulate,
                                                     CallbackInfoReturnable<ItemStack> cir) {
        BasinInventory self = (BasinInventory) (Object) this;
        if (self != blockEntity.getInputInventory()) return;

        if (CreateTobaccoHomogenization.shouldBlockExternalInputExtraction(blockEntity, slot)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
