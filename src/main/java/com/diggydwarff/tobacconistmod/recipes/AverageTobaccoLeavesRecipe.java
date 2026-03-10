package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AverageTobaccoLeavesRecipe extends CustomRecipe {

    public AverageTobaccoLeavesRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack first = ItemStack.EMPTY;
        boolean foundAny = false;
        boolean dry = false;
        String cureType = "";

        int nonEmptyStacks = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            if (!isTobaccoLeaf(stack)) {
                return false;
            }

            nonEmptyStacks++;

            if (!foundAny) {
                first = stack;
                foundAny = true;
                dry = isDryLeaf(stack);

                if (dry) {
                    cureType = TobaccoCuringHelper.getCureType(stack);
                    if (cureType.isEmpty()) return false;
                }
            } else {
                if (!ItemStack.isSameItem(first, stack)) {
                    return false;
                }

                if (isDryLeaf(stack) != dry) {
                    return false;
                }

                if (dry) {
                    String otherCure = TobaccoCuringHelper.getCureType(stack);
                    if (!cureType.equals(otherCure)) {
                        return false;
                    }
                }
            }
        }

        return foundAny && nonEmptyStacks == 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                // remove the entire stack
                remaining.set(i, ItemStack.EMPTY);
            }
        }

        return remaining;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack first = ItemStack.EMPTY;
        boolean dry = false;
        String cureType = "";

        int leavesUsed = 0;
        int totalQuality = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            if (first.isEmpty()) {
                first = stack.copy();
                dry = isDryLeaf(stack);
                if (dry) {
                    cureType = TobaccoCuringHelper.getCureType(stack);
                }
            }

            int quality = 0;

            if (dry) {
                if (stack.hasTag()) {
                    quality = stack.getTag().getInt(TobaccoCuringHelper.TAG_QUALITY);
                }
            } else {
                if (stack.hasTag()) {
                    quality = stack.getTag().getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
                }
            }

            totalQuality += quality;
            leavesUsed++;
        }

        if (first.isEmpty() || leavesUsed < 2) {
            return ItemStack.EMPTY;
        }

        int avgQuality = Math.round((float) totalQuality / leavesUsed);

        ItemStack result = new ItemStack(first.getItem(), leavesUsed);

        if (dry) {
            TobaccoCuringHelper.applyCureData(result, cureType, avgQuality);
        } else {
            TobaccoGrowthHelper.applyGrowthQuality(result, avgQuality);
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.AVERAGE_TOBACCO_LEAVES_SERIALIZER.get();
    }

    private boolean isTobaccoLeaf(ItemStack stack) {
        String path = stack.getItem().builtInRegistryHolder().key().location().getPath();
        return path.startsWith("tobacco_leaf_");
    }

    private boolean isDryLeaf(ItemStack stack) {
        String path = stack.getItem().builtInRegistryHolder().key().location().getPath();
        return path.endsWith("_dry");
    }
}