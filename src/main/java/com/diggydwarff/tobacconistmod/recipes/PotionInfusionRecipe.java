package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.PotionInfusion;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** One ordinary 250 mB potion bottle treats five identical tobacco items, one in each slot. */
public final class PotionInfusionRecipe extends CustomRecipe {
    public PotionInfusionRecipe(CraftingBookCategory category) { super(category); }
    private ItemStack result(CraftingInput input) {
        ItemStack potion = ItemStack.EMPTY, tobacco = ItemStack.EMPTY;
        int count = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.POTION)) {
                if (!potion.isEmpty()) return ItemStack.EMPTY;
                potion = stack;
            } else {
                if (!TobaccoCuringHelper.isLooseTobacco(stack) || !PotionInfusion.canTreat(stack)) return ItemStack.EMPTY;
                if (!tobacco.isEmpty() && !ItemStack.isSameItemSameComponents(tobacco, stack)) return ItemStack.EMPTY;
                tobacco = stack;
                count++;
            }
        }
        if (potion.isEmpty() || count != PotionInfusion.CRAFT_BATCH) return ItemStack.EMPTY;
        ItemStack result = PotionInfusion.treat(tobacco, potion.get(DataComponents.POTION_CONTENTS));
        if (!result.isEmpty()) result.setCount(PotionInfusion.CRAFT_BATCH);
        return result;
    }
    @Override public boolean matches(CraftingInput input, Level level) { return !result(input).isEmpty(); }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return result(input); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 6; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.POTION_INFUSION.get(); }
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        var remains = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        if (result(input).isEmpty()) return remains;
        for (int i = 0; i < input.size(); i++)
            if (input.getItem(i).is(Items.POTION)) remains.set(i, new ItemStack(Items.GLASS_BOTTLE));
        return remains;
    }
}
