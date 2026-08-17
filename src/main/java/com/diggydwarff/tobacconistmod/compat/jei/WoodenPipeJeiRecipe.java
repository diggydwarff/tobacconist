package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.List;
import java.util.Map;

public class WoodenPipeJeiRecipe {

    public static List<RecipeHolder<CraftingRecipe>> createAll() {
        ShapedRecipePattern pattern = ShapedRecipePattern.of(
                Map.of(
                        'P', Ingredient.of(ItemTags.PLANKS),
                        'S', Ingredient.of(Items.STICK)
                ),
                "  P",
                " PS",
                "S  "
        );

        ShapedRecipe recipe = new ShapedRecipe(
                "",
                CraftingBookCategory.MISC,
                pattern,
                new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get())
        );

        return List.of(new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "jei/wooden_smoking_pipe"),
                recipe
        ));
    }
}
