package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TobacconistsSpectaclesItem extends Item {
    public TobacconistsSpectaclesItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Wear in the Curios Eyes slot").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Reveals tobacco growth and processing information").withStyle(ChatFormatting.DARK_GRAY));
    }
}
