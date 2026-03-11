package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.world.item.ItemStack;

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

        List<ItemStack> pipes = JeiItemLists.getAllSmokingPipes();

        return List.of(new WoodenPipeFillJeiRecipe(tobaccos, pipes, pipes));
    }
}