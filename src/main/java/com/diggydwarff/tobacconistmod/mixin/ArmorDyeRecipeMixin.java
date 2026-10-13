package com.diggydwarff.tobacconistmod.mixin;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.vapor.VapePenItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
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
            method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tobacconist$recolorFresh(
            CraftingInput input,
            HolderLookup.Provider registries,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack target = ItemStack.EMPTY;
        List<DyeItem> dyes = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
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
        result.remove(DataComponents.DYED_COLOR);
        cir.setReturnValue(DyedItemColor.applyDyes(result, dyes));
    }
}
