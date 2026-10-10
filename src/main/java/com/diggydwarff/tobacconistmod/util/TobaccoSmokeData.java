package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record TobaccoSmokeData(
        String filler,
        String wrapper,
        int quality,
        boolean fermented,
        int agedDays,
        boolean blended,
        String blendName
) {
    private static final String TAG_TOBACCO = "tobacco";
    private static final String TAG_WRAPPER = "wrapper";

    public static TobaccoSmokeData from(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null) tag = new CompoundTag();

        List<String> varieties = blendVarieties(stack);
        String filler = varieties.isEmpty() ? fillerName(stack, tag) : String.join(" ", varieties);

        return new TobaccoSmokeData(
                lower(filler),
                lower(TobaccoTooltipHelper.cleanTobaccoName(tag.getString(TAG_WRAPPER))),
                TobaccoCuringHelper.getQuality(stack),
                TobaccoBarrelBlockEntity.isFermented(stack),
                TobaccoBarrelBlockEntity.getAgedDays(stack),
                !varieties.isEmpty(),
                lower(TobaccoBlendHelper.getSecretBlendName(stack))
        );
    }

    private static List<String> blendVarieties(ItemStack stack) {
        List<String> direct = TobaccoBlendHelper.getComponents(stack);
        if (!direct.isEmpty()) return direct;

        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        if (packed == null) return List.of();

        List<TobaccoBlendComponent> components = TobaccoBlendHelper.getComponentData(packed);
        List<String> varieties = new ArrayList<>(components.size());
        for (TobaccoBlendComponent component : components) {
            if (!component.variety().isBlank()) varieties.add(component.variety());
        }
        return varieties;
    }

    private static String fillerName(ItemStack stack, CompoundTag tag) {
        if (tag.contains(TAG_TOBACCO)) return tag.getString(TAG_TOBACCO);

        String variety = TobaccoBlendHelper.getVarietyId(stack);
        if (!variety.isEmpty()) return variety;

        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
