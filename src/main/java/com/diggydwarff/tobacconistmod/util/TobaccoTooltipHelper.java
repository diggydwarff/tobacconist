package com.diggydwarff.tobacconistmod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class TobaccoTooltipHelper {

    private TobaccoTooltipHelper() {}

    public static String getSuperscriptAge(int agedStages) {
        if (agedStages <= 0) return "";

        if (agedStages < 12) {
            return toSuperscriptNumber(agedStages) + "ᵐ";
        }

        if (agedStages % 12 == 0) {
            return toSuperscriptNumber(agedStages / 12) + "ʸ";
        }

        int years = agedStages / 12;
        int months = agedStages % 12;
        return toSuperscriptNumber(years) + "ʸ" + toSuperscriptNumber(months) + "ᵐ";
    }

    public static String toSuperscriptNumber(int num) {
        StringBuilder out = new StringBuilder();
        for (char c : String.valueOf(num).toCharArray()) {
            out.append(switch (c) {
                case '0' -> "⁰";
                case '1' -> "¹";
                case '2' -> "²";
                case '3' -> "³";
                case '4' -> "⁴";
                case '5' -> "⁵";
                case '6' -> "⁶";
                case '7' -> "⁷";
                case '8' -> "⁸";
                case '9' -> "⁹";
                default -> "";
            });
        }
        return out.toString();
    }

    public static String getQualityWord(int quality100) {
        if (quality100 >= 90) return "Premium";
        if (quality100 >= 75) return "Fine";
        if (quality100 >= 50) return "Standard";
        if (quality100 >= 25) return "Harsh";
        return "Poor";
    }

    public static String cleanTobaccoName(String name) {
        return name.replace(" Loose Tobacco", "")
                .replace(" Tobacco Leaf Wrapper", "")
                .replace(" Tobacco Leaf", "")
                .replace(" Tobacco", "")
                .replace("[", "")
                .replace("]", "")
                .trim();
    }

    public static boolean isFermented(CompoundTag tag) {
        return tag != null && tag.getBoolean("Fermented");
    }

    public static int getAgedStages(CompoundTag tag) {
        return tag == null ? 0 : tag.getInt("AgedStages");
    }

    public static String getProcessSuffix(CompoundTag tag) {
        if (tag == null) return "";

        boolean fermented = isFermented(tag);
        int agedStages = getAgedStages(tag);

        StringBuilder out = new StringBuilder();

        if (fermented) {
            out.append(" ✿");
        }

        String age = getSuperscriptAge(agedStages);
        if (!age.isEmpty()) {
            out.append(" ").append(age);
        }

        return out.toString();
    }

    public static CompoundTag getPackedTobaccoData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("PackedTobaccoData")) {
            return null;
        }
        return tag.getCompound("PackedTobaccoData");
    }

    public static String getPackedLeafName(ItemStack stack) {
        CompoundTag packed = getPackedTobaccoData(stack);
        if (packed == null) return "";

        String name = stack.getTag() != null ? stack.getTag().getString("tobacco") : "";
        if (name.isEmpty()) return "";

        String qualityWord = getQualityWord(
                packed.contains(TobaccoCuringHelper.TAG_QUALITY)
                        ? packed.getInt(TobaccoCuringHelper.TAG_QUALITY)
                        : 60
        );

        String base = cleanTobaccoName(name);

        String cureWord = TobaccoCuringHelper.getCureDisplayName(
                packed.contains(TobaccoCuringHelper.TAG_CURE_TYPE)
                        ? packed.getString(TobaccoCuringHelper.TAG_CURE_TYPE)
                        : TobaccoCuringHelper.CURE_AIR
        );

        String cutWord = TobaccoCuringHelper.getCutDisplayName(
                packed.contains(TobaccoCuringHelper.TAG_CUT_TYPE)
                        ? packed.getString(TobaccoCuringHelper.TAG_CUT_TYPE)
                        : ""
        );

        String prefix = qualityWord;
        if (!cureWord.isEmpty()) prefix += " " + cureWord;
        if (!cutWord.isEmpty() && !cutWord.equals("Uncut")) prefix += " " + cutWord;

        if (base.startsWith(prefix + " ")) {
            base = base.substring(prefix.length()).trim();
        }

        return cleanTobaccoName(base);
    }
}