package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PaintingTabHelper {

    public static ItemStack paintingVariant(String id) {
        ItemStack stack = new ItemStack(Items.PAINTING);

        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("variant", new ResourceLocation(TobacconistMod.MODID, id).toString());

        CompoundTag tag = stack.getOrCreateTag();
        tag.put("EntityTag", entityTag);

        return stack;
    }
}