package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** Server-side Curios integration kept isolated so Curios can remain optional. */
public final class CuriosSmokingHelper {
    private CuriosSmokingHelper() {}

    public static String trySmokeMouthItem(ServerPlayer player) {
        final String[] result = {"No Curios Mouth slot is available."};
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            var mouth = inventory.getCurios().get("mouth");
            if (mouth == null || mouth.getStacks().getSlots() <= 0) return;
            ItemStack stack = mouth.getStacks().getStackInSlot(0);
            if (stack.isEmpty()) { result[0] = "No item is equipped in your Mouth slot."; return; }
            if (!(stack.getItem() instanceof SmokingItem smokingItem)) {
                result[0] = "The item in your Mouth slot cannot be smoked."; return;
            }
            if (!smokingItem.smokeFromMouthSlot(player, player.serverLevel(), stack)) {
                result[0] = "That item has no puffs left."; return;
            }
            mouth.getStacks().setStackInSlot(0, stack.isEmpty() ? ItemStack.EMPTY : stack);
            result[0] = null;
        });
        return result[0];
    }
}
