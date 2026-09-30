package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Rules shared by the named specialty tobacco processes. */
public final class TobaccoSpecialProcessingHelper {
    private TobaccoSpecialProcessingHelper() {}

    public static final int LATAKIA_SMOKE_TIME = 48000;
    public static final int DARK_FIRED_KENTUCKY_SMOKE_TIME = 42000;

    /**
     * Specialty rack finishes intentionally build on an already completed base cure.
     * Sun-cured Oriental can become Latakia; fire-cured Burley can become Dark Fired Kentucky.
     */
    public static String getRackSmokeFinishTarget(ItemStack stack) {
        if (!TobaccoCuringHelper.isDryTobaccoLeaf(stack)) return "";

        String cure = TobaccoCuringHelper.getCureType(stack);
        String variety = getVarietyId(stack);
        if ("oriental".equals(variety) && TobaccoCuringHelper.CURE_SUN.equals(cure)) {
            return TobaccoCuringHelper.CURE_LATAKIA;
        }
        if ("burley".equals(variety) && TobaccoCuringHelper.CURE_FIRE.equals(cure)) {
            return TobaccoCuringHelper.CURE_DARK_FIRED_KENTUCKY;
        }
        return "";
    }

    public static int getRackSmokeFinishTime(String targetCure) {
        return switch (targetCure) {
            case TobaccoCuringHelper.CURE_LATAKIA -> LATAKIA_SMOKE_TIME;
            case TobaccoCuringHelper.CURE_DARK_FIRED_KENTUCKY -> DARK_FIRED_KENTUCKY_SMOKE_TIME;
            default -> 0;
        };
    }

    public static void applyNamedCure(ItemStack stack, String targetCure) {
        if (stack.isEmpty() || targetCure == null || targetCure.isBlank()) return;
        TobaccoCuringHelper.applyCureData(stack, targetCure, TobaccoCuringHelper.getQuality(stack));
    }

    /**
     * Perique is a pressure-fermented whole-leaf treatment, not a synonym for generic
     * fermentation. Keeping it on intact air-cured Burley also leaves pressed Burley plugs free
     * to follow the separate Cavendish path.
     */
    public static boolean isPeriqueCandidate(ItemStack stack) {
        return TobaccoCuringHelper.isDryTobaccoLeaf(stack)
                && "burley".equals(getVarietyId(stack))
                && TobaccoCuringHelper.CURE_AIR.equals(TobaccoCuringHelper.getCureType(stack));
    }

    public static boolean isCavendishCandidate(ItemStack stack) {
        if (!TobaccoCuringHelper.isLooseTobacco(stack)) return false;
        if (!TobaccoCuringHelper.CUT_PLUG.equals(TobaccoCuringHelper.getCutType(stack))) return false;
        String cure = TobaccoCuringHelper.getCureType(stack);
        return !TobaccoCuringHelper.CURE_CAVENDISH.equals(cure)
                && !TobaccoCuringHelper.CURE_BLACK_CAVENDISH.equals(cure);
    }

    public static boolean isBlackCavendishCandidate(ItemStack stack) {
        return TobaccoCuringHelper.isLooseTobacco(stack)
                && TobaccoCuringHelper.CUT_PLUG.equals(TobaccoCuringHelper.getCutType(stack))
                && TobaccoCuringHelper.CURE_CAVENDISH.equals(TobaccoCuringHelper.getCureType(stack));
    }

    public static boolean isStovedVirginiaCandidate(ItemStack stack) {
        return isProcessedLeafOrLoose(stack)
                && "virginia".equals(getVarietyId(stack))
                && TobaccoCuringHelper.CURE_FLUE.equals(TobaccoCuringHelper.getCureType(stack))
                // Plug is reserved for the pressure/heat fermentation path into Cavendish.
                && !TobaccoCuringHelper.CUT_PLUG.equals(TobaccoCuringHelper.getCutType(stack));
    }

    /** Named cures that already communicate fermentation in the Cure field. */
    public static boolean isNamedFermentationCure(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String cure = TobaccoCuringHelper.getCureType(stack);
        return TobaccoCuringHelper.CURE_PERIQUE.equals(cure)
                || TobaccoCuringHelper.CURE_CAVENDISH.equals(cure)
                || TobaccoCuringHelper.CURE_BLACK_CAVENDISH.equals(cure);
    }

    public static boolean isHaunted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = LegacyItemTags.getTag(stack);
        return tag != null && containsHauntedCure(tag);
    }

    public static boolean containsHauntedCure(CompoundTag tag) {
        if (tag == null) return false;
        if (TobaccoCuringHelper.CURE_HAUNTED.equals(tag.getString(TobaccoCuringHelper.TAG_CURE_TYPE))) {
            return true;
        }
        for (TobaccoBlendComponent component : TobaccoBlendHelper.getComponentData(tag)) {
            if (TobaccoCuringHelper.CURE_HAUNTED.equals(component.cure())) return true;
        }
        if (tag.contains("PackedTobaccoData")
                && containsHauntedCure(tag.getCompound("PackedTobaccoData"))) {
            return true;
        }
        // Cigars preserve the wrapper independently from their filler snapshot. Haunted wrapper
        // leaf should therefore carry the same wisps even when the filler itself is ordinary.
        if (tag.contains("WrapperLeafData")
                && containsHauntedCure(tag.getCompound("WrapperLeafData"))) {
            return true;
        }
        return false;
    }

    public static String getVarietyId(ItemStack stack) {
        if (stack.is(ModItems.WILD_TOBACCO_LEAF.get()) || stack.is(ModItems.WILD_TOBACCO_LEAF_DRY.get())
                || stack.is(ModItems.TOBACCO_LOOSE_WILD.get())) return "wild";
        if (stack.is(ModItems.VIRGINIA_TOBACCO_LEAF.get()) || stack.is(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get())
                || stack.is(ModItems.TOBACCO_LOOSE_VIRGINIA.get())) return "virginia";
        if (stack.is(ModItems.BURLEY_TOBACCO_LEAF.get()) || stack.is(ModItems.BURLEY_TOBACCO_LEAF_DRY.get())
                || stack.is(ModItems.TOBACCO_LOOSE_BURLEY.get())) return "burley";
        if (stack.is(ModItems.ORIENTAL_TOBACCO_LEAF.get()) || stack.is(ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get())
                || stack.is(ModItems.TOBACCO_LOOSE_ORIENTAL.get())) return "oriental";
        if (stack.is(ModItems.DOKHA_TOBACCO_LEAF.get()) || stack.is(ModItems.DOKHA_TOBACCO_LEAF_DRY.get())
                || stack.is(ModItems.TOBACCO_LOOSE_DOKHA.get())) return "dokha";
        if (stack.is(ModItems.SHADE_TOBACCO_LEAF.get()) || stack.is(ModItems.SHADE_TOBACCO_LEAF_DRY.get())
                || stack.is(ModItems.TOBACCO_LOOSE_SHADE.get())) return "shade";
        return "";
    }

    private static boolean isProcessedLeafOrLoose(ItemStack stack) {
        return TobaccoCuringHelper.isDryTobaccoLeaf(stack) || TobaccoCuringHelper.isLooseTobacco(stack);
    }
}
