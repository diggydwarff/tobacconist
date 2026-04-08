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
        TobaccoCuringHelper.ensureDefaultTobaccoData(stack);
        Component baseName = super.getName(stack);

        int quality = TobaccoCuringHelper.getQuality(stack);
        String tier = capitalize(TobaccoCuringHelper.getQualityTier(quality));
        String cureType = TobaccoCuringHelper.getCureType(stack);

        if (!cureType.isEmpty()) {
            return Component.literal(tier + " " + TobaccoCuringHelper.getCureDisplayName(cureType) + " ")
                    .append(baseName);
        }

        return Component.literal(tier + " ").append(baseName);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TobaccoCuringHelper.ensureDefaultTobaccoData(stack);
        super.appendHoverText(stack, level, tooltip, flag);

        int quality = TobaccoCuringHelper.getQuality(stack);
        tooltip.add(Component.literal(
                "Quality: " + quality + " (" + capitalize(TobaccoCuringHelper.getQualityTier(quality)) + ")"
        ).withStyle(ChatFormatting.GRAY));

        String cureType = TobaccoCuringHelper.getCureType(stack);
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