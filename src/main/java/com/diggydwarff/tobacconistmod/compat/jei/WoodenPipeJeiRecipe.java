package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.List;

public class WoodenPipeJeiRecipe {

    public static List<CraftingRecipe> createAll() {

        NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);

        ingredients.set(2, Ingredient.of(ItemTags.PLANKS));
        ingredients.set(4, Ingredient.of(ItemTags.PLANKS));
        ingredients.set(5, Ingredient.of(Items.STICK));
        ingredients.set(6, Ingredient.of(Items.STICK));

        ShapedRecipe recipe = new ShapedRecipe(
                new ResourceLocation(TobacconistMod.MODID, "jei_wooden_pipe"),
                "",
                CraftingBookCategory.MISC,
                3,
                3,
                ingredients,
                new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get())
        );

        return List.of(recipe);
    }
}