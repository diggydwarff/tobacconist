package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChavetaItem extends Item {
    public ChavetaItem(Properties props) {
        super(props);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setDamageValue(copy.getDamageValue() + 1);

        if (copy.getDamageValue() >= copy.getMaxDamage()) {
            return ItemStack.EMPTY;
        }

        return copy;
    }
}