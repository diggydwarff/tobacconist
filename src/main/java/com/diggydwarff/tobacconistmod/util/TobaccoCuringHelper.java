package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class TobaccoCuringHelper {
    public static final String TAG_CURE_TYPE = "CureType";
    public static final String TAG_QUALITY = "Quality";
    public static final String TAG_GROWTH_QUALITY = "GrowthQuality";
    public static final String TAG_QUALITY_TIER = "QualityTier";
    public static final String TAG_CUT_TYPE = "CutType";

    public static final String CURE_AIR = "air";
    public static final String CURE_FIRE = "fire";
    public static final String CURE_SUN = "sun";
    public static final String CURE_FLUE = "flue";

    public static final String CUT_RIBBON = "ribbon";
    public static final String CUT_SHAG = "shag";
    public static final String CUT_ROUGH = "rough";
    public static final String CUT_FLAKE = "flake";

    private TobaccoCuringHelper() {}

    public static boolean isRawTobaccoLeaf(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == ModItems.WILD_TOBACCO_LEAF.get()
                || item == ModItems.VIRGINIA_TOBACCO_LEAF.get()
                || item == ModItems.BURLEY_TOBACCO_LEAF.get()
                || item == ModItems.ORIENTAL_TOBACCO_LEAF.get()
                || item == ModItems.DOKHA_TOBACCO_LEAF.get()
                || item == ModItems.SHADE_TOBACCO_LEAF.get();
    }

    public static boolean isDryTobaccoLeaf(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == ModItems.WILD_TOBACCO_LEAF_DRY.get()
                || item == ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()
                || item == ModItems.BURLEY_TOBACCO_LEAF_DRY.get()
                || item == ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get()
                || item == ModItems.DOKHA_TOBACCO_LEAF_DRY.get()
                || item == ModItems.SHADE_TOBACCO_LEAF_DRY.get();
    }

    public static boolean isLooseTobacco(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == ModItems.TOBACCO_LOOSE_WILD.get()
                || item == ModItems.TOBACCO_LOOSE_VIRGINIA.get()
                || item == ModItems.TOBACCO_LOOSE_BURLEY.get()
                || item == ModItems.TOBACCO_LOOSE_ORIENTAL.get()
                || item == ModItems.TOBACCO_LOOSE_DOKHA.get()
                || item == ModItems.TOBACCO_LOOSE_SHADE.get();
    }

    public static boolean isProcessedTobacco(ItemStack stack) {
        return isDryTobaccoLeaf(stack) || isLooseTobacco(stack);
    }

    public static boolean isChaveta(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == ModItems.STONE_CHAVETA.get()
                || item == ModItems.COPPER_CHAVETA.get()
                || item == ModItems.IRON_CHAVETA.get()
                || item == ModItems.GOLD_CHAVETA.get()
                || item == ModItems.DIAMOND_CHAVETA.get()
                || item == ModItems.NETHERITE_CHAVETA.get();
    }

    public static String getQualityTierId(int quality) {
        int clamped = clampQuality(quality);
        if (clamped <= 24) return "poor";
        if (clamped <= 49) return "common";
        if (clamped <= 69) return "good";
        if (clamped <= 89) return "fine";
        return "premium";
    }

    public static ItemStack getCuredLeafForRaw(ItemStack rawStack) {
        if (rawStack.isEmpty()) return ItemStack.EMPTY;

        Item item = rawStack.getItem();
        ItemStack result;

        if (item == ModItems.WILD_TOBACCO_LEAF.get()) {
            result = new ItemStack(ModItems.WILD_TOBACCO_LEAF_DRY.get());
        } else if (item == ModItems.VIRGINIA_TOBACCO_LEAF.get()) {
            result = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        } else if (item == ModItems.BURLEY_TOBACCO_LEAF.get()) {
            result = new ItemStack(ModItems.BURLEY_TOBACCO_LEAF_DRY.get());
        } else if (item == ModItems.ORIENTAL_TOBACCO_LEAF.get()) {
            result = new ItemStack(ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get());
        } else if (item == ModItems.DOKHA_TOBACCO_LEAF.get()) {
            result = new ItemStack(ModItems.DOKHA_TOBACCO_LEAF_DRY.get());
        } else if (item == ModItems.SHADE_TOBACCO_LEAF.get()) {
            result = new ItemStack(ModItems.SHADE_TOBACCO_LEAF_DRY.get());
        } else {
            return ItemStack.EMPTY;
        }

        if (rawStack.hasTag()) {
            result.setTag(rawStack.getTag().copy());
        }

        return result;
    }

    public static ItemStack getLooseTobaccoForDryLeaf(ItemStack dryLeaf, int count) {
        if (dryLeaf.isEmpty()) return ItemStack.EMPTY;

        Item item = dryLeaf.getItem();
        ItemStack result;

        if (item == ModItems.WILD_TOBACCO_LEAF_DRY.get()) {
            result = new ItemStack(ModItems.TOBACCO_LOOSE_WILD.get(), count);
        } else if (item == ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()) {
            result = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get(), count);
        } else if (item == ModItems.BURLEY_TOBACCO_LEAF_DRY.get()) {
            result = new ItemStack(ModItems.TOBACCO_LOOSE_BURLEY.get(), count);
        } else if (item == ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get()) {
            result = new ItemStack(ModItems.TOBACCO_LOOSE_ORIENTAL.get(), count);
        } else if (item == ModItems.DOKHA_TOBACCO_LEAF_DRY.get()) {
            result = new ItemStack(ModItems.TOBACCO_LOOSE_DOKHA.get(), count);
        } else if (item == ModItems.SHADE_TOBACCO_LEAF_DRY.get()) {
            result = new ItemStack(ModItems.TOBACCO_LOOSE_SHADE.get(), count);
        } else {
            return ItemStack.EMPTY;
        }

        return result;
    }

    public static void copyTobaccoProcessingData(ItemStack from, ItemStack to) {
        CompoundTag tag = from.hasTag() ? from.getTag().copy() : new CompoundTag();
        tag.remove(TAG_GROWTH_QUALITY);

        int quality = getQuality(from);
        tag.putInt(TAG_QUALITY, quality);
        tag.putString(TAG_QUALITY_TIER, getQualityTierId(quality));

        to.setTag(tag);
    }

    public static void setCutType(ItemStack stack, String cutType) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_CUT_TYPE, cutType);
    }

    public static String getCutType(ItemStack stack) {
        if (!stack.hasTag()) return "";
        CompoundTag tag = stack.getTag();
        if (!tag.contains(TAG_CUT_TYPE)) return "";
        return tag.getString(TAG_CUT_TYPE);
    }

    public static String getCutDisplayName(String cutType) {
        return switch (cutType) {
            case CUT_RIBBON -> "Ribbon Cut";
            case CUT_SHAG -> "Shag Cut";
            case CUT_ROUGH -> "Rough Cut";
            case CUT_FLAKE -> "Flake Cut";
            default -> "Uncut";
        };
    }

    public static void applyCureData(ItemStack stack, String cureType, int quality) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_CURE_TYPE, cureType);

        int clamped = clampQuality(quality);
        tag.putInt(TAG_QUALITY, clamped);
        tag.putString(TAG_QUALITY_TIER, getQualityTierId(clamped));
    }

    public static String getCureType(ItemStack stack) {
        if (!stack.hasTag()) return CURE_AIR;

        CompoundTag tag = stack.getTag();
        if (!tag.contains(TAG_CURE_TYPE)) return CURE_AIR;

        String cureType = tag.getString(TAG_CURE_TYPE);
        return cureType == null || cureType.isEmpty() ? CURE_AIR : cureType;
    }

    public static int getCanonicalTierQuality(int quality) {
        int clamped = clampQuality(quality);

        if (clamped >= 98) {
            return 100;
        }

        return switch (getQualityTierId(clamped)) {
            case "poor" -> 12;
            case "common" -> 37;
            case "good" -> 60;
            case "fine" -> 80;
            default -> 95;
        };
    }

    public static int getQuality(ItemStack stack) {
        if (!stack.hasTag()) return 60;

        CompoundTag tag = stack.getTag();
        if (tag.contains(TAG_QUALITY)) {
            return clampQuality(tag.getInt(TAG_QUALITY));
        }
        if (tag.contains(TAG_GROWTH_QUALITY)) {
            return clampQuality(tag.getInt(TAG_GROWTH_QUALITY));
        }
        return 60;
    }

    public static int buildFinalQuality(ItemStack inputLeaf, String cureType, int interruptionCount) {
        int quality = inputLeaf.hasTag() && inputLeaf.getTag().contains(TAG_GROWTH_QUALITY)
                ? inputLeaf.getTag().getInt(TAG_GROWTH_QUALITY)
                : 60;

        quality += switch (cureType) {
            case CURE_FLUE -> 6;
            case CURE_SUN -> 5;
            case CURE_FIRE -> 4;
            case CURE_AIR -> 3;
            default -> 0;
        };

        quality -= interruptionCount * 5;
        return clampQuality(quality);
    }

    public static int clampQuality(int quality) {
        return Math.max(0, Math.min(100, quality));
    }

    public static String getQualityTier(int quality) {
        int clamped = clampQuality(quality);
        if (clamped <= 24) return "Poor";
        if (clamped <= 49) return "Common";
        if (clamped <= 69) return "Good";
        if (clamped <= 89) return "Fine";
        return clamped >= 98 ? "Perfect" : "Premium";
    }

    public static String getCureDisplayName(String cureType) {
        return switch (cureType) {
            case CURE_FIRE -> "Fire-Cured";
            case CURE_SUN -> "Sun-Cured";
            case CURE_FLUE -> "Flue-Cured";
            default -> "Air-Cured";
        };
    }
}