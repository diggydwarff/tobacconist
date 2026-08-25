package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelMode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

/** Translation-backed text for tobacco metadata shown to players. */
public final class TobaccoText {
    private TobaccoText() {}

    public static MutableComponent qualityTier(int quality) {
        return Component.translatable("tobacconistmod.quality_tier." + TobaccoCuringHelper.getQualityTierId(quality));
    }

    public static MutableComponent rawLeafTier(int growthQuality) {
        int clamped = Math.max(0, Math.min(70, growthQuality));
        String id;
        if (clamped <= 15) id = "poor";
        else if (clamped <= 30) id = "common";
        else if (clamped <= 45) id = "good";
        else if (clamped <= 59) id = "excellent";
        else id = "perfect";
        return Component.translatable("tobacconistmod.quality_tier." + id);
    }

    public static MutableComponent qualityDescriptor(int quality) {
        String id;
        if (quality >= 90) id = "premium";
        else if (quality >= 75) id = "fine";
        else if (quality >= 50) id = "standard";
        else if (quality >= 25) id = "harsh";
        else id = "poor";
        return Component.translatable("tobacconistmod.quality_descriptor." + id);
    }

    public static MutableComponent qualityTierFromDisplay(String value) {
        String id = normalize(value);
        return Component.translatable("tobacconistmod.quality_tier." + switch (id) {
            case "poor", "common", "good", "excellent", "perfect", "exceptional" -> id;
            default -> "unknown";
        });
    }

    public static MutableComponent qualityThreshold(String value) {
        String id = normalize(value);
        return Component.translatable("tobacconistmod.quality_threshold." + switch (id) {
            case "good_or_better", "excellent_or_better", "perfect_or_better" -> id;
            default -> "unknown";
        });
    }

    public static MutableComponent variety(String variety) {
        String id = normalize(variety);
        return Component.translatable("tobacconistmod.variety." + switch (id) {
            case "wild", "virginia", "burley", "oriental", "dokha", "shade" -> id;
            default -> "unknown";
        });
    }

    public static MutableComponent varietyFromStoredName(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return Component.translatable("tobacconistmod.variety.unknown");
        }
        String lower = storedName.toLowerCase(Locale.ROOT);
        for (String id : new String[]{"virginia", "burley", "oriental", "dokha", "shade", "wild"}) {
            if (lower.contains(id)) return variety(id);
        }
        return Component.translatable("tobacconistmod.variety.unknown");
    }

    public static MutableComponent cure(String cureType) {
        String normalized = normalize(cureType);
        String id = switch (normalized) {
            case TobaccoCuringHelper.CURE_FIRE, "fire_cured" -> "fire";
            case TobaccoCuringHelper.CURE_SUN, "sun_cured" -> "sun";
            case TobaccoCuringHelper.CURE_FLUE, "flue_cured" -> "flue";
            case TobaccoCuringHelper.CURE_MIXED, "mixed_cure" -> "mixed";
            default -> "air";
        };
        return Component.translatable("tobacconistmod.cure." + id);
    }

    public static MutableComponent cut(String cutType) {
        String normalized = normalize(cutType);
        String id = switch (normalized) {
            case TobaccoCuringHelper.CUT_RIBBON, "ribbon_cut" -> "ribbon";
            case TobaccoCuringHelper.CUT_SHAG, "shag_cut" -> "shag";
            case TobaccoCuringHelper.CUT_ROUGH, "rough_cut" -> "rough";
            case TobaccoCuringHelper.CUT_FLAKE, "flake_cut" -> "flake";
            default -> "uncut";
        };
        return Component.translatable("tobacconistmod.cut." + id);
    }

    public static MutableComponent ageLabel(int agedDays) {
        String id;
        if (agedDays < 7) id = "fresh";
        else if (agedDays < 30) id = "light_aged";
        else if (agedDays < 90) id = "deep_aged";
        else if (agedDays < 365) id = "vintage";
        else id = "cellared";
        return Component.translatable("tobacconistmod.age." + id);
    }

    public static MutableComponent ageDuration(int days) {
        int safeDays = Math.max(0, days);
        if (safeDays >= 365) {
            return Component.translatable("tobacconistmod.time.years_days", safeDays / 365, safeDays % 365);
        }
        return Component.translatable("tobacconistmod.time.days", safeDays);
    }

    public static MutableComponent barrelMode(TobaccoBarrelMode mode) {
        String id = switch (mode) {
            case FERMENTING -> "fermenting";
            case AGING -> "aging";
            default -> "idle";
        };
        return Component.translatable("tobacconistmod.barrel.mode." + id);
    }

    public static MutableComponent condition(int environmentScore) {
        String id;
        if (environmentScore >= 18) id = "excellent";
        else if (environmentScore >= 10) id = "good";
        else if (environmentScore >= 0) id = "fair";
        else id = "poor";
        return Component.translatable("tobacconistmod.condition." + id);
    }

    public static MutableComponent yesNo(boolean value) {
        return Component.translatable(value ? "tobacconistmod.ui.yes" : "tobacconistmod.ui.no");
    }

    public static MutableComponent flavor(String value) {
        String id = TobaccoAromaticHelper.normalizeFlavorId(value);
        if (id.isEmpty() || id.equals("plain")) {
            return Component.translatable("tobacconistmod.flavor.molasses");
        }
        if (id.equals(TobaccoAromaticHelper.FLAVOR_MIXED) || id.equals("mixed_aromatic")) {
            return Component.translatable("tobacconistmod.flavor.mixed");
        }
        for (var flavor : com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors.values()) {
            if (TobaccoAromaticHelper.getFlavorId(flavor).equals(id)
                    || normalize(TobaccoAromaticHelper.getFlavorDisplayName(flavor)).equals(id)) {
                return Component.translatable("tobacconistmod.flavor." + TobaccoAromaticHelper.getFlavorId(flavor));
            }
        }
        return Component.literal(TobaccoAromaticHelper.formatFlavorId(value));
    }

    public static MutableComponent state(String value) {
        String id = normalize(value);
        return Component.translatable("tobacconistmod.state." + switch (id) {
            case "tobacco", "raw_leaf", "dry_leaf", "loose_tobacco", "cigarette", "cigar", "shisha",
                    "tobacco_box", "blended", "aromatic", "fermented", "aged", "ruined" -> id;
            default -> "unknown";
        });
    }

    public static MutableComponent product(String value) {
        String id = normalize(value);
        return Component.translatable("tobacconistmod.product." + switch (id) {
            case "tobacco_box", "loose_tobacco", "cigarette", "cigar", "shisha", "cigarettes", "cigars",
                    "blend", "empty", "other" -> id;
            default -> "other";
        });
    }

    public static MutableComponent processedDescription(int quality, String cure, String cut, String variety,
                                                        String processSuffix) {
        return Component.translatable(
                "tobacconistmod.tooltip.tobacco_description",
                qualityDescriptor(quality),
                cure(cure),
                cut(cut),
                varietyFromStoredName(variety),
                processSuffix == null ? "" : processSuffix
        );
    }

    public static MutableComponent leafDescription(int quality, String cure, String variety, String processSuffix) {
        return Component.translatable(
                "tobacconistmod.tooltip.leaf_description",
                qualityDescriptor(quality),
                cure(cure),
                varietyFromStoredName(variety),
                processSuffix == null ? "" : processSuffix
        );
    }

    public static MutableComponent blendComponent(String variety, Integer quality, String cure, Component flavor) {
        Component qualityPart = quality == null
                ? Component.empty()
                : Component.translatable("tobacconistmod.tooltip.segment.quality", quality);
        Component curePart = cure == null || cure.isBlank()
                ? Component.empty()
                : Component.translatable("tobacconistmod.tooltip.segment.value", cure(cure));
        Component flavorPart = flavor == null
                ? Component.empty()
                : Component.translatable("tobacconistmod.tooltip.segment.value", flavor);
        return Component.translatable(
                "tobacconistmod.tooltip.blend_component",
                variety(variety), qualityPart, curePart, flavorPart
        );
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
