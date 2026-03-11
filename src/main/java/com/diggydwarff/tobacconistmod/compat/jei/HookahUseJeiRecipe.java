package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record HookahUseJeiRecipe(
        ItemStack loadedHookah,
        String line1,
        String line2
) {
    public static List<HookahUseJeiRecipe> createAll() {
        ItemStack loaded = new ItemStack(ModBlocks.HOOKAH.get().asItem());
        loaded.getOrCreateTag().putBoolean("Loaded", true);

        return List.of(
                new HookahUseJeiRecipe(
                        loaded,
                        "Right-click loaded hookah to smoke",
                        "Consumes contents and applies effects"
                )
        );
    }
}