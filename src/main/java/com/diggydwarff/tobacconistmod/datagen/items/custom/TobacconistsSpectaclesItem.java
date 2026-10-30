package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import java.util.List;

/**
 * Inspection spectacles. Curios integration is optional; the standard Curios head slot is preferred and vanilla HEAD equipment remains the fallback.
 */
public class TobacconistsSpectaclesItem extends Item implements Wearable {
    public TobacconistsSpectaclesItem(Properties properties) {
        super(properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack previous = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!player.getAbilities().instabuild && net.minecraft.world.item.enchantment.EnchantmentHelper.hasBindingCurse(previous)) {
            return InteractionResultHolder.fail(held);
        }
        ItemStack equipped = held.copy();
        equipped.setCount(1);
        player.setItemSlot(EquipmentSlot.HEAD, equipped);
        held.shrink(1);
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        if (held.isEmpty()) return InteractionResultHolder.sidedSuccess(previous.copy(), level.isClientSide);
        if (!previous.isEmpty() && !player.getInventory().add(previous.copy())) player.drop(previous.copy(), false);
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (ModList.get().isLoaded("curios")) {
            tooltip.add(Component.translatable("tobacconistmod.tooltip.spectacles.curios_or_head").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tobacconistmod.tooltip.spectacles.head").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tobacconistmod.tooltip.spectacles.info").withStyle(ChatFormatting.DARK_GRAY));
    }
}
