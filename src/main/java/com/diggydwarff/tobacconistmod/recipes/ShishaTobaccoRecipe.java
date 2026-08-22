package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobaccoItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.ShishaFlavoringItem;
import com.diggydwarff.tobacconistmod.fluid.MolassesBottleFluidHandler;
import com.diggydwarff.tobacconistmod.util.TobaccoAromaticHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared molasses crafting path.
 * Rough loose tobacco + 1-3 molasses doses makes Shisha; any other loose cut + exactly one dose
 * becomes single-flavor aromatic tobacco. Existing unused Shisha may still receive up to 3 flavors.
 */
public class ShishaTobaccoRecipe extends CustomRecipe {
    public ShishaTobaccoRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingContainer, Level level) {
        ItemStack sourceStack = ItemStack.EMPTY;
        List<BottledMolassesFlavors> flavors = new ArrayList<>(3);

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem
                    || TobaccoProcessingHelper.isShisha(itemstack)) {
                if (!sourceStack.isEmpty()) return false;
                sourceStack = itemstack;
            } else if (itemstack.getItem() instanceof ShishaFlavoringItem) {
                if (!MolassesBottleFluidHandler.hasDose(itemstack)) return false;
                BottledMolassesFlavors flavor = BottledMolassesFlavors.fromItem(itemstack.getItem());
                if (flavor == null) return false;
                flavors.add(flavor);
            } else {
                return false;
            }
        }

        if (sourceStack.isEmpty() || flavors.isEmpty()) return false;

        if (sourceStack.getItem() instanceof LooseTobaccoItem) {
            if (TobaccoCuringHelper.CUT_ROUGH.equals(TobaccoCuringHelper.getCutType(sourceStack))) {
                return flavors.size() <= 3;
            }
            return flavors.size() == 1 && TobaccoAromaticHelper.canAromatize(sourceStack);
        }

        return TobaccoProcessingHelper.canAddShishaFlavor(sourceStack)
                && TobaccoProcessingHelper.getShishaFlavorCount(sourceStack) + flavors.size() <= 3;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingContainer, HolderLookup.Provider registries) {
        ItemStack sourceStack = ItemStack.EMPTY;
        List<BottledMolassesFlavors> flavors = new ArrayList<>(3);

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem
                    || TobaccoProcessingHelper.isShisha(itemstack)) {
                sourceStack = itemstack;
            } else if (itemstack.getItem() instanceof ShishaFlavoringItem
                    && MolassesBottleFluidHandler.hasDose(itemstack)) {
                BottledMolassesFlavors flavor = BottledMolassesFlavors.fromItem(itemstack.getItem());
                if (flavor != null) flavors.add(flavor);
            }
        }

        if (sourceStack.getItem() instanceof LooseTobaccoItem) {
            if (TobaccoCuringHelper.CUT_ROUGH.equals(TobaccoCuringHelper.getCutType(sourceStack))) {
                return TobaccoProcessingHelper.createShisha(
                        sourceStack,
                        flavors.stream().map(BottledMolassesFlavors::getShishaFlavorTag).toList()
                );
            }
            return flavors.size() == 1
                    ? TobaccoAromaticHelper.aromatize(sourceStack, flavors.getFirst())
                    : ItemStack.EMPTY;
        }

        return TobaccoProcessingHelper.addShishaFlavors(
                sourceStack,
                flavors.stream().map(BottledMolassesFlavors::getShishaFlavorTag).toList()
        );
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
            if (stack.isEmpty()) continue;

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
