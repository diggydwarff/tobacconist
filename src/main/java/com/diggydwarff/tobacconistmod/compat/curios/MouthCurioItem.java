package com.diggydwarff.tobacconistmod.compat.curios;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class MouthCurioItem implements ICurioItem {

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return context.identifier().equals("mouth");
    }

}