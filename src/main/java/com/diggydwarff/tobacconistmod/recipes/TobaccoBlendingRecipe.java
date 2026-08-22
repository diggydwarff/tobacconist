package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Crafting-grid fallback for Create-independent tobacco blending. */
public class TobaccoBlendingRecipe extends CustomRecipe {
    public TobaccoBlendingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return TobaccoBlendHelper.canBlend(collect(input));
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return TobaccoBlendHelper.blend(collect(input));
    }

    private List<ItemStack> collect(CraftingInput input) {
        List<ItemStack> tobaccos = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (!TobaccoBlendHelper.isBlendableBaseTobacco(stack)) return List.of();
            tobaccos.add(stack);
        }
        return tobaccos;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TOBACCO_BLENDING_RECIPE_SERIALIZER.get();
    }
}
