package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.TobaccoCrateHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Nine units of the same tobacco item form a crate; their per-stack metadata may differ. */
public class TobaccoCrateRecipe extends CustomRecipe {
    public TobaccoCrateRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack first = ItemStack.EMPTY;
        int count = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (!TobaccoCrateHelper.isCrateableTobacco(stack)) return false;

            if (first.isEmpty()) {
                first = stack;
            } else if (!TobaccoCrateHelper.sameTobaccoType(first, stack)) {
                return false;
            }
            count++;
        }
        return count == TobaccoCrateHelper.CAPACITY;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<ItemStack> contents = new ArrayList<>(TobaccoCrateHelper.CAPACITY);
        ItemStack first = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (first.isEmpty()) first = stack;
            contents.add(stack.copyWithCount(1));
        }

        if (first.isEmpty() || contents.size() != TobaccoCrateHelper.CAPACITY) return ItemStack.EMPTY;
        return TobaccoCrateHelper.createCrate(first, contents, registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= TobaccoCrateHelper.CAPACITY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TOBACCO_CRATE_RECIPE_SERIALIZER.get();
    }
}
