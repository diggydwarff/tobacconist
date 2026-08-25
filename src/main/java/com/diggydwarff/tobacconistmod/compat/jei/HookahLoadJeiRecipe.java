package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record HookahLoadJeiRecipe(
        ItemStack hookah,
        ItemStack shisha,
        ItemStack water,
        ItemStack heat,
        ItemStack output,
        Component line1,
        Component line2
) {
    public static List<HookahLoadJeiRecipe> createAll() {
        ItemStack hookah = new ItemStack(ModBlocks.HOOKAH.get().asItem());
        ItemStack shisha = new ItemStack(ModItems.SHISHA_TOBACCO.get());
        ItemStack water = new ItemStack(Items.POTION);
        ItemStack heat = new ItemStack(Items.COAL);

        ItemStack loaded = new ItemStack(ModBlocks.HOOKAH.get().asItem());
        LegacyItemTags.getOrCreateTag(loaded).putBoolean("Loaded", true);

        return List.of(
                new HookahLoadJeiRecipe(
                        hookah,
                        shisha,
                        water,
                        heat,
                        loaded,
                        Component.translatable("tobacconistmod.jei.hookah_load.action"),
                        Component.translatable("tobacconistmod.jei.hookah_load.result")
                )
        );
    }
}