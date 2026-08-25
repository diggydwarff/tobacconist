package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.compat.patchouli.PatchouliCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.List;

/**
 * Opens the Tobacconist manual when Patchouli is installed.
 */
public class TobaccoGuideItem extends Item {
    public TobaccoGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (ModList.get().isLoaded("patchouli") && player instanceof ServerPlayer serverPlayer) {
                PatchouliCompat.openManual(serverPlayer);
            } else {
                player.displayClientMessage(
                        Component.literal("Patchouli is required to open the Tobacconist's Manual.")
                                .withStyle(ChatFormatting.RED),
                        true
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (ModList.get().isLoaded("patchouli")) {
            tooltip.add(Component.literal("Right-click to open the manual").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Requires Patchouli to open").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
