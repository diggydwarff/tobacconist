package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Shared transformations for tobacco processing.
 *
 * <p>Create integration must call these helpers instead of constructing fresh tobacco stacks on
 * its own. This keeps Tobacconist authoritative for variety, cure, quality, fermentation, aging,
 * ruin state, and other per-stack processing data.</p>
 */
public final class TobaccoProcessingHelper {
    private TobaccoProcessingHelper() {}

    public static boolean isValidCutType(String cutType) {
        return TobaccoCuringHelper.CUT_ROUGH.equals(cutType)
                || TobaccoCuringHelper.CUT_RIBBON.equals(cutType)
                || TobaccoCuringHelper.CUT_SHAG.equals(cutType)
                || TobaccoCuringHelper.CUT_FLAKE.equals(cutType);
    }

    /**
     * Converts one cured leaf into loose tobacco while preserving its processing data.
     *
     * @param dryLeaf cured tobacco leaf
     * @param cutType target cut
     * @param outputCount number of loose tobacco items produced
     */
    public static ItemStack cutDryLeaf(ItemStack dryLeaf, String cutType, int outputCount) {
        if (!TobaccoCuringHelper.isDryTobaccoLeaf(dryLeaf)
                || !isValidCutType(cutType)
                || outputCount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack result = TobaccoCuringHelper.getLooseTobaccoForDryLeaf(dryLeaf, outputCount);
        if (result.isEmpty()) {
            return ItemStack.EMPTY;
        }

        TobaccoCuringHelper.copyTobaccoProcessingData(dryLeaf, result);
        TobaccoCuringHelper.setCutType(result, cutType);
        return result;
    }

    /**
     * Changes the cut of one loose-tobacco item without changing its variety or processing data.
     * This is intended for staged mechanical cutting where each operation consumes one loose item.
     */
    public static ItemStack recutLooseTobacco(ItemStack looseTobacco, String cutType) {
        if (!TobaccoCuringHelper.isLooseTobacco(looseTobacco) || !isValidCutType(cutType)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = looseTobacco.copy();
        result.setCount(1);
        TobaccoCuringHelper.copyTobaccoProcessingData(looseTobacco, result);
        TobaccoCuringHelper.setCutType(result, cutType);
        return result;
    }

    /** Returns the next mechanical cut: cured leaf -> Rough -> Ribbon -> Shag. */
    public static String getNextMechanicalCut(ItemStack stack) {
        if (TobaccoCuringHelper.isDryTobaccoLeaf(stack)) {
            return TobaccoCuringHelper.CUT_ROUGH;
        }
        if (!TobaccoCuringHelper.isLooseTobacco(stack)) {
            return "";
        }

        return switch (TobaccoCuringHelper.getCutType(stack)) {
            case TobaccoCuringHelper.CUT_ROUGH -> TobaccoCuringHelper.CUT_RIBBON;
            case TobaccoCuringHelper.CUT_RIBBON -> TobaccoCuringHelper.CUT_SHAG;
            default -> "";
        };
    }

    /** Applies one mechanical cutting step. */
    public static ItemStack mechanicallyCutOne(ItemStack stack) {
        String nextCut = getNextMechanicalCut(stack);
        if (nextCut.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (TobaccoCuringHelper.isDryTobaccoLeaf(stack)) {
            // Cured leaves yield three loose tobacco.
            return cutDryLeaf(stack, nextCut, 3);
        }

        return recutLooseTobacco(stack, nextCut);
    }

    /** Any loose cut can become Shisha once it is thoroughly mixed with a full flavored-molasses batch. */
    public static boolean canMechanicallyMixToShisha(ItemStack stack) {
        return TobaccoCuringHelper.isLooseTobacco(stack)
                && !TobaccoBarrelBlockEntity.isRuined(stack);
    }

    /** Returns whether the stack is Tobacconist Shisha Tobacco. */
    public static boolean isShisha(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ModItems.SHISHA_TOBACCO.get();
    }

    /** Returns the currently stored Shisha flavors in their display order. */
    public static List<String> getShishaFlavors(ItemStack stack) {
        if (!isShisha(stack)) {
            return List.of();
        }

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null) {
            return List.of();
        }

        java.util.ArrayList<String> flavors = new java.util.ArrayList<>(3);
        for (String key : List.of("flavor1", "flavor2", "flavor3")) {
            String flavor = tag.getString(key);
            if (!flavor.isBlank()) {
                flavors.add(flavor);
            }
        }
        return List.copyOf(flavors);
    }

    /** Number of flavor slots already occupied on a Shisha stack. */
    public static int getShishaFlavorCount(ItemStack stack) {
        return getShishaFlavors(stack).size();
    }

    /** Returns whether this exact flavor is already present on the Shisha stack. */
    public static boolean hasShishaFlavor(ItemStack stack, String flavorName) {
        if (flavorName == null || flavorName.isBlank()) {
            return false;
        }
        return getShishaFlavors(stack).contains(flavorName);
    }

    /**
     * Existing Shisha may be flavored again only while completely unused and below the three-
     * flavor cap. Once Hookah use has added durability damage, its blend is locked.
     */
    public static boolean canAddShishaFlavor(ItemStack stack) {
        return isShisha(stack)
                && stack.getDamageValue() == 0
                && getShishaFlavorCount(stack) < 3;
    }

    /**
     * Duplicate flavor entries are permitted while unused Shisha still has an open flavor slot.
     */
    public static boolean canAddShishaFlavor(ItemStack stack, String flavorName) {
        return canAddShishaFlavor(stack)
                && flavorName != null
                && !flavorName.isBlank();
    }

    /**
     * Adds one or more flavors to unused Shisha without recalculating or replacing any tobacco
     * metadata. The returned stack is a one-item copy of the original with only the empty flavor
     * slots filled.
     */
    public static ItemStack addShishaFlavors(ItemStack shishaStack, List<String> additionalFlavors) {
        if (!canAddShishaFlavor(shishaStack)
                || additionalFlavors == null
                || additionalFlavors.isEmpty()) {
            return ItemStack.EMPTY;
        }

        List<String> existing = getShishaFlavors(shishaStack);
        if (existing.size() + additionalFlavors.size() > 3
                || additionalFlavors.stream().anyMatch(flavor -> flavor == null || flavor.isBlank())) {
            return ItemStack.EMPTY;
        }

        ItemStack result = shishaStack.copy();
        result.setCount(1);

        java.util.ArrayList<String> combined = new java.util.ArrayList<>(existing);
        combined.addAll(additionalFlavors);

        CompoundTag tag = LegacyItemTags.getOrCreateTag(result);
        tag.putString("flavor1", combined.size() > 0 ? combined.get(0) : "");
        tag.putString("flavor2", combined.size() > 1 ? combined.get(1) : "");
        tag.putString("flavor3", combined.size() > 2 ? combined.get(2) : "");
        return result;
    }

    /**
     * Create's progressive flavoring rule. A Mixer can either start Shisha from loose tobacco or
     * add one more flavor to unused Shisha that still has an open flavor slot.
     */
    public static boolean canMechanicallyFlavorShisha(ItemStack stack) {
        return canMechanicallyMixToShisha(stack) || canAddShishaFlavor(stack);
    }

    /** Flavor-aware Create Mixer rule used while matching a specific molasses recipe. */
    public static boolean canMechanicallyFlavorShisha(ItemStack stack, String flavorName) {
        if (flavorName == null || flavorName.isBlank()) {
            return false;
        }
        return canMechanicallyMixToShisha(stack) || canAddShishaFlavor(stack, flavorName);
    }

    /** Applies one full 250 mB flavored-molasses bottle to loose tobacco or unused Shisha. */
    public static ItemStack mechanicallyFlavorShisha(ItemStack stack, String flavorName) {
        if (!canMechanicallyFlavorShisha(stack, flavorName)) {
            return ItemStack.EMPTY;
        }

        if (canMechanicallyMixToShisha(stack)) {
            return createShisha(stack, List.of(flavorName));
        }
        return addShishaFlavors(stack, List.of(flavorName));
    }

    /**
     * Builds the canonical Shisha output from the actual tobacco stack and one to three flavor
     * labels. Both vanilla crafting and Create mixing call this method so their metadata behavior
     * stays identical.
     */
    public static ItemStack createShisha(ItemStack tobaccoStack, List<String> flavorNames) {
        if (tobaccoStack.isEmpty() || !TobaccoCuringHelper.isLooseTobacco(tobaccoStack)
                || flavorNames == null || flavorNames.isEmpty() || flavorNames.size() > 3) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(ModItems.SHISHA_TOBACCO.get(), 1);
        CompoundTag tag = LegacyItemTags.getOrCreateTag(result);

        tag.putString("tobacco", TobaccoProductQualityHelper.getShortTobaccoLabel(tobaccoStack));
        tag.putString("flavor1", flavorNames.get(0));
        tag.putString("flavor2", flavorNames.size() > 1 ? flavorNames.get(1) : "");
        tag.putString("flavor3", flavorNames.size() > 2 ? flavorNames.get(2) : "");

        TobaccoDataHelper.applyTobaccoMetadata(result, tobaccoStack);
        // Reacquire custom data after applyTobaccoMetadata writes the stack component.
        tag = LegacyItemTags.getOrCreateTag(result);

        CompoundTag tobaccoData = LegacyItemTags.getTag(tobaccoStack);
        if (tobaccoData != null) {
            if (tobaccoData.contains("AgedDays")) {
                tag.putInt("AgedDays", tobaccoData.getInt("AgedDays"));
            }
            if (tobaccoData.getBoolean("Fermented")) {
                tag.putBoolean("Fermented", true);
            }
            if (tobaccoData.getBoolean("Ruined")) {
                tag.putBoolean("Ruined", true);
            }
        }

        TobaccoProductQualityHelper.applyProductQualityToTag(
                tag, tobaccoStack, TobaccoProductQualityHelper.getShishaQuality(tobaccoStack));
        return result;
    }

    /**
     * Returns whether this loose tobacco is eligible for the Create Mechanical Press branch.
     * Rough tobacco is the intentional branch point: Chaveta cutting can continue toward Ribbon
     * and Shag, while pressing Rough tobacco produces Flake.
     */
    public static boolean canMechanicallyPressToFlake(ItemStack stack) {
        return TobaccoCuringHelper.isLooseTobacco(stack)
                && TobaccoCuringHelper.CUT_ROUGH.equals(TobaccoCuringHelper.getCutType(stack));
    }

    /**
     * Presses one Rough loose-tobacco item into one Flake item without changing its tobacco data.
     * Create's recipe application handles stack quantities by applying this one-item result once
     * per processed input item.
     */
    public static ItemStack mechanicallyPressOne(ItemStack stack) {
        if (!canMechanicallyPressToFlake(stack)) {
            return ItemStack.EMPTY;
        }

        return recutLooseTobacco(stack, TobaccoCuringHelper.CUT_FLAKE);
    }

    /** Returns whether two leaf stacks differ only in homogenized quality metadata. */
    public static boolean areHomogenizingCompatibleLeaves(ItemStack first, ItemStack second) {
        boolean rawPair = TobaccoCuringHelper.isRawTobaccoLeaf(first)
                && TobaccoCuringHelper.isRawTobaccoLeaf(second);
        boolean curedPair = TobaccoCuringHelper.isDryTobaccoLeaf(first)
                && TobaccoCuringHelper.isDryTobaccoLeaf(second);

        if ((!rawPair && !curedPair) || !ItemStack.isSameItem(first, second)) {
            return false;
        }

        return ItemStack.isSameItemSameTags(
                normalizeLeafForHomogenizing(first),
                normalizeLeafForHomogenizing(second)
        );
    }

    /** Compatibility alias retained for integrations that still perform two-leaf checks. */
    public static boolean canMechanicallyHomogenizeLeaves(ItemStack first, ItemStack second) {
        return areHomogenizingCompatibleLeaves(first, second)
                && getHomogenizingQuality(first) != getHomogenizingQuality(second);
    }

    /**
     * Returns the highest whole-number quality that cannot create quality points.
     *
     * <p>A homogenized stack can only carry one integer quality value for every item. When the
     * exact average is fractional, rounding to nearest can manufacture quality (for example,
     * 50 + 51 -> two 51-quality leaves). Flooring the exact point total is therefore intentional:
     * item count is preserved and at most the unavoidable fractional remainder is discarded.</p>
     */
    public static int getConservativeAverageQuality(long qualityPoints, int itemCount, boolean rawLeaf) {
        if (itemCount <= 0) return 0;

        long average = Math.floorDiv(Math.max(0L, qualityPoints), itemCount);
        if (rawLeaf) {
            return (int) Math.max(0L, Math.min(70L, average));
        }
        return TobaccoCuringHelper.clampQuality((int) Math.min(Integer.MAX_VALUE, average));
    }

    /** Convenience overload for leaf homogenization, deriving the raw/cured range from the template. */
    public static int getConservativeHomogenizedQuality(ItemStack template, long qualityPoints, int itemCount) {
        return getConservativeAverageQuality(
                qualityPoints,
                itemCount,
                TobaccoCuringHelper.isRawTobaccoLeaf(template)
        );
    }

    /** Returns the quality value used by bulk homogenization. */
    public static int getHomogenizingQuality(ItemStack stack) {
        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack)) {
            CompoundTag tag = LegacyItemTags.getTag(stack);
            if (tag != null && tag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
                return Math.max(0, Math.min(70,
                        tag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY)));
            }
            return 50;
        }
        return TobaccoCuringHelper.getQuality(stack);
    }

    /**
     * Builds one standardized leaf stack from a compatible batch. The supplied quality is applied
     * directly and the output count is preserved. Raw leaves retain GrowthQuality; cured leaves
     * retain their cure and other processing metadata. Quality is never rounded upward here.
     */
    public static ItemStack buildHomogenizedLeafBatch(ItemStack template, int quality, int count) {
        if (template.isEmpty() || count <= 0
                || (!TobaccoCuringHelper.isRawTobaccoLeaf(template)
                && !TobaccoCuringHelper.isDryTobaccoLeaf(template))) {
            return ItemStack.EMPTY;
        }

        ItemStack result = template.copy();
        result.setCount(count);

        if (TobaccoCuringHelper.isRawTobaccoLeaf(result)) {
            TobaccoGrowthHelper.applyGrowthQuality(result, Math.max(0, Math.min(70, quality)));
            CompoundTag tag = LegacyItemTags.getOrCreateTag(result);
            tag.remove(TobaccoCuringHelper.TAG_QUALITY);
            tag.remove(TobaccoCuringHelper.TAG_QUALITY_TIER);
            tag.remove(TobaccoCuringHelper.TAG_CURE_TYPE);
            tag.remove(TobaccoCuringHelper.TAG_CUT_TYPE);
            return result;
        }

        CompoundTag tag = LegacyItemTags.getOrCreateTag(result);
        int clamped = TobaccoCuringHelper.clampQuality(quality);
        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, clamped);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(clamped));
        tag.remove(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
        return result;
    }

    private static ItemStack normalizeLeafForHomogenizing(ItemStack stack) {
        ItemStack normalized = stack.copy();
        normalized.setCount(1);

        CompoundTag tag = LegacyItemTags.hasTag(normalized)
                ? LegacyItemTags.getTag(normalized).copy()
                : new CompoundTag();

        if (TobaccoCuringHelper.isRawTobaccoLeaf(normalized)) {
            tag.remove(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
            tag.remove(TobaccoCuringHelper.TAG_QUALITY);
            tag.remove(TobaccoCuringHelper.TAG_QUALITY_TIER);
            tag.remove(TobaccoCuringHelper.TAG_CURE_TYPE);
            tag.remove(TobaccoCuringHelper.TAG_CUT_TYPE);
        } else {
            tag.remove(TobaccoCuringHelper.TAG_QUALITY);
            tag.remove(TobaccoCuringHelper.TAG_QUALITY_TIER);
            tag.remove(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
        }

        LegacyItemTags.setTag(normalized, tag);
        return normalized;
    }

}
