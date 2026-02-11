package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class UpgradedHookahHoseItem extends Item {
    public UpgradedHookahHoseItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
        tooltip.add(Component.literal("Upgraded Hookah Hose").withStyle(ChatFormatting.AQUA));

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    };

}
