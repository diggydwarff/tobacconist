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
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TobacconistsSpectaclesItem extends Item implements Equipable {
    public TobacconistsSpectaclesItem(Properties properties) { super(properties); }

    @Override
    public EquipmentSlot getEquipmentSlot() { return EquipmentSlot.HEAD; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.isEmpty()) return InteractionResultHolder.pass(held);
        if (!level.isClientSide) {
            ItemStack equipped = held.copy();
            equipped.setCount(1);
            player.setItemSlot(EquipmentSlot.HEAD, equipped);
            if (!player.getAbilities().instabuild) held.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal(ModList.get().isLoaded("curios")
                ? "Wear in the Curios Eyes slot or helmet slot" : "Wear in the helmet slot")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Reveals tobacco growth and processing information")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
