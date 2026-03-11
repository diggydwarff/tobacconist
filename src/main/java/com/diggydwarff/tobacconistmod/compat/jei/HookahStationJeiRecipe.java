package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record HookahStationJeiRecipe(
        List<ItemStack> hookahs,
        List<ItemStack> fuels,
        ItemStack topSlot,
        ItemStack bottomSlot,
        List<ItemStack> outputs
) {
    public static List<HookahStationJeiRecipe> createAll() {
        return List.of(
                new HookahStationJeiRecipe(
                        List.of(
                                new ItemStack(ModBlocks.HOOKAH.get().asItem()),
                                new ItemStack(ModBlocks.ORNATE_IRON_HOOKAH.get().asItem()),
                                new ItemStack(ModBlocks.ORNATE_GOLD_HOOKAH.get().asItem()),
                                new ItemStack(ModBlocks.ORNATE_COPPER_HOOKAH.get().asItem()),
                                new ItemStack(ModBlocks.ORNATE_AMETHYST_HOOKAH.get().asItem()),
                                new ItemStack(ModBlocks.ORNATE_DIAMOND_HOOKAH.get().asItem())
                        ),
                        List.of(
                                new ItemStack(Items.COAL),
                                new ItemStack(Items.CHARCOAL),
                                new ItemStack(ModItems.BAMBOO_CHARCOAL.get())
                        ),
                        new ItemStack(Items.POTION),
                        new ItemStack(ModItems.SHISHA_TOBACCO.get()),
                        makeLoadedOutputs()
                )
        );
    }

    private static List<ItemStack> makeLoadedOutputs() {
        return List.of(
                makeLoaded(ModBlocks.HOOKAH.get().asItem()),
                makeLoaded(ModBlocks.ORNATE_IRON_HOOKAH.get().asItem()),
                makeLoaded(ModBlocks.ORNATE_GOLD_HOOKAH.get().asItem()),
                makeLoaded(ModBlocks.ORNATE_COPPER_HOOKAH.get().asItem()),
                makeLoaded(ModBlocks.ORNATE_AMETHYST_HOOKAH.get().asItem()),
                makeLoaded(ModBlocks.ORNATE_DIAMOND_HOOKAH.get().asItem())
        );
    }

    private static ItemStack makeLoaded(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putBoolean("Loaded", true);
        return stack;
    }
}