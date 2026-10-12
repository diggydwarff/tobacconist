package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.SmokingDraws;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class HookahHoseItem extends Item {
    public HookahHoseItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return SmokingDraws.duration(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            SmokingDraws.drawSound(level, entity, stack, getUseDuration(stack, entity) - remainingUseDuration);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player && player.isAlive()) SmokingDraws.finishHookah(player, stack, getUseDuration(stack, entity));
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof ServerPlayer player && player.isAlive()) {
            SmokingDraws.finishHookah(player, stack, getUseDuration(stack, entity) - timeLeft);
        }
    }

    protected String hoseTooltipKey() {
        return "tobacconistmod.tooltip.hose.basic";
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn){
        tooltip.add(Component.translatable(hoseTooltipKey()).withStyle(ChatFormatting.AQUA));

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    };

}
