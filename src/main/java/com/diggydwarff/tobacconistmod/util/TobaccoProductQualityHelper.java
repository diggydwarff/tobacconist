package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class TobaccoProductQualityHelper {
    public static final String TAG_PRODUCT_QUALITY = "ProductQuality";
    public static final String TAG_INPUT_TOBACCO_QUALITY = "InputTobaccoQuality";
    public static final String TAG_INPUT_WRAPPER_QUALITY = "InputWrapperQuality";
    public static final String TAG_INPUT_CUT_TYPE = "InputCutType";
    public static final String TAG_INPUT_CURE_TYPE = "InputCureType";

    private TobaccoProductQualityHelper() {}

    public static int getCigaretteQuality(ItemStack tobacco) {
        return scoreSingleTobacco(tobacco, ProductType.CIGARETTE);
    }

    /**
     * Cigar quality is a product score, not just the filler leaf score. Filler carries most of
     * the cigar, while the wrapper still contributes meaningfully to burn/construction quality.
     */
    public static int getCigarQuality(ItemStack filler, ItemStack wrapper) {
        int fillerQuality = TobaccoCuringHelper.getQuality(filler);
        int wrapperQuality = wrapper == null || wrapper.isEmpty()
                ? fillerQuality
                : TobaccoCuringHelper.getQuality(wrapper);
        int compositeQuality = Math.round(fillerQuality * 0.75f + wrapperQuality * 0.25f);
        return scoreQuality(compositeQuality, TobaccoCuringHelper.getCutType(filler), ProductType.CIGAR);
    }

    /** Backward-compatible filler-only fallback for callers that do not have wrapper data. */
    public static int getCigarQuality(ItemStack tobacco) {
        return getCigarQuality(tobacco, ItemStack.EMPTY);
    }

    public static int getShishaQuality(ItemStack tobacco) {
        return scoreSingleTobacco(tobacco, ProductType.SHISHA);
    }

    /**
     * Finished products use the displayed ProductQuality for smoking-quality effects so the
     * tooltip, Create filters, Tobacco Boxes, and actual smoking bonus all describe one score.
     */
    public static int getEffectiveSmokingQuality(ItemStack stack) {
        int productQuality = getStoredProductQuality(stack);
        return productQuality >= 0 ? productQuality * 10 : TobaccoCuringHelper.getQuality(stack);
    }

    public static String getShortTobaccoLabel(ItemStack tobaccoStack) {
        if (!TobacconistConfig.isQualitySystemEnabled()) {
            return tobaccoStack.getHoverName().getString()
                    .replaceFirst("^(Premium|Fine|Standard|Harsh|Poor|Low-Grade)\\s+", "")
                    .trim();
        }

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

    public static void applyCigarProductQualityToTag(
            CompoundTag tag,
            ItemStack filler,
            ItemStack wrapper,
            int score
    ) {
        applyProductQualityToTag(tag, filler, score);
        if (wrapper != null && !wrapper.isEmpty()) {
            tag.putInt(TAG_INPUT_WRAPPER_QUALITY, TobaccoCuringHelper.getQuality(wrapper));
        }
    }

    public static int getStoredProductQuality(ItemStack stack) {
        if (!LegacyItemTags.hasTag(stack)) return -1;
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (!tag.contains(TAG_PRODUCT_QUALITY)) return -1;
        return clampTen(tag.getInt(TAG_PRODUCT_QUALITY));
    }

    private static int scoreSingleTobacco(ItemStack tobacco, ProductType type) {
        return scoreQuality(
                TobaccoCuringHelper.getQuality(tobacco),
                TobaccoCuringHelper.getCutType(tobacco),
                type
        );
    }

    /**
     * Leaf quality remains the dominant factor. Cut choice is a modest preparation modifier,
     * with only obviously unsuitable preparations imposing a ceiling. This avoids turning an
     * excellent leaf into a mediocre product solely because it used a reasonable second-choice cut.
     */
    private static int scoreQuality(int quality100, String cutType, ProductType type) {
        int adjusted = TobaccoCuringHelper.clampQuality(quality100) + getQualityPointModifier(type, cutType);
        int score = Math.round(Math.max(0, adjusted) / 10.0f);
        return Math.min(getCap(type, cutType), clampTen(score));
    }

    private static int getQualityPointModifier(ProductType type, String cutType) {
        return switch (type) {
            case CIGARETTE -> switch (cutType) {
                case TobaccoCuringHelper.CUT_SHAG -> 0;
                case TobaccoCuringHelper.CUT_RIBBON -> -3;
                case TobaccoCuringHelper.CUT_ROUGH -> -12;
                case TobaccoCuringHelper.CUT_FLAKE -> -20;
                default -> -25;
            };
            case CIGAR -> switch (cutType) {
                // Rough is the closest Tobacconist preparation to conventional cigar filler.
                case TobaccoCuringHelper.CUT_ROUGH -> 0;
                case TobaccoCuringHelper.CUT_RIBBON -> -3;
                case TobaccoCuringHelper.CUT_FLAKE -> -8;
                case TobaccoCuringHelper.CUT_SHAG -> -15;
                default -> -20;
            };
            case SHISHA -> switch (cutType) {
                case TobaccoCuringHelper.CUT_ROUGH -> 0;
                case TobaccoCuringHelper.CUT_RIBBON -> -3;
                case TobaccoCuringHelper.CUT_FLAKE -> -10;
                case TobaccoCuringHelper.CUT_SHAG -> -15;
                default -> -20;
            };
        };
    }

    private static int getCap(ProductType type, String cutType) {
        return switch (type) {
            case CIGARETTE -> switch (cutType) {
                case TobaccoCuringHelper.CUT_SHAG, TobaccoCuringHelper.CUT_RIBBON -> 10;
                case TobaccoCuringHelper.CUT_ROUGH -> 8;
                case TobaccoCuringHelper.CUT_FLAKE -> 7;
                default -> 6;
            };
            case CIGAR -> switch (cutType) {
                case TobaccoCuringHelper.CUT_ROUGH, TobaccoCuringHelper.CUT_RIBBON -> 10;
                case TobaccoCuringHelper.CUT_FLAKE -> 9;
                case TobaccoCuringHelper.CUT_SHAG -> 8;
                default -> 7;
            };
            case SHISHA -> switch (cutType) {
                case TobaccoCuringHelper.CUT_ROUGH, TobaccoCuringHelper.CUT_RIBBON -> 10;
                case TobaccoCuringHelper.CUT_FLAKE -> 9;
                case TobaccoCuringHelper.CUT_SHAG -> 8;
                default -> 7;
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
