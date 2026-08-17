package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosSpectaclesHelper {
    private CuriosSpectaclesHelper() {}

    public static boolean isWearing(Player player) {
        return CuriosApi.getCuriosInventory(player).map(inventory -> {
            var eyes = inventory.getCurios().get("eyes");
            if (eyes == null || eyes.getStacks().getSlots() <= 0) return false;

            for (int i = 0; i < eyes.getStacks().getSlots(); i++) {
                ItemStack stack = eyes.getStacks().getStackInSlot(i);
                if (stack.is(ModItems.TOBACCONISTS_SPECTACLES.get())) return true;
            }
            return false;
        }).orElse(false);
    }
}
