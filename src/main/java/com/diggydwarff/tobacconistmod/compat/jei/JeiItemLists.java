package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.custom.WoodenSmokingPipeItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.StreamSupport;

public final class JeiItemLists {

    private JeiItemLists() {}

    public static List<ItemStack> getAllSmokingPipes() {
        return StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
                .filter(item -> item instanceof WoodenSmokingPipeItem)
                .map(ItemStack::new)
                .toList();
    }
}