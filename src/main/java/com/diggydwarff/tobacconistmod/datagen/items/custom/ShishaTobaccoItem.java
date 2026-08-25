package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingProduct;
import com.diggydwarff.tobacconistmod.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShishaTobaccoItem extends SmokingProduct {

    public ShishaTobaccoItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        String label = TobaccoLabelHelper.getProductLabel(stack);
        if (!label.isEmpty()) {
            return TobaccoLabelHelper.buildNamedProduct(label, Component.translatable("item.tobacconistmod.shisha_tobacco"));
        }
        return super.getName(stack);
    }

    private int getDisplayQuality10(ItemStack stack) {
        int productQuality = TobaccoProductQualityHelper.getStoredProductQuality(stack);
        if (productQuality >= 0) {
            return productQuality;
        }

        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        if (packed != null) {
            int quality = packed.contains(TobaccoCuringHelper.TAG_QUALITY)
                    ? packed.getInt(TobaccoCuringHelper.TAG_QUALITY)
                    : packed.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)
                    ? packed.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY)
                    : 60;

            return Math.max(1, Math.round(quality / 10.0f));
        }

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag != null && tag.contains(TobaccoCuringHelper.TAG_QUALITY)) {
            return Math.max(1, Math.round(tag.getInt(TobaccoCuringHelper.TAG_QUALITY) / 10.0f));
        }

        return 6;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = LegacyItemTags.getTag(stack);

        String label = TobaccoLabelHelper.getProductLabel(stack);
        if (!label.isEmpty()) {
            tooltip.add(Component.translatable("tobacconistmod.ui.label", label).withStyle(ChatFormatting.YELLOW));
        }

        if (tag != null) {
            String tobacco = TobaccoTooltipHelper.cleanTobaccoName(tag.getString("tobacco"));
            if (!tobacco.isEmpty()) {
                tooltip.add(Component.translatable(
                        "tobacconistmod.tooltip.variety_summary",
                        TobaccoText.varietyFromStoredName(tobacco),
                        TobaccoTooltipHelper.getProcessSuffix(tag)
                ).withStyle(ChatFormatting.GOLD));
                int agedDays = TobaccoBarrelBlockEntity.getAgedDays(stack);
                if (agedDays > 0) {
                    tooltip.add(Component.translatable(
                            "tobacconistmod.ui.age_detailed",
                            TobaccoText.ageDuration(agedDays),
                            TobaccoText.ageLabel(agedDays)
                    ).withStyle(ChatFormatting.GOLD));
                }

                tooltip.add(Component.empty());
            }

            if (TobacconistConfig.isQualitySystemEnabled()) {
                int displayQuality = getDisplayQuality10(stack);
                if (displayQuality >= 0) {
                    tooltip.add(Component.translatable("tobacconistmod.ui.quality_10", displayQuality)
                            .withStyle(ChatFormatting.GRAY));
                }
            }

            CompoundTag normalized = tag.copy();

            if (!normalized.contains(TobaccoProductQualityHelper.TAG_INPUT_CUT_TYPE)
                    && !normalized.contains(TobaccoCuringHelper.TAG_CUT_TYPE)) {
                normalized.putString(TobaccoCuringHelper.TAG_CUT_TYPE, TobaccoCuringHelper.CUT_RIBBON);
            }

            if (!normalized.contains(TobaccoProductQualityHelper.TAG_INPUT_CURE_TYPE)
                    && !normalized.contains(TobaccoCuringHelper.TAG_CURE_TYPE)) {
                normalized.putString(TobaccoCuringHelper.TAG_CURE_TYPE, TobaccoCuringHelper.CURE_AIR);
            }

            ItemStack temp = new ItemStack(stack.getItem());
            LegacyItemTags.setTag(temp, normalized);

            String cutType = normalized.contains(TobaccoProductQualityHelper.TAG_INPUT_CUT_TYPE)
                    ? normalized.getString(TobaccoProductQualityHelper.TAG_INPUT_CUT_TYPE)
                    : TobaccoCuringHelper.getCutType(temp);

            if (!cutType.isEmpty()) {
                tooltip.add(Component.translatable("tobacconistmod.ui.cut", TobaccoText.cut(cutType))
                        .withStyle(ChatFormatting.GRAY));
            }

            String cureType = normalized.contains(TobaccoProductQualityHelper.TAG_INPUT_CURE_TYPE)
                    ? normalized.getString(TobaccoProductQualityHelper.TAG_INPUT_CURE_TYPE)
                    : TobaccoCuringHelper.getCureType(temp);

            if (!cureType.isEmpty()) {
                tooltip.add(Component.translatable("tobacconistmod.ui.cure", TobaccoText.cure(cureType))
                        .withStyle(ChatFormatting.GRAY));
            }

            if (TobaccoBarrelBlockEntity.isRuined(stack)) {
                tooltip.add(Component.translatable("tobacconistmod.ui.ruined").withStyle(ChatFormatting.DARK_RED));
            }

            for (String shishaFlavor : Arrays.asList(
                    tag.getString("flavor1"),
                    tag.getString("flavor2"),
                    tag.getString("flavor3")
            )) {
                String flavorName = getShishaFlavorDisplayName(shishaFlavor);
                if (!flavorName.isEmpty()) {
                    tooltip.add(Component.translatable("tobacconistmod.tooltip.flavor_bullet", TobaccoText.flavor(flavorName)).withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
        }

        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (flagIn.isAdvanced()) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tobacconistmod.ui.fermented_marker").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable("tobacconistmod.ui.age_legend").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /** Accepts current flavor IDs and legacy "Bottle of Molasses (...)" metadata. */
    private String getShishaFlavorDisplayName(String storedFlavor) {
        if (storedFlavor == null || storedFlavor.isBlank()) return "";

        if (storedFlavor.contains("Molasses")) {
            Matcher matcher = Pattern.compile("\\(([^)]+)\\)").matcher(storedFlavor);
            if (matcher.find()) {
                String legacy = matcher.group(1);
                return legacy.endsWith(" Flavored")
                        ? legacy.substring(0, legacy.length() - " Flavored".length())
                        : legacy;
            }
            return "";
        }

        return storedFlavor;
    }

}