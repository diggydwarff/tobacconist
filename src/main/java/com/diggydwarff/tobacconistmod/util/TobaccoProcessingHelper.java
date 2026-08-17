package com.diggydwarff.tobacconistmod.util;

import net.minecraft.world.item.ItemStack;

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
}
