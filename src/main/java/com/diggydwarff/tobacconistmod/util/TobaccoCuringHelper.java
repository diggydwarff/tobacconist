package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

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
        if (clamped <= 30) return "poor";
        if (clamped <= 60) return "common";
        if (clamped <= 80) return "good";
        if (clamped <= 89) return "excellent";
        if (clamped <= 100) return "perfect";
        return "exceptional";
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

        if (LegacyItemTags.hasTag(rawStack)) {
            LegacyItemTags.setTag(result, LegacyItemTags.getTag(rawStack).copy());
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
        CompoundTag tag = LegacyItemTags.hasTag(from) ? LegacyItemTags.getTag(from).copy() : new CompoundTag();
        tag.remove(TAG_GROWTH_QUALITY);

        int quality = getQuality(from);
        tag.putInt(TAG_QUALITY, quality);
        tag.putString(TAG_QUALITY_TIER, getQualityTierId(quality));

        LegacyItemTags.setTag(to, tag);
    }

    public static void setCutType(ItemStack stack, String cutType) {
        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);
        tag.putString(TAG_CUT_TYPE, cutType);
    }

    public static String getCutType(ItemStack stack) {
        if (stack.isEmpty()) return "";

        if (!LegacyItemTags.hasTag(stack)) {
            return isLooseTobacco(stack) ? CUT_RIBBON : "";
        }

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (!tag.contains(TAG_CUT_TYPE)) {
            return isLooseTobacco(stack) ? CUT_RIBBON : "";
        }

        String cutType = tag.getString(TAG_CUT_TYPE);
        if (cutType == null || cutType.isEmpty()) {
            return isLooseTobacco(stack) ? CUT_RIBBON : "";
        }

        return cutType;
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

    public static String getCureType(ItemStack stack) {
        if (stack.isEmpty()) return "";

        if (!LegacyItemTags.hasTag(stack)) {
            return isDryTobaccoLeaf(stack) || isLooseTobacco(stack) ? CURE_AIR : "";
        }

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (!tag.contains(TAG_CURE_TYPE)) {
            return isDryTobaccoLeaf(stack) || isLooseTobacco(stack) ? CURE_AIR : "";
        }

        String cureType = tag.getString(TAG_CURE_TYPE);
        if (cureType == null || cureType.isEmpty()) {
            return isDryTobaccoLeaf(stack) || isLooseTobacco(stack) ? CURE_AIR : "";
        }

        return cureType;
    }

    public static void applyCureData(ItemStack stack, String cureType, int quality) {
        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);
        tag.putString(TAG_CURE_TYPE, cureType);

        int clamped = clampQuality(quality);
        tag.putInt(TAG_QUALITY, clamped);
        tag.putString(TAG_QUALITY_TIER, getQualityTierId(clamped));
    }

    public static void ensureDefaultLeafData(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!isRawTobaccoLeaf(stack) && !isDryTobaccoLeaf(stack) && !isLooseTobacco(stack)) return;

        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);

        if (isRawTobaccoLeaf(stack)) {
            if (!tag.contains(TAG_GROWTH_QUALITY)) {
                tag.putInt(TAG_GROWTH_QUALITY, 50);
            }
            tag.remove(TAG_QUALITY);
            tag.remove(TAG_QUALITY_TIER);
            tag.remove(TAG_CURE_TYPE);
            return;
        }

        if (!tag.contains(TAG_QUALITY)) {
            tag.putInt(TAG_QUALITY, 75);
        }

        if (!tag.contains(TAG_QUALITY_TIER) || tag.getString(TAG_QUALITY_TIER).isEmpty()) {
            tag.putString(TAG_QUALITY_TIER, getQualityTierId(getQuality(stack)));
        }

        if (!tag.contains(TAG_CURE_TYPE) || tag.getString(TAG_CURE_TYPE).isEmpty()) {
            tag.putString(TAG_CURE_TYPE, CURE_AIR);
        }
    }

    public static int getCanonicalTierQuality(int quality) {
        int clamped = clampQuality(quality);

        return switch (getQualityTierId(clamped)) {
            case "poor" -> 15;
            case "common" -> 45;
            case "good" -> 70;
            case "excellent" -> 85;
            case "perfect" -> 95;
            default -> 110;
        };
    }

    public static int getQuality(ItemStack stack) {
        if (!LegacyItemTags.hasTag(stack)) {
            return isRawTobaccoLeaf(stack) ? 50 : 75;
        }

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag.contains(TAG_QUALITY)) {
            return clampQuality(tag.getInt(TAG_QUALITY));
        }
        if (tag.contains(TAG_GROWTH_QUALITY)) {
            return Math.max(0, Math.min(70, tag.getInt(TAG_GROWTH_QUALITY)));
        }
        return isRawTobaccoLeaf(stack) ? 50 : 75;
    }

    public static int buildFinalQuality(
            int growthQuality,
            String cureType,
            int interruptionCount,
            boolean mixedMethods,
            boolean properEnvironment,
            int randomBonus
    ) {
        int clampedGrowth = Math.max(0, Math.min(70, growthQuality));

        int curingBonus = switch (cureType) {
            case CURE_FLUE -> 10;
            case CURE_SUN -> 9;
            case CURE_FIRE -> 8;
            default -> 7;
        };

        if (properEnvironment) {
            curingBonus += 10;
        } else {
            curingBonus += 3;
        }

        if (interruptionCount == 0) {
            curingBonus += 10;
        } else {
            curingBonus -= Math.min(18, interruptionCount * 3);
        }

        if (mixedMethods) {
            curingBonus -= 8;
        }

        curingBonus += Math.max(0, Math.min(10, randomBonus));

        int finalQuality = clampedGrowth + Math.max(0, Math.min(30, curingBonus));
        return clampQuality(Math.min(100, finalQuality));
    }

    public static void applyCreativeLeafDefaults(ItemStack stack, boolean cured) {
        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);

        if (cured) {
            tag.putInt(TAG_QUALITY, 75);
            tag.putString(TAG_QUALITY_TIER, getQualityTierId(75));
            tag.putString(TAG_CURE_TYPE, CURE_AIR);
            tag.remove(TAG_GROWTH_QUALITY);
        } else {
            // Raw creative leaf should just be "Good"
            tag.putInt(TAG_GROWTH_QUALITY, 40);
            tag.remove(TAG_QUALITY);
            tag.remove(TAG_QUALITY_TIER);
            tag.remove(TAG_CURE_TYPE);
        }

        tag.remove(TAG_CUT_TYPE);
        tag.remove("Fermented");
        tag.remove("AgedStages");
        tag.remove("Ruined");
    }

    public static ItemStack makeCreativeLoose(ItemStack base, String cutType) {
        ItemStack stack = base.copy();
        stack.setCount(1);

        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);
        tag.putInt(TAG_QUALITY, 75);
        tag.putString(TAG_QUALITY_TIER, getQualityTierId(75));
        tag.putString(TAG_CURE_TYPE, CURE_AIR);
        tag.putString(TAG_CUT_TYPE, cutType);

        tag.remove("Fermented");
        tag.remove("AgedStages");
        tag.remove("Ruined");

        return stack;
    }

    public static int clampQuality(int quality) {
        return Math.max(0, Math.min(120, quality));
    }

    public static String getQualityTier(int quality) {
        int clamped = clampQuality(quality);
        if (clamped <= 30) return "Poor";
        if (clamped <= 60) return "Common";
        if (clamped <= 80) return "Good";
        if (clamped <= 89) return "Excellent";
        if (clamped <= 100) return "Perfect";
        return "Exceptional";
    }

    public static String getCureDisplayName(String cureType) {
        return switch (cureType) {
            case CURE_FIRE -> "Fire-Cured";
            case CURE_SUN -> "Sun-Cured";
            case CURE_FLUE -> "Flue-Cured";
            default -> "Air-Cured";
        };
    }

    public static void ensureDefaultTobaccoData(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!isRawTobaccoLeaf(stack) && !isDryTobaccoLeaf(stack) && !isLooseTobacco(stack)) return;

        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);

        if (isRawTobaccoLeaf(stack)) {
            if (!tag.contains(TAG_GROWTH_QUALITY)) {
                tag.putInt(TAG_GROWTH_QUALITY, 50);
            }
            tag.remove(TAG_QUALITY);
            tag.remove(TAG_QUALITY_TIER);
            tag.remove(TAG_CURE_TYPE);
            tag.remove(TAG_CUT_TYPE);
            return;
        }

        if (!tag.contains(TAG_QUALITY)) {
            tag.putInt(TAG_QUALITY, 75);
        }

        if (!tag.contains(TAG_QUALITY_TIER) || tag.getString(TAG_QUALITY_TIER).isEmpty()) {
            tag.putString(TAG_QUALITY_TIER, getQualityTierId(getQuality(stack)));
        }

        if (isDryTobaccoLeaf(stack) || isLooseTobacco(stack)) {
            if (!tag.contains(TAG_CURE_TYPE) || tag.getString(TAG_CURE_TYPE).isEmpty()) {
                tag.putString(TAG_CURE_TYPE, CURE_AIR);
            }
        }

        if (isLooseTobacco(stack)) {
            if (!tag.contains(TAG_CUT_TYPE) || tag.getString(TAG_CUT_TYPE).isEmpty()) {
                tag.putString(TAG_CUT_TYPE, CUT_RIBBON);
            }
        }
    }

    public static String getRawLeafTier(int growthQuality) {
        int clamped = Math.max(0, Math.min(70, growthQuality));
        if (clamped <= 15) return "Poor";
        if (clamped <= 30) return "Common";
        if (clamped <= 45) return "Good";
        if (clamped <= 59) return "Excellent";
        return "Perfect";
    }
}