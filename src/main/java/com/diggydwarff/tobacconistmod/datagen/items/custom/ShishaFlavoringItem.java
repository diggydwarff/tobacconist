package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ShishaFlavoringItem extends Item {

    public ShishaFlavoringItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (stack.isDamageableItem()) {
            int usesLeft = stack.getMaxDamage() - stack.getDamageValue();
            tooltip.add(Component.literal("Uses Left: " + usesLeft + "/4").withStyle(ChatFormatting.GRAY));
        }
    }

}
