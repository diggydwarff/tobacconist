package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record DryingRackJeiRecipe(
        ItemStack input,
        ItemStack output,
        String line1,
        String line2
) {
    public static List<DryingRackJeiRecipe> createAll() {
        List<DryingRackJeiRecipe> recipes = new ArrayList<>();

        List<LeafPair> leafPairs = List.of(
                new LeafPair(ModItems.WILD_TOBACCO_LEAF.get(), ModItems.WILD_TOBACCO_LEAF_DRY.get()),
                new LeafPair(ModItems.VIRGINIA_TOBACCO_LEAF.get(), ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()),
                new LeafPair(ModItems.BURLEY_TOBACCO_LEAF.get(), ModItems.BURLEY_TOBACCO_LEAF_DRY.get()),
                new LeafPair(ModItems.ORIENTAL_TOBACCO_LEAF.get(), ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get()),
                new LeafPair(ModItems.DOKHA_TOBACCO_LEAF.get(), ModItems.DOKHA_TOBACCO_LEAF_DRY.get()),
                new LeafPair(ModItems.SHADE_TOBACCO_LEAF.get(), ModItems.SHADE_TOBACCO_LEAF_DRY.get())
        );

        for (LeafPair pair : leafPairs) {
            ItemStack rawLeaf = makeRawLeaf(pair.rawLeaf());

            recipes.add(new DryingRackJeiRecipe(
                    rawLeaf,
                    makeDryLeaf(pair.dryLeaf(), TobaccoCuringHelper.CURE_AIR, 60),
                    "Shade or indirect light",
                    "Slow air curing"
            ));

            recipes.add(new DryingRackJeiRecipe(
                    rawLeaf.copy(),
                    makeDryLeaf(pair.dryLeaf(), TobaccoCuringHelper.CURE_SUN, 60),
                    "Direct sunlight",
                    "Faster sun curing"
            ));

            recipes.add(new DryingRackJeiRecipe(
                    rawLeaf.copy(),
                    makeDryLeaf(pair.dryLeaf(), TobaccoCuringHelper.CURE_FLUE, 60),
                    "Hot dry enclosed heat",
                    "Flue curing"
            ));

            recipes.add(new DryingRackJeiRecipe(
                    rawLeaf.copy(),
                    makeDryLeaf(pair.dryLeaf(), TobaccoCuringHelper.CURE_FIRE, 60),
                    "Smoke and low heat",
                    "Fire curing"
            ));
        }

        return recipes;
    }

    private static ItemStack makeRawLeaf(Item rawLeafItem) {
        ItemStack stack = new ItemStack(rawLeafItem);
        stack.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        stack.getOrCreateTag().putString(
                TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(60)
        );
        return stack;
    }

    private static ItemStack makeDryLeaf(Item dryLeafItem, String cureType, int quality) {
        ItemStack stack = new ItemStack(dryLeafItem);
        TobaccoCuringHelper.applyCureData(stack, cureType, quality);
        return stack;
    }

    private record LeafPair(Item rawLeaf, Item dryLeaf) {}
}