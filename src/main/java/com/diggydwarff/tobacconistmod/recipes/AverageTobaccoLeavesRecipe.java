package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCrateHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AverageTobaccoLeavesRecipe extends CustomRecipe {

    public AverageTobaccoLeavesRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        // Reserve a full 3x3 same-item grid for the crate recipe.
        ItemStack crateFirst = ItemStack.EMPTY;
        int crateSlots = 0;
        boolean sameCrateType = true;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            crateSlots++;
            if (!TobaccoCrateHelper.isCrateableTobacco(stack)) {
                sameCrateType = false;
                break;
            }
            if (crateFirst.isEmpty()) crateFirst = stack;
            else if (!TobaccoCrateHelper.sameTobaccoType(crateFirst, stack)) sameCrateType = false;
        }
        if (crateSlots == TobaccoCrateHelper.CAPACITY && sameCrateType) return false;

        ItemStack first = ItemStack.EMPTY;
        boolean foundAny = false;
        int nonEmptyStacks = 0;

        Mode mode = null;
        String cureType = "";
        String cutType = "";

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            Mode thisMode = getMode(stack);
            if (thisMode == null) {
                return false;
            }

            nonEmptyStacks++;

            if (!foundAny) {
                first = stack;
                foundAny = true;
                mode = thisMode;

                if (mode != Mode.RAW_LEAF) {
                    cureType = TobaccoCuringHelper.getCureType(stack);
                    if (cureType.isEmpty()) return false;
                }

                if (mode == Mode.LOOSE) {
                    cutType = TobaccoCuringHelper.getCutType(stack);
                    if (cutType.isEmpty()) return false;
                }
            } else {
                if (!ItemStack.isSameItem(first, stack)) {
                    return false;
                }

                if (thisMode != mode) {
                    return false;
                }

                if (mode != Mode.RAW_LEAF) {
                    String otherCure = TobaccoCuringHelper.getCureType(stack);
                    if (!cureType.equals(otherCure)) {
                        return false;
                    }
                }

                if (mode == Mode.LOOSE) {
                    String otherCut = TobaccoCuringHelper.getCutType(stack);
                    if (!cutType.equals(otherCut)) {
                        return false;
                    }
                }
            }
        }

        return foundAny && nonEmptyStacks >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        return NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
        ItemStack first = ItemStack.EMPTY;
        Mode mode = null;
        String cureType = "";
        String cutType = "";

        int usedItems = 0;
        long totalQuality = 0L;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            if (first.isEmpty()) {
                first = stack.copy();
                mode = getMode(stack);

                if (mode != Mode.RAW_LEAF) {
                    cureType = TobaccoCuringHelper.getCureType(stack);
                }

                if (mode == Mode.LOOSE) {
                    cutType = TobaccoCuringHelper.getCutType(stack);
                }
            }

            int quality = getStackQuality(stack, mode);
            usedItems++;
            totalQuality += quality;
        }

        if (first.isEmpty() || mode == null || usedItems <= 0) {
            return ItemStack.EMPTY;
        }

        // One integer quality is stored for the whole output stack. Floor fractional averages so
        // homogenization can never manufacture total quality points through repeated crafting.
        int avgQuality = TobaccoProcessingHelper.getConservativeAverageQuality(
                totalQuality, usedItems, mode == Mode.RAW_LEAF);

        ItemStack result = new ItemStack(first.getItem(), usedItems);

        if (mode == Mode.RAW_LEAF) {
            TobaccoGrowthHelper.applyGrowthQuality(result, avgQuality);
        } else {
            TobaccoCuringHelper.copyTobaccoProcessingData(first, result);
            LegacyItemTags.getOrCreateTag(result).putInt(
                    TobaccoCuringHelper.TAG_QUALITY,
                    TobaccoCuringHelper.clampQuality(avgQuality)
            );
            LegacyItemTags.getOrCreateTag(result).putString(
                    TobaccoCuringHelper.TAG_QUALITY_TIER,
                    TobaccoCuringHelper.getQualityTierId(avgQuality)
            );
            LegacyItemTags.getOrCreateTag(result).putString(
                    TobaccoCuringHelper.TAG_CURE_TYPE,
                    cureType
            );

            if (mode == Mode.LOOSE) {
                LegacyItemTags.getOrCreateTag(result).putString(
                        TobaccoCuringHelper.TAG_CUT_TYPE,
                        cutType
                );
            }
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

    private static int getStackQuality(ItemStack stack, Mode mode) {
        if (mode == Mode.RAW_LEAF) {
            if (LegacyItemTags.hasTag(stack) && LegacyItemTags.getTag(stack).contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
                return TobaccoCuringHelper.clampQuality(LegacyItemTags.getTag(stack).getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY));
            }
            return 50;
        }

        return TobaccoCuringHelper.getQuality(stack);
    }

    private static Mode getMode(ItemStack stack) {
        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack)) return Mode.RAW_LEAF;
        if (TobaccoCuringHelper.isDryTobaccoLeaf(stack)) return Mode.DRY_LEAF;
        if (TobaccoBlendHelper.isBlendableBaseTobacco(stack)) return Mode.LOOSE;
        return null;
    }

    private enum Mode {
        RAW_LEAF,
        DRY_LEAF,
        LOOSE
    }
}