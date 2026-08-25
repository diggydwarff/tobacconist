package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record HookahUseJeiRecipe(
        ItemStack loadedHookah,
        Component line1,
        Component line2
) {
    public static List<HookahUseJeiRecipe> createAll() {
        ItemStack loaded = new ItemStack(ModBlocks.HOOKAH.get().asItem());
        LegacyItemTags.getOrCreateTag(loaded).putBoolean("Loaded", true);

        return List.of(
                new HookahUseJeiRecipe(
                        loaded,
                        Component.translatable("tobacconistmod.jei.hookah_use.action"),
                        Component.translatable("tobacconistmod.jei.hookah_use.result")
                )
        );
    }
}