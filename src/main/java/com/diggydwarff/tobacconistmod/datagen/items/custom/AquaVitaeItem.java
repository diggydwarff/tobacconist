package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Distilled extraction spirit used as the solvent for Tobacconist flavoring essences. */
public final class AquaVitaeItem extends Item {
    public AquaVitaeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tobacconistmod.tooltip.aqua_vitae")
                .withStyle(ChatFormatting.GRAY));
    }
}
