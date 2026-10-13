package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/** Refines the liquid in its existing bottle, without adding water or replacing its label. */
public record VaporBrewingRecipe(Fluid source, Item ingredient, Fluid target) implements IBrewingRecipe {
    public static void register(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addRecipe(new VaporBrewingRecipe(VaporContent.CRUDE_FLUID.get(), Items.PAPER, VaporContent.NICOTINE_FLUID.get()));
        event.getBuilder().addRecipe(new VaporBrewingRecipe(com.diggydwarff.tobacconistmod.fluid.ModExtractionFluids.aquaVitae(), Items.SUGAR_CANE, VaporContent.CLEAR_FLUID.get()));
    }
    @Override public boolean isInput(ItemStack input) {
        FluidStack fluid = CauldronExtraction.heldFluid(input);
        return !fluid.isEmpty() && fluid.getFluid() == source;
    }
    @Override public boolean isIngredient(ItemStack stack) { return stack.is(ingredient); }
    @Override public ItemStack getOutput(ItemStack input, ItemStack reagent) {
        if (!isInput(input) || !isIngredient(reagent)) return ItemStack.EMPTY;
        FluidStack refined = new FluidStack(target, CauldronExtraction.heldFluid(input).getAmount());
        if (input.getItem() instanceof SizedBottleItem) {
            ItemStack result = input.copyWithCount(1);
            result.set(VaporContent.BOTTLED_FLUID.get(), BottledFluid.of(refined, refined.getAmount()));
            return result;
        }
        ItemStack result = VaporBottleHandler.filled(refined);
        result.applyComponents(input.getComponentsPatch());
        result.set(VaporContent.VOLUME.get(), refined.getAmount());
        return result;
    }
}
