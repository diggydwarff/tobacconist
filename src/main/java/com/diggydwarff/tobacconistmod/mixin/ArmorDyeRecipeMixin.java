package com.diggydwarff.tobacconistmod.mixin;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.vapor.VapePenItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import com.diggydwarff.tobacconistmod.backport.DataComponents;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses vanilla dye blending for pouches and vapes while excluding their existing color
 * from a new crafting operation. Multiple dyes in the same craft are blended normally.
 */
@Mixin(ArmorDyeRecipe.class)
public abstract class ArmorDyeRecipeMixin {
    @Inject(
            method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tobacconist$recolorFresh(
            CraftingContainer input,
            RegistryAccess registries,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack target = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();

        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.TOBACCO_POUCH.get()) || VapePenItem.isDevice(stack)) {
                if (!target.isEmpty()) return;
                target = stack;
            } else if (stack.getItem() instanceof DyeItem dye) {
                dyes.add(dye);
            } else {
                return;
            }
        }

        if (target.isEmpty() || dyes.isEmpty()) return;

        ItemStack result = target.copy();
        result.setCount(1);
        ((DyeableLeatherItem) result.getItem()).clearColor(result);
        cir.setReturnValue(DyeableLeatherItem.dyeArmor(result, dyes));
    }
}
