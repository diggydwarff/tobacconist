package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.ChatFormatting;
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
        String cureType = TobaccoCuringHelper.getCureType(stack);
        int quality = TobaccoCuringHelper.getQuality(stack);
        String tier = TobaccoCuringHelper.getQualityTier(quality);
        return Component.literal(tier + " " + TobaccoCuringHelper.getCureDisplayName(cureType) + " ")
                .append(super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        String cureType = TobaccoCuringHelper.getCureType(stack);
        int quality = TobaccoCuringHelper.getQuality(stack);

        tooltip.add(Component.literal("Cure: " + TobaccoCuringHelper.getCureDisplayName(cureType))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Quality: " + quality + " (" + TobaccoCuringHelper.getQualityTier(quality) + ")")
                .withStyle(ChatFormatting.GRAY));
    }
}
