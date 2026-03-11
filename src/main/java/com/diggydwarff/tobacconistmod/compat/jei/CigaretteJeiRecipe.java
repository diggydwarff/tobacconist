package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record CigaretteJeiRecipe(
        ItemStack tobacco,
        ItemStack paper,
        ItemStack output
) {
    public static List<CigaretteJeiRecipe> createAll() {
        List<CigaretteJeiRecipe> recipes = new ArrayList<>();

        add(recipes, ModItems.TOBACCO_LOOSE_WILD.get());
        add(recipes, ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        add(recipes, ModItems.TOBACCO_LOOSE_BURLEY.get());
        add(recipes, ModItems.TOBACCO_LOOSE_ORIENTAL.get());
        add(recipes, ModItems.TOBACCO_LOOSE_DOKHA.get());
        add(recipes, ModItems.TOBACCO_LOOSE_SHADE.get());

        return recipes;
    }

    private static void add(List<CigaretteJeiRecipe> recipes, Item looseItem) {
        ItemStack tobacco = new ItemStack(looseItem);

        tobacco.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        tobacco.getOrCreateTag().putString(
                TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(60)
        );
        TobaccoCuringHelper.setCutType(tobacco, TobaccoCuringHelper.CUT_RIBBON);

        ItemStack paper = new ItemStack(ModItems.ROLLING_PAPER.get());
        ItemStack output = new ItemStack(ModItems.CIGARETTE.get());

        recipes.add(new CigaretteJeiRecipe(tobacco, paper, output));
    }
}