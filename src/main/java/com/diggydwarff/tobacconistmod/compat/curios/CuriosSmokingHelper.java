package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** Server-side Curios integration kept separate so the optional Curios dependency is only touched when loaded. */
public final class CuriosSmokingHelper {
    private CuriosSmokingHelper() {}

    public static void trySmokeMouthItem(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            var mouth = inventory.getCurios().get("mouth");
            if (mouth == null || mouth.getStacks().getSlots() <= 0) return;

            ItemStack stack = mouth.getStacks().getStackInSlot(0);
            if (stack.isEmpty() || !(stack.getItem() instanceof SmokingItem smokingItem)) return;

            if (smokingItem.smokeFromMouthSlot(player, player.serverLevel(), stack)) {
                // Reinsert to notify Curios that NBT/damage/count changed and synchronize the slot.
                mouth.getStacks().setStackInSlot(0, stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
        });
    }
}
