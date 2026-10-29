package com.diggydwarff.tobacconistmod.backport;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public record PotionContents(Potion potion) {
    public static final PotionContents EMPTY = new PotionContents(Potions.EMPTY);
    public boolean is(Potion expected) { return potion == expected; }
    public static ItemStack createItemStack(Item item, Potion potion) {
        return PotionUtils.setPotion(new ItemStack(item), potion);
    }
}
