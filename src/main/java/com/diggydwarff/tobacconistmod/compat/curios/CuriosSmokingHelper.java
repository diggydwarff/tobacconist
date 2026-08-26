package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** Server-side Curios integration kept separate so the optional Curios dependency is only touched when loaded. */
public final class CuriosSmokingHelper {
    private CuriosSmokingHelper() {}

    /**
     * Attempts one deliberate puff. Returns {@code null} on success or a short
     * player-facing failure message when no puff could be taken.
     */
    public static Component trySmokeMouthItem(ServerPlayer player) {
        final Component[] result = {Component.translatable("tobacconistmod.message.mouth.no_slot")};

        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            var mouth = inventory.getCurios().get("mouth");
            if (mouth == null || mouth.getStacks().getSlots() <= 0) {
                result[0] = Component.translatable("tobacconistmod.message.mouth.no_slot");
                return;
            }

            ItemStack stack = mouth.getStacks().getStackInSlot(0);
            if (stack.isEmpty()) {
                result[0] = Component.translatable("tobacconistmod.message.mouth.empty");
                return;
            }
            if (!(stack.getItem() instanceof SmokingItem smokingItem)) {
                result[0] = Component.translatable("tobacconistmod.message.mouth.not_smokable");
                return;
            }

            if (!smokingItem.smokeFromMouthSlot(player, player.serverLevel(), stack)) {
                result[0] = Component.translatable("tobacconistmod.message.mouth.no_puffs");
                return;
            }

            // Reinsert to notify Curios that NBT/damage/count changed and synchronize the slot.
            mouth.getStacks().setStackInSlot(0, stack.isEmpty() ? ItemStack.EMPTY : stack);
            result[0] = null;
        });

        return result[0];
    }
}
