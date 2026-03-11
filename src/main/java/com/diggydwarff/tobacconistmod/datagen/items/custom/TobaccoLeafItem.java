package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TobaccoLeafItem extends Item {
    public TobaccoLeafItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = super.getName(stack);
        CompoundTag tag = stack.getTag();

        if (tag == null) {
            return baseName;
        }

        int quality = getDisplayedQuality(stack);
        if (quality <= 0) {
            return baseName;
        }

        String tier = capitalize(TobaccoCuringHelper.getQualityTier(quality));
        String cureType = tag.getString(TobaccoCuringHelper.TAG_CURE_TYPE);

        if (!cureType.isEmpty()) {
            return Component.literal(tier + " " + TobaccoCuringHelper.getCureDisplayName(cureType) + " ")
                    .append(baseName);
        }

        return Component.literal(tier + " ").append(baseName);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        int quality = getDisplayedQuality(stack);
        if (quality > 0) {
            tooltip.add(Component.literal(
                    "Quality: " + quality + " (" + capitalize(TobaccoCuringHelper.getQualityTier(quality)) + ")"
            ).withStyle(ChatFormatting.GRAY));
        }

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            String cureType = tag.getString(TobaccoCuringHelper.TAG_CURE_TYPE);
            if (!cureType.isEmpty()) {
                tooltip.add(Component.literal(
                        "Cure: " + TobaccoCuringHelper.getCureDisplayName(cureType)
                ).withStyle(ChatFormatting.GRAY));
            }

            if (TobaccoBarrelBlockEntity.isFermented(stack)) {
                tooltip.add(Component.literal("Fermented").withStyle(ChatFormatting.GOLD));
            }

            int agedDays = TobaccoBarrelBlockEntity.getAgedDays(stack);
            if (agedDays > 0) {
                tooltip.add(Component.literal(
                        "Age: " + formatAge(agedDays) + " (" + getAgeLabel(agedDays) + ")"
                ).withStyle(ChatFormatting.GOLD));
            }

            if (TobaccoBarrelBlockEntity.isRuined(stack)) {
                tooltip.add(Component.literal("Ruined").withStyle(ChatFormatting.DARK_RED));
            }
        }
    }

    private int getDisplayedQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }

        if (tag.contains(TobaccoCuringHelper.TAG_QUALITY)) {
            return tag.getInt(TobaccoCuringHelper.TAG_QUALITY);
        }

        if (tag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
            return tag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
        }

        return 0;
    }

    private String formatAge(int agedDays) {
        int years = agedDays / 365;
        int days = agedDays % 365;

        if (years > 0) {
            return years + "y " + days + "d";
        }
        return days + "d";
    }

    private String getAgeLabel(int agedDays) {
        if (agedDays < 7) return "Fresh";
        if (agedDays < 30) return "Light Aged";
        if (agedDays < 90) return "Deep Aged";
        if (agedDays < 365) return "Vintage";
        return "Cellared";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}