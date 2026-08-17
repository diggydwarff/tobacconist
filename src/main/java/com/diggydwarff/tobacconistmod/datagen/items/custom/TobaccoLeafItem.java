package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
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
        boolean qualityEnabled = TobacconistConfig.isQualitySystemEnabled();

        if (TobaccoCuringHelper.isDryTobaccoLeaf(stack)) {
            String cureType = TobaccoCuringHelper.getCureType(stack);
            String prefix = "";
            if (qualityEnabled) {
                prefix = TobaccoCuringHelper.getQualityTier(TobaccoCuringHelper.getQuality(stack));
            }
            if (!cureType.isEmpty()) {
                if (!prefix.isEmpty()) prefix += " ";
                prefix += TobaccoCuringHelper.getCureDisplayName(cureType);
            }
            return prefix.isEmpty() ? baseName : Component.literal(prefix + " ").append(baseName);
        }

        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack) && qualityEnabled) {
            return Component.literal(TobaccoCuringHelper.getRawLeafTier(getRawGrowthQuality(stack)) + " ").append(baseName);
        }
        return baseName;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack)) {
            if (TobacconistConfig.isQualitySystemEnabled()) {
                int growth = getRawGrowthQuality(stack);
                tooltip.add(Component.literal("Growth Quality: " + growth + " (" + TobaccoCuringHelper.getRawLeafTier(growth) + ")")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            if (TobacconistConfig.isQualitySystemEnabled()) {
                int quality = TobaccoCuringHelper.getQuality(stack);
                tooltip.add(Component.literal("Quality: " + quality + " (" + TobaccoCuringHelper.getQualityTier(quality) + ")")
                        .withStyle(ChatFormatting.GRAY));
            }
            String cureType = TobaccoCuringHelper.getCureType(stack);
            if (!cureType.isEmpty()) {
                tooltip.add(Component.literal("Cure: " + TobaccoCuringHelper.getCureDisplayName(cureType))
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        if (TobaccoBarrelBlockEntity.isFermented(stack)) tooltip.add(Component.literal("Fermented").withStyle(ChatFormatting.GOLD));
        int agedDays = TobaccoBarrelBlockEntity.getAgedDays(stack);
        if (agedDays > 0) tooltip.add(Component.literal("Age: " + formatAge(agedDays) + " (" + getAgeLabel(agedDays) + ")").withStyle(ChatFormatting.GOLD));
        if (TobaccoBarrelBlockEntity.isRuined(stack)) tooltip.add(Component.literal("Ruined").withStyle(ChatFormatting.RED));
    }

    private int getRawGrowthQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
            return Math.max(0, Math.min(70, tag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY)));
        }
        return 50;
    }

    private String formatAge(int agedDays) {
        int years = agedDays / 365;
        int days = agedDays % 365;
        return years > 0 ? years + "y " + days + "d" : days + "d";
    }

    private String getAgeLabel(int agedDays) {
        if (agedDays < 7) return "Fresh";
        if (agedDays < 30) return "Light Aged";
        if (agedDays < 90) return "Deep Aged";
        if (agedDays < 365) return "Vintage";
        return "Cellared";
    }
}
