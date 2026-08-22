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

    /**
     * Returns the next target used by the planned Create + Chaveta production line.
     * Dry leaf -> Rough -> Ribbon -> Shag. Flake is intentionally reserved for pressing.
     */
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

    /**
     * Executes one step of the planned mechanical cutting progression.
     */
    public static ItemStack mechanicallyCutOne(ItemStack stack) {
        String nextCut = getNextMechanicalCut(stack);
        if (nextCut.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (TobaccoCuringHelper.isDryTobaccoLeaf(stack)) {
            // Match the existing manual cured-leaf cutting yield.
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
     * Flavor-aware progressive rule. Duplicate flavors are intentionally allowed: a player may
     * choose a double/triple-strength blend, provided the Shisha is unused and still has room.
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

    /** Applies one full 1000 mB flavored-molasses batch to loose tobacco or unused Shisha. */
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
        // applyTobaccoMetadata commits through the legacy CUSTOM_DATA bridge, so reacquire the
        // live tag before adding the remaining Shisha-specific fields.
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
    /**
     * Returns whether two tobacco leaves can be mechanically homogenized into a single quality.
     * Raw leaves are averaged by GrowthQuality before curing; cured leaves are averaged by their
     * final Quality. Both inputs must be the same leaf item and the same processing state apart
     * from the quality fields being averaged. Different quality values are required so Create's
     * Basin matcher selects two distinct quality stacks instead of consuming twice from one stack.
     */
    public static boolean canMechanicallyHomogenizeLeaves(ItemStack first, ItemStack second) {
        boolean rawPair = TobaccoCuringHelper.isRawTobaccoLeaf(first)
                && TobaccoCuringHelper.isRawTobaccoLeaf(second);
        boolean curedPair = TobaccoCuringHelper.isDryTobaccoLeaf(first)
                && TobaccoCuringHelper.isDryTobaccoLeaf(second);

        if ((!rawPair && !curedPair) || !ItemStack.isSameItem(first, second)) {
            return false;
        }

        if (getHomogenizingQuality(first) == getHomogenizingQuality(second)) {
            return false;
        }

        return ItemStack.isSameItemSameComponents(
                normalizeLeafForHomogenizing(first),
                normalizeLeafForHomogenizing(second)
        );
    }

    /**
     * Averages one leaf from each compatible quality stack and returns two identical leaves.
     * Raw leaves keep the result as GrowthQuality so they can be standardized before curing.
     * Cured leaves keep their existing cure/processing metadata and receive the averaged final
     * Quality. Integer half-points are rounded down deliberately so automation never creates
     * quality through rounding.
     */
    public static ItemStack mechanicallyHomogenizeLeafPair(ItemStack first, ItemStack second) {
        if (!canMechanicallyHomogenizeLeaves(first, second)) {
            return ItemStack.EMPTY;
        }

        int averagedQuality = (getHomogenizingQuality(first) + getHomogenizingQuality(second)) / 2;

        ItemStack result = first.copy();
        result.setCount(2);

        if (TobaccoCuringHelper.isRawTobaccoLeaf(result)) {
            TobaccoGrowthHelper.applyGrowthQuality(result, averagedQuality);
            // A raw leaf should never inherit a processing-only cut field from malformed legacy data.
            LegacyItemTags.getOrCreateTag(result).remove(TobaccoCuringHelper.TAG_CUT_TYPE);
            return result;
        }

        TobaccoCuringHelper.copyTobaccoProcessingData(first, result);

        CompoundTag tag = LegacyItemTags.getOrCreateTag(result);
        int clamped = TobaccoCuringHelper.clampQuality(averagedQuality);
        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, clamped);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(clamped));
        tag.remove(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
        return result;
    }

    private static int getHomogenizingQuality(ItemStack stack) {
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

    private static ItemStack normalizeLeafForHomogenizing(ItemStack stack) {
        ItemStack normalized = stack.copy();
        normalized.setCount(1);

        CompoundTag tag = LegacyItemTags.hasTag(normalized)
                ? LegacyItemTags.getTag(normalized).copy()
                : new CompoundTag();

        if (TobaccoCuringHelper.isRawTobaccoLeaf(normalized)) {
            tag.remove(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
            // These are not meaningful on raw leaves and should not prevent legacy stacks mixing.
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
