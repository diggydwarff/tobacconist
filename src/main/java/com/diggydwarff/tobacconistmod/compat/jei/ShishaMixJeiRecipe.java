package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

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
        LegacyItemTags.getOrCreateTag(output).putString("tobacco", "Virginia");
        LegacyItemTags.getOrCreateTag(output).putString("flavor1", "Molasses");
        LegacyItemTags.getOrCreateTag(output).putInt("Quality", 60);

        java.util.ArrayList<ItemStack> sources = new java.util.ArrayList<>(JeiItemLists.getAllLooseTobaccos());
        ItemStack remixable = output.copy();
        sources.add(remixable);

        return List.of(new ShishaMixJeiRecipe(
                List.copyOf(sources),
                JeiItemLists.getAllShishaFlavorings(),
                output
        ));
    }
}