package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class PaintingTabHelper {

    public static ItemStack paintingVariant(String id) {
        ItemStack stack = new ItemStack(Items.PAINTING);

        CompoundTag entityData = new CompoundTag();
        entityData.putString("id", "minecraft:painting");
        entityData.putString("variant", ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, id).toString());
        CustomData.set(DataComponents.ENTITY_DATA, stack, entityData);

        return stack;
    }
}
