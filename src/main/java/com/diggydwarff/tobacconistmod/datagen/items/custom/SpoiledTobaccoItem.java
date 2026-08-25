package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SpoiledTobaccoItem extends Item {

    public SpoiledTobaccoItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext level,
                                List<Component> tooltip,
                                TooltipFlag flag) {

        tooltip.add(Component.translatable("tobacconistmod.tooltip.spoiled_tobacco.useless"));
        tooltip.add(Component.translatable("tobacconistmod.tooltip.spoiled_tobacco.compost"));
    }
}