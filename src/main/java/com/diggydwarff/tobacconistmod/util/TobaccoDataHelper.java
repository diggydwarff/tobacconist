package com.diggydwarff.tobacconistmod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class TobaccoDataHelper {

    public static void applyTobaccoMetadata(ItemStack result, ItemStack source) {
        if (result.isEmpty() || source.isEmpty()) return;

        CompoundTag resultTag = result.getOrCreateTag();
        CompoundTag sourceTag = source.getTag();

        if (sourceTag == null) return;

        // Normalize TobaccoType
        String type = sourceTag.getString("TobaccoType");
        if (!type.isEmpty()) {
            resultTag.putString("TobaccoType", type);
        }

        // Core fields
        if (sourceTag.contains("CureType")) {
            resultTag.putString("CureType", sourceTag.getString("CureType"));
        }

        if (sourceTag.contains("CutType")) {
            resultTag.putString("CutType", sourceTag.getString("CutType"));
        }

        if (sourceTag.contains("Quality")) {
            resultTag.putInt("Quality", sourceTag.getInt("Quality"));
        }

        if (sourceTag.contains("QualityTier")) {
            resultTag.putString("QualityTier", sourceTag.getString("QualityTier"));
        }

        // Packed data copy (for deep reference)
        CompoundTag packed = sourceTag.copy();
        if (!packed.contains("TobaccoType") && !type.isEmpty()) {
            packed.putString("TobaccoType", type);
        }

        resultTag.put("PackedTobaccoData", packed);
    }

}
