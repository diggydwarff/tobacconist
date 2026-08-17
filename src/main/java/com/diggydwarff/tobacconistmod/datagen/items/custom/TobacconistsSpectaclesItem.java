package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.List;

/**
 * Inspection spectacles. Curios integration is optional; the standard Curios head slot is preferred and vanilla HEAD equipment remains the fallback.
 */
public class TobacconistsSpectaclesItem extends Item implements Equipable {
    public TobacconistsSpectaclesItem(Properties properties) {
        super(properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (ModList.get().isLoaded("curios")) {
            tooltip.add(Component.literal("Wear in the Curios Head slot or helmet slot").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Wear in the helmet slot").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.literal("Reveals tobacco growth and processing information").withStyle(ChatFormatting.DARK_GRAY));
    }
}
