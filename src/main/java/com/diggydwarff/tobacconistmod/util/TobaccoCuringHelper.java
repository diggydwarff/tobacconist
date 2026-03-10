package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class TobaccoCuringHelper {
    public static final String TAG_CURE_TYPE = "CureType";
    public static final String TAG_QUALITY = "Quality";
    public static final String TAG_GROWTH_QUALITY = "GrowthQuality";

    public static final String CURE_AIR = "air";
    public static final String CURE_FIRE = "fire";
    public static final String CURE_SUN = "sun";
    public static final String CURE_FLUE = "flue";
    public static final String TAG_QUALITY_TIER = "QualityTier";

    private TobaccoCuringHelper() {
    }

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

    public static String getQualityTierId(int quality) {
        int clamped = clampQuality(quality);
        if (clamped <= 24) return "poor";
        if (clamped <= 49) return "common";
        if (clamped <= 69) return "good";
        if (clamped <= 89) return "fine";
        return "premium";
    }

    public static ItemStack getCuredLeafForRaw(ItemStack rawStack) {
        if (rawStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

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

    public static void applyCureData(ItemStack stack, String cureType, int quality) {
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString(TAG_CURE_TYPE, cureType);

        int canonical = getCanonicalTierQuality(quality);
        tag.putInt(TAG_QUALITY, canonical);
        tag.putString(TAG_QUALITY_TIER, getQualityTierId(canonical));
    }

    public static String getCureType(ItemStack stack) {
        if (!stack.hasTag()) {
            return CURE_AIR;
        }
        return stack.getTag().getString(TAG_CURE_TYPE);
    }

    public static int getCanonicalTierQuality(int quality) {
        return switch (getQualityTierId(quality)) {
            case "poor" -> 12;
            case "common" -> 37;
            case "good" -> 60;
            case "fine" -> 80;
            default -> 95;
        };
    }

    public static int getQuality(ItemStack stack) {
        if (!stack.hasTag()) {
            return 50;
        }
        CompoundTag tag = stack.getTag();
        if (tag.contains(TAG_QUALITY)) {
            return clampQuality(tag.getInt(TAG_QUALITY));
        }
        if (tag.contains(TAG_GROWTH_QUALITY)) {
            return clampQuality(tag.getInt(TAG_GROWTH_QUALITY));
        }
        return 50;
    }

    public static int buildFinalQuality(ItemStack inputLeaf, String cureType, int interruptionCount) {
        int quality = inputLeaf.hasTag() && inputLeaf.getTag().contains(TAG_GROWTH_QUALITY)
                ? inputLeaf.getTag().getInt(TAG_GROWTH_QUALITY)
                : 85;

        quality += switch (cureType) {
            case CURE_SUN -> 8;
            case CURE_FIRE -> 6;
            case CURE_AIR -> 4;
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
        return "Premium";
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
