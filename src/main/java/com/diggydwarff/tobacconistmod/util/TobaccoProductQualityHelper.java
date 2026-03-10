package com.diggydwarff.tobacconistmod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class TobaccoProductQualityHelper {
    public static final String TAG_PRODUCT_QUALITY = "ProductQuality";
    public static final String TAG_INPUT_TOBACCO_QUALITY = "InputTobaccoQuality";
    public static final String TAG_INPUT_CUT_TYPE = "InputCutType";
    public static final String TAG_INPUT_CURE_TYPE = "InputCureType";

    private TobaccoProductQualityHelper() {}

    public static int getCigaretteQuality(ItemStack tobacco) {
        return score(tobacco, ProductType.CIGARETTE);
    }

    public static int getCigarQuality(ItemStack tobacco) {
        return score(tobacco, ProductType.CIGAR);
    }

    public static int getShishaQuality(ItemStack tobacco) {
        return score(tobacco, ProductType.SHISHA);
    }

    public static String getShortTobaccoLabel(ItemStack tobaccoStack) {
        String qualityTier = TobaccoCuringHelper.getQualityTier(TobaccoCuringHelper.getQuality(tobaccoStack));
        String baseName = tobaccoStack.getItem().getDescription().getString();

        if (baseName.startsWith("item.")) {
            baseName = tobaccoStack.getItem().getDescriptionId();
        }

        String itemName = tobaccoStack.getHoverName().getString();

        String cureName = TobaccoCuringHelper.getCureDisplayName(TobaccoCuringHelper.getCureType(tobaccoStack));
        String cutName = TobaccoCuringHelper.getCutDisplayName(TobaccoCuringHelper.getCutType(tobaccoStack));

        String prefix = qualityTier;
        if (!cureName.isEmpty()) prefix += " " + cureName;
        if (!cutName.isEmpty()) prefix += " " + cutName;

        if (itemName.startsWith(prefix + " ")) {
            return qualityTier + " " + itemName.substring(prefix.length()).trim();
        }

        return itemName;
    }

    public static void applyProductQualityToTag(CompoundTag tag, ItemStack tobacco, int score) {
        tag.putInt(TAG_PRODUCT_QUALITY, clampTen(score));
        tag.putInt(TAG_INPUT_TOBACCO_QUALITY, TobaccoCuringHelper.getQuality(tobacco));

        String cutType = TobaccoCuringHelper.getCutType(tobacco);
        if (!cutType.isEmpty()) {
            tag.putString(TAG_INPUT_CUT_TYPE, cutType);
        }

        String cureType = TobaccoCuringHelper.getCureType(tobacco);
        if (!cureType.isEmpty()) {
            tag.putString(TAG_INPUT_CURE_TYPE, cureType);
        }
    }

    public static int getStoredProductQuality(ItemStack stack) {
        if (!stack.hasTag()) return -1;
        CompoundTag tag = stack.getTag();
        if (!tag.contains(TAG_PRODUCT_QUALITY)) return -1;
        return clampTen(tag.getInt(TAG_PRODUCT_QUALITY));
    }

    private static int score(ItemStack tobacco, ProductType type) {
        int base = Math.round(TobaccoCuringHelper.getQuality(tobacco) / 10.0f);
        String cutType = TobaccoCuringHelper.getCutType(tobacco);

        int modifier = getModifier(type, cutType);
        int cap = getCap(type, cutType);

        return Math.min(cap, clampTen(base + modifier));
    }

    private static int getModifier(ProductType type, String cutType) {
        return switch (type) {
            case CIGARETTE -> switch (cutType) {
                case TobaccoCuringHelper.CUT_SHAG -> 0;
                case TobaccoCuringHelper.CUT_RIBBON -> -1;
                case TobaccoCuringHelper.CUT_ROUGH -> -3;
                case TobaccoCuringHelper.CUT_FLAKE -> -4;
                default -> -5;
            };
            case CIGAR -> switch (cutType) {
                case TobaccoCuringHelper.CUT_FLAKE -> 0;
                case TobaccoCuringHelper.CUT_ROUGH -> -1;
                case TobaccoCuringHelper.CUT_RIBBON -> -2;
                case TobaccoCuringHelper.CUT_SHAG -> -4;
                default -> -5;
            };
            case SHISHA -> switch (cutType) {
                case TobaccoCuringHelper.CUT_ROUGH -> 0;
                case TobaccoCuringHelper.CUT_RIBBON -> -1;
                case TobaccoCuringHelper.CUT_FLAKE -> -2;
                case TobaccoCuringHelper.CUT_SHAG -> -4;
                default -> -5;
            };
        };
    }

    private static int getCap(ProductType type, String cutType) {
        return switch (type) {
            case CIGARETTE -> switch (cutType) {
                case TobaccoCuringHelper.CUT_SHAG -> 10;
                case TobaccoCuringHelper.CUT_RIBBON -> 9;
                case TobaccoCuringHelper.CUT_ROUGH -> 6;
                case TobaccoCuringHelper.CUT_FLAKE -> 5;
                default -> 3;
            };
            case CIGAR -> switch (cutType) {
                case TobaccoCuringHelper.CUT_FLAKE -> 10;
                case TobaccoCuringHelper.CUT_ROUGH -> 9;
                case TobaccoCuringHelper.CUT_RIBBON -> 7;
                case TobaccoCuringHelper.CUT_SHAG -> 4;
                default -> 3;
            };
            case SHISHA -> switch (cutType) {
                case TobaccoCuringHelper.CUT_ROUGH -> 10;
                case TobaccoCuringHelper.CUT_RIBBON -> 8;
                case TobaccoCuringHelper.CUT_FLAKE -> 7;
                case TobaccoCuringHelper.CUT_SHAG -> 4;
                default -> 3;
            };
        };
    }

    private static int clampTen(int value) {
        return Math.max(0, Math.min(10, value));
    }

    private enum ProductType {
        CIGARETTE,
        CIGAR,
        SHISHA
    }
}