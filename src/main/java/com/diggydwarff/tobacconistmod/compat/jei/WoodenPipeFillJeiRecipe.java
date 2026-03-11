package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record WoodenPipeFillJeiRecipe(
        List<ItemStack> tobaccos,
        List<ItemStack> emptyPipes,
        List<ItemStack> filledPipes
) {
    public static List<WoodenPipeFillJeiRecipe> createAll() {
        List<ItemStack> tobaccos = List.of(
                new ItemStack(ModItems.TOBACCO_LOOSE_WILD.get()),
                new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get()),
                new ItemStack(ModItems.TOBACCO_LOOSE_BURLEY.get()),
                new ItemStack(ModItems.TOBACCO_LOOSE_ORIENTAL.get()),
                new ItemStack(ModItems.TOBACCO_LOOSE_DOKHA.get()),
                new ItemStack(ModItems.TOBACCO_LOOSE_SHADE.get())
        );

        List<ItemStack> pipes = new ArrayList<>();

        pipes.add(makeWoodPipe("minecraft:oak_planks"));
        pipes.add(makeWoodPipe("minecraft:spruce_planks"));
        pipes.add(makeWoodPipe("minecraft:birch_planks"));
        pipes.add(makeWoodPipe("minecraft:jungle_planks"));
        pipes.add(makeWoodPipe("minecraft:acacia_planks"));
        pipes.add(makeWoodPipe("minecraft:dark_oak_planks"));
        pipes.add(makeWoodPipe("minecraft:mangrove_planks"));
        pipes.add(makeWoodPipe("minecraft:cherry_planks"));
        pipes.add(makeWoodPipe("minecraft:bamboo_planks"));

        pipes.add(new ItemStack(ModItems.COPPER_SMOKING_PIPE.get()));
        pipes.add(new ItemStack(ModItems.IRON_SMOKING_PIPE.get()));
        pipes.add(new ItemStack(ModItems.GOLD_SMOKING_PIPE.get()));
        pipes.add(new ItemStack(ModItems.GEM_ENCRUSTED_SMOKING_PIPE.get()));
        pipes.add(new ItemStack(ModItems.DIAMOND_ENCRUSTED_SMOKING_PIPE.get()));
        pipes.add(new ItemStack(ModItems.LAPIS_ENCRUSTED_SMOKING_PIPE.get()));
        pipes.add(new ItemStack(ModItems.EMERALD_ENCRUSTED_SMOKING_PIPE.get()));
        pipes.add(new ItemStack(ModItems.EMERALD_AZTEC_SMOKING_PIPE.get()));
        pipes.add(new ItemStack(ModItems.NETHERITE_SMOKING_PIPE.get()));
        pipes.add(new ItemStack(ModItems.KISERU_SMOKING_PIPE.get()));

        return List.of(new WoodenPipeFillJeiRecipe(tobaccos, pipes, pipes));
    }

    private static ItemStack makeWoodPipe(String plankId) {
        ItemStack pipe = new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get());

        pipe.getOrCreateTag().putString(
                WoodenPipeRecipe.NBT_WOOD_PLANK,
                plankId
        );

        return pipe;
    }
}