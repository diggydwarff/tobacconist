package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
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
    public static String trySmokeMouthItem(ServerPlayer player) {
        final String[] result = {"No Curios Mouth slot is available."};

        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            var mouth = inventory.getCurios().get("mouth");
            if (mouth == null || mouth.getStacks().getSlots() <= 0) {
                result[0] = "No Curios Mouth slot is available.";
                return;
            }

            ItemStack stack = mouth.getStacks().getStackInSlot(0);
            if (stack.isEmpty()) {
                result[0] = "No item is equipped in your Mouth slot.";
                return;
            }
            if (!(stack.getItem() instanceof SmokingItem smokingItem)) {
                result[0] = "The item in your Mouth slot cannot be smoked.";
                return;
            }

            if (!smokingItem.smokeFromMouthSlot(player, player.serverLevel(), stack)) {
                result[0] = "That item has no puffs left.";
                return;
            }

            // Reinsert to notify Curios that NBT/damage/count changed and synchronize the slot.
            mouth.getStacks().setStackInSlot(0, stack.isEmpty() ? ItemStack.EMPTY : stack);
            result[0] = null;
        });

        return result[0];
    }
}
