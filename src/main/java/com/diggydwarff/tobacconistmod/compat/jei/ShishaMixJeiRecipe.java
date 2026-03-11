package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ShishaMixJeiRecipe(
        List<ItemStack> tobaccos,
        List<ItemStack> flavorings,
        ItemStack output
) {
    public static List<ShishaMixJeiRecipe> createAll() {
        ItemStack output = new ItemStack(ModItems.SHISHA_TOBACCO.get());
        output.getOrCreateTag().putString("tobacco", "Virginia");
        output.getOrCreateTag().putString("flavor1", "Molasses");
        output.getOrCreateTag().putInt("Quality", 60);

        return List.of(new ShishaMixJeiRecipe(
                JeiItemLists.getAllLooseTobaccos(),
                JeiItemLists.getAllShishaFlavorings(),
                output
        ));
    }
}