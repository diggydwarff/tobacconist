package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** A concentrated alcohol-based flavor extract. Each bottle is consumed in a single use. */
public final class FlavoringEssenceItem extends Item {
    public FlavoringEssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tobacconistmod.tooltip.flavoring_essence")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
}
