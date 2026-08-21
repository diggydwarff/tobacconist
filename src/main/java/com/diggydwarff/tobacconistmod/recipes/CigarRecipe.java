package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobaccoItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.TobaccoLeafItem;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoDataHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CigarRecipe extends CustomRecipe {
    public CigarRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingContainer, Level level) {
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack tobaccoLeafStack = ItemStack.EMPTY;

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem) {
                if (!tobaccoStack.isEmpty()) return false;
                tobaccoStack = itemstack;
            } else if (itemstack.getItem() instanceof TobaccoLeafItem) {
                if (!tobaccoLeafStack.isEmpty()) return false;
                tobaccoLeafStack = itemstack;
            } else {
                return false;
            }
        }

        return !tobaccoStack.isEmpty() && !tobaccoLeafStack.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput craftingContainer, HolderLookup.Provider registries) {
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack tobaccoLeafStack = ItemStack.EMPTY;

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem) {
                tobaccoStack = itemstack;
            } else if (itemstack.getItem() instanceof TobaccoLeafItem) {
                tobaccoLeafStack = itemstack;
            }
        }

        if (tobaccoStack.isEmpty() || tobaccoLeafStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack returnStack = new ItemStack(ModItems.CIGAR.get(), 1);
        CompoundTag tag = LegacyItemTags.getOrCreateTag(returnStack);

        CompoundTag wrapperData = LegacyItemTags.getTag(tobaccoLeafStack);
        if (wrapperData != null) {
            tag.put("WrapperLeafData", wrapperData.copy());
        }

        tag.putString("tobacco", TobaccoProductQualityHelper.getShortTobaccoLabel(tobaccoStack));
        tag.putString("wrapper", tobaccoLeafStack.getDisplayName().getString());

        TobaccoDataHelper.applyTobaccoMetadata(returnStack, tobaccoStack);

        CompoundTag fillerTag = LegacyItemTags.getTag(tobaccoStack);
        CompoundTag wrapperTag = LegacyItemTags.getTag(tobaccoLeafStack);

        int fillerAge = fillerTag != null ? fillerTag.getInt("AgedDays") : 0;
        int wrapperAge = wrapperTag != null ? wrapperTag.getInt("AgedDays") : 0;
        int finalAge = Math.max(fillerAge, wrapperAge);

        if (finalAge > 0) {
            tag.putInt("AgedDays", finalAge);
        }

        boolean fermented =
                (fillerTag != null && fillerTag.getBoolean("Fermented")) ||
                        (wrapperTag != null && wrapperTag.getBoolean("Fermented"));

        if (fermented) {
            tag.putBoolean("Fermented", true);
        }

        boolean ruined =
                (fillerTag != null && fillerTag.getBoolean("Ruined")) ||
                        (wrapperTag != null && wrapperTag.getBoolean("Ruined"));

        if (ruined) {
            tag.putBoolean("Ruined", true);
        }

        TobaccoProductQualityHelper.applyProductQualityToTag(
                tag,
                tobaccoStack,
                TobaccoProductQualityHelper.getCigarQuality(tobaccoStack)
        );

        return returnStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CIGAR_RECIPE_SERIALIZER.get();
    }




}