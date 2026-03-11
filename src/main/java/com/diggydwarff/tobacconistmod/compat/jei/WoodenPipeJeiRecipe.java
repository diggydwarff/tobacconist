package com.diggydwarff.tobacconistmod.compat.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record WoodenPipeJeiRecipe(
        ItemStack plank,
        ItemStack stick,
        ItemStack output
) {
    public static List<WoodenPipeJeiRecipe> createAll() {
        return List.of(
                new WoodenPipeJeiRecipe(
                        new ItemStack(Items.OAK_PLANKS),
                        new ItemStack(Items.STICK),
                        new ItemStack(com.diggydwarff.tobacconistmod.datagen.items.ModItems.WOODEN_SMOKING_PIPE.get())
                )
        );
    }
}