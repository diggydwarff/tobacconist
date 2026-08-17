package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobaccoItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.ShishaFlavoringItem;
import com.diggydwarff.tobacconistmod.fluid.MolassesBottleFluidHandler;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ShishaTobaccoRecipe extends CustomRecipe {
    public ShishaTobaccoRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingContainer, Level level) {
        ItemStack sourceStack = ItemStack.EMPTY;
        List<String> addedFlavors = new ArrayList<>(3);

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem
                    || TobaccoProcessingHelper.isShisha(itemstack)) {
                if (!sourceStack.isEmpty()) return false;
                sourceStack = itemstack;
            } else if (itemstack.getItem() instanceof ShishaFlavoringItem) {
                if (!MolassesBottleFluidHandler.hasDose(itemstack)) return false;
                addedFlavors.add(itemstack.getDisplayName().getString());
            } else {
                return false;
            }
        }

        if (sourceStack.isEmpty() || addedFlavors.isEmpty()) {
            return false;
        }

        if (sourceStack.getItem() instanceof LooseTobaccoItem) {
            return addedFlavors.size() <= 3;
        }

        if (!TobaccoProcessingHelper.canAddShishaFlavor(sourceStack)
                || TobaccoProcessingHelper.getShishaFlavorCount(sourceStack) + addedFlavors.size() > 3) {
            return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingContainer, HolderLookup.Provider registries) {
        ItemStack sourceStack = ItemStack.EMPTY;
        List<String> flavorNames = new ArrayList<>(3);

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem
                    || TobaccoProcessingHelper.isShisha(itemstack)) {
                sourceStack = itemstack;
            } else if (itemstack.getItem() instanceof ShishaFlavoringItem
                    && MolassesBottleFluidHandler.hasDose(itemstack)) {
                flavorNames.add(itemstack.getDisplayName().getString());
            }
        }

        if (sourceStack.getItem() instanceof LooseTobaccoItem) {
            return TobaccoProcessingHelper.createShisha(sourceStack, flavorNames);
        }
        return TobaccoProcessingHelper.addShishaFlavors(sourceStack, flavorNames);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingContainer) {
        NonNullList<ItemStack> remains = NonNullList.withSize(craftingContainer.size(), ItemStack.EMPTY);

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack stack = craftingContainer.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof ShishaFlavoringItem) {
                ItemStack bottle = stack.copy();
                bottle.setCount(1);

                if (bottle.isDamageableItem() && MolassesBottleFluidHandler.hasDose(bottle)) {
                    int nextDamage = Math.min(bottle.getMaxDamage(), bottle.getDamageValue() + 1);
                    if (nextDamage >= bottle.getMaxDamage()) {
                        remains.set(i, new ItemStack(Items.GLASS_BOTTLE));
                    } else {
                        bottle.setDamageValue(nextDamage);
                        remains.set(i, bottle);
                    }
                }
            }
        }

        return remains;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SHISHA_TOBACCO_RECIPE_SERIALIZER.get();
    }




}