package com.diggydwarff.tobacconistmod.datagen.items.custom;

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

            if (tag.getBoolean("Fermented")) {
                tooltip.add(Component.literal("Fermented").withStyle(ChatFormatting.GOLD));
            }

            int aged = tag.getInt("AgedStages");
            if (aged > 0) {
                tooltip.add(Component.literal("Aged: " + aged).withStyle(ChatFormatting.GOLD));
            }

            if (tag.getBoolean("Ruined")) {
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

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}