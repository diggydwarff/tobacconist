package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record LeafCuttingJeiRecipe(
        ItemStack inputLeaf,
        ItemStack output,
        String cutType,
        int leafX,
        int leafY,
        int chavetaX,
        int chavetaY
) {
    public static List<LeafCuttingJeiRecipe> createAll() {
        List<LeafCuttingJeiRecipe> recipes = new ArrayList<>();

        addAllCuts(recipes, ModItems.WILD_TOBACCO_LEAF_DRY.get(), ModItems.TOBACCO_LOOSE_WILD.get());
        addAllCuts(recipes, ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        addAllCuts(recipes, ModItems.BURLEY_TOBACCO_LEAF_DRY.get(), ModItems.TOBACCO_LOOSE_BURLEY.get());
        addAllCuts(recipes, ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get(), ModItems.TOBACCO_LOOSE_ORIENTAL.get());
        addAllCuts(recipes, ModItems.DOKHA_TOBACCO_LEAF_DRY.get(), ModItems.TOBACCO_LOOSE_DOKHA.get());
        addAllCuts(recipes, ModItems.SHADE_TOBACCO_LEAF_DRY.get(), ModItems.TOBACCO_LOOSE_SHADE.get());

        return recipes;
    }

    private static void addAllCuts(List<LeafCuttingJeiRecipe> recipes, Item dryLeaf, Item looseTobacco) {
        recipes.add(create(dryLeaf, looseTobacco, TobaccoCuringHelper.CUT_RIBBON, 1, 1, 0, 1));
        recipes.add(create(dryLeaf, looseTobacco, TobaccoCuringHelper.CUT_ROUGH, 1, 1, 2, 1));
        recipes.add(create(dryLeaf, looseTobacco, TobaccoCuringHelper.CUT_SHAG, 1, 1, 1, 0));
        recipes.add(create(dryLeaf, looseTobacco, TobaccoCuringHelper.CUT_FLAKE, 1, 1, 1, 2));
    }

    private static LeafCuttingJeiRecipe create(Item dryLeaf, Item looseTobacco, String cutType,
                                               int leafX, int leafY, int chavetaX, int chavetaY) {
        ItemStack leaf = new ItemStack(dryLeaf);
        TobaccoCuringHelper.applyCreativeLeafDefaults(leaf, true);

        ItemStack output = new ItemStack(looseTobacco, 3);
        TobaccoCuringHelper.copyTobaccoProcessingData(leaf, output);
        TobaccoCuringHelper.setCutType(output, cutType);

        return new LeafCuttingJeiRecipe(leaf, output, cutType, leafX, leafY, chavetaX, chavetaY);
    }
}