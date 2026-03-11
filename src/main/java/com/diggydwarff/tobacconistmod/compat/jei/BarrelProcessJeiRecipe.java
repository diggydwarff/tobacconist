package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record BarrelProcessJeiRecipe(
        ItemStack input,
        ItemStack output,
        String line1,
        String line2
) {
    public static List<BarrelProcessJeiRecipe> createAll() {
        List<BarrelProcessJeiRecipe> recipes = new ArrayList<>();

        addLeafRecipes(recipes, ModItems.WILD_TOBACCO_LEAF_DRY.get());
        addLeafRecipes(recipes, ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        addLeafRecipes(recipes, ModItems.BURLEY_TOBACCO_LEAF_DRY.get());
        addLeafRecipes(recipes, ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get());
        addLeafRecipes(recipes, ModItems.DOKHA_TOBACCO_LEAF_DRY.get());
        addLeafRecipes(recipes, ModItems.SHADE_TOBACCO_LEAF_DRY.get());

        return recipes;
    }

    private static void addLeafRecipes(List<BarrelProcessJeiRecipe> recipes, Item dryLeafItem) {
        ItemStack dryLeaf = makeDryLeaf(dryLeafItem);
        ItemStack fermentedLeaf = makeFermentedLeaf(dryLeafItem);
        ItemStack agedLeaf = makeAgedLeaf(dryLeafItem, 30);

        recipes.add(new BarrelProcessJeiRecipe(
                dryLeaf,
                fermentedLeaf,
                "Warmth 3+ / Barrel humidity 25+",
                "Ferments after 2 in-game days"
        ));

        recipes.add(new BarrelProcessJeiRecipe(
                fermentedLeaf.copy(),
                agedLeaf,
                "Warmth 0 or lower / Humidity 1-3",
                "Ages in cool dark storage"
        ));
    }

    private static ItemStack makeDryLeaf(Item item) {
        ItemStack stack = new ItemStack(item);
        TobaccoCuringHelper.applyCureData(stack, TobaccoCuringHelper.CURE_AIR, 60);
        return stack;
    }

    private static ItemStack makeFermentedLeaf(Item item) {
        ItemStack stack = makeDryLeaf(item);
        stack.getOrCreateTag().putBoolean(TobaccoBarrelBlockEntity.TAG_FERMENTED, true);

        int newQ = 67;
        stack.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, newQ);
        stack.getOrCreateTag().putString(
                TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(newQ)
        );

        return stack;
    }

    private static ItemStack makeAgedLeaf(Item item, int agedDays) {
        ItemStack stack = makeFermentedLeaf(item);
        stack.getOrCreateTag().putInt(TobaccoBarrelBlockEntity.TAG_AGED_DAYS, agedDays);

        int newQ = 72;
        stack.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, newQ);
        stack.getOrCreateTag().putString(
                TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(newQ)
        );

        return stack;
    }
}