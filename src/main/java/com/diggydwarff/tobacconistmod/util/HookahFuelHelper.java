package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HookahFuelHelper {

    public static float getMultiplier(ItemStack stack) {

        if (stack.is(ModItems.BAMBOO_CHARCOAL.get())) {
            return 5.0f;
        }

        if (stack.is(Items.CHARCOAL)) {
            return 2.5f;
        }

        if (stack.is(Items.COAL)) {
            return 0.5f;
        }

        return 0f;
    }

    public static boolean isFuel(ItemStack stack) {
        return getMultiplier(stack) > 0;
    }

}