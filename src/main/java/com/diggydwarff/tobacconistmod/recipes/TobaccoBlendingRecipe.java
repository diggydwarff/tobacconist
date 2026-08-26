package com.diggydwarff.tobacconistmod.recipes;

import net.minecraft.resources.ResourceLocation;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Crafting-grid fallback for Create-independent tobacco blending. */
public class TobaccoBlendingRecipe extends CustomRecipe {
    public TobaccoBlendingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        return TobaccoBlendHelper.canBlend(collect(input));
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        return TobaccoBlendHelper.blend(collect(input));
    }

    private List<ItemStack> collect(CraftingContainer input) {
        List<ItemStack> tobaccos = new ArrayList<>();
        for (int i = 0; i < input.getContainerSize(); i++) {
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
