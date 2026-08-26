package com.diggydwarff.tobacconistmod.compat;

import com.diggydwarff.tobacconistmod.compat.curios.CuriosSpectaclesHelper;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * Loader-safe lookup for the Tobacconist's Spectacles.
 * Curios is optional: vanilla HEAD is always supported, Curios head is checked only when present.
 */
public final class SpectaclesEquipmentHelper {
    private SpectaclesEquipmentHelper() {}

    public static ItemStack findWorn(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.is(ModItems.TOBACCONISTS_SPECTACLES.get())) {
            return head;
        }

        if (ModList.get().isLoaded("curios")) {
            return CuriosSpectaclesHelper.findWorn(player);
        }

        return ItemStack.EMPTY;
    }

    public static boolean isWearing(Player player) {
        return !findWorn(player).isEmpty();
    }
}
