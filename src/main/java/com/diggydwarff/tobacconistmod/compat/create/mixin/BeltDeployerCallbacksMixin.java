package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.compat.create.CreateDeployerCompat;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Release Tobacconist belt products when their consumable Deployer input runs out. */
@Mixin(value = BeltDeployerCallbacks.class, remap = false)
public abstract class BeltDeployerCallbacksMixin {

    @Inject(method = "whenItemHeld", at = @At("HEAD"), cancellable = true)
    private static void tobacconist$releaseAfterLastConsumable(
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            CallbackInfoReturnable<ProcessingResult> cir) {

        if (deployer.getPlayer() == null || !deployer.getPlayer().getMainHandItem().isEmpty()) {
            return;
        }

        if (transported.stack.is(ModItems.INCOMPLETE_CIGARETTE.get())
                || transported.stack.is(ModItems.INCOMPLETE_CIGAR.get())
                || transported.stack.is(ModItems.CIGARETTE.get())
                || transported.stack.is(ModItems.CIGAR.get())
                || transported.stack.is(ModItems.TOBACCO_BOX.get())) {
            cir.setReturnValue(ProcessingResult.PASS);
        }
    }
    /**
     * Bulk Tobacco Box packing uses a dynamic ItemApplicationRecipe with keepHeldItem=true so
     * Create does not perform its hard-coded one-item shrink. Consume the exact number that was
     * packed after the belt application completes.
     */
    @Inject(method = "activate", at = @At("TAIL"))
    private static void tobacconist$consumeBulkPackedItems(
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            Recipe<?> recipe,
            CallbackInfo ci) {

        int requested = CreateDeployerCompat.getBulkPackingConsumption(recipe);
        if (requested <= 0 || deployer.getPlayer() == null) return;

        ItemStack held = deployer.getPlayer().getMainHandItem();
        if (held.isEmpty()) return;

        held.shrink(Math.min(requested, held.getCount()));
        deployer.notifyUpdate();
    }

}
