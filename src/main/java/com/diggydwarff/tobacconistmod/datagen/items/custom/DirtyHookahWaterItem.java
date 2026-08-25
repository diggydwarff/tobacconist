package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Hookah water that applies Nausea on a hose draw until replaced with fresh water. */
public class DirtyHookahWaterItem extends Item {
    public DirtyHookahWaterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.sidedSuccess(new ItemStack(Items.GLASS_BOTTLE), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Used hookah water").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Smoking through it causes brief Nausea").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.literal("Right-click to empty the bottle").withStyle(ChatFormatting.DARK_GRAY));
    }
}
