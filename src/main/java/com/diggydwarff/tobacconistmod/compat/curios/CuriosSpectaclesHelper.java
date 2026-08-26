package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** Curios-only spectacles lookup for the 1.20.1 Head slot. */
public final class CuriosSpectaclesHelper {
    private CuriosSpectaclesHelper() {}

    public static ItemStack findWorn(Player player) {
        return CuriosApi.getCuriosInventory(player).map(inventory -> {
            var head = inventory.getCurios().get("head");
            if (head == null || head.getStacks().getSlots() <= 0) return ItemStack.EMPTY;
            for (int i = 0; i < head.getStacks().getSlots(); i++) {
                ItemStack stack = head.getStacks().getStackInSlot(i);
                if (stack.is(ModItems.TOBACCONISTS_SPECTACLES.get())) return stack;
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }

    public static boolean isWearing(Player player) {
        return !findWorn(player).isEmpty();
    }
}
