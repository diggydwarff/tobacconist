package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BrassNameTagItem extends Item {
    public BrassNameTagItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static String getStampName(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasCustomHoverName()) return "";
        return stack.getHoverName().getString().trim();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level context, List<Component> tooltip, TooltipFlag flag) {
        String name = getStampName(stack);
        if (name.isEmpty()) {
            tooltip.add(Component.translatable("tobacconistmod.tooltip.rename_in_anvil").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tobacconistmod.tooltip.reusable_deployer_label", name).withStyle(ChatFormatting.GOLD));
        }
    }
}
