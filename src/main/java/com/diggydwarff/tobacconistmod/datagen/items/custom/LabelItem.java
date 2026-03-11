package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LabelItem extends Item {

    public static final String TAG_LABEL_NAME = "LabelName";

    public LabelItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return super.getDefaultInstance();
    }

    public static String getLabelName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_LABEL_NAME)) {
            return tag.getString(TAG_LABEL_NAME);
        }
        if (stack.hasCustomHoverName()) {
            return stack.getHoverName().getString();
        }
        return "";
    }

    public static void normalizeLabel(ItemStack stack) {

        if (!stack.hasCustomHoverName()) return;

        String name = stack.getHoverName().getString();

        // store our clean label
        stack.getOrCreateTag().putString(TAG_LABEL_NAME, name);

        // remove the italic vanilla name
        stack.resetHoverName();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) return;

        if (stack.hasCustomHoverName()) {
            normalizeLabel(stack);
        }
    }

    public static void setLabelName(ItemStack stack, String name) {
        if (name == null || name.isBlank()) return;
        stack.getOrCreateTag().putString(TAG_LABEL_NAME, name.trim());
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String name = getLabelName(stack);
        if (!name.isEmpty()) {
            tooltip.add(Component.literal(name).withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.literal("Rename in an anvil").withStyle(ChatFormatting.GRAY));
        }
    }
}