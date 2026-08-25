package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag != null && tag.contains(TAG_LABEL_NAME)) {
            return tag.getString(TAG_LABEL_NAME);
        }
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            return stack.getHoverName().getString();
        }
        return "";
    }

    public static void normalizeLabel(ItemStack stack) {

        if (!stack.has(DataComponents.CUSTOM_NAME)) return;

        String name = stack.getHoverName().getString();

        // Store the normalized label text.
        LegacyItemTags.getOrCreateTag(stack).putString(TAG_LABEL_NAME, name);

        // remove the italic vanilla name
        stack.remove(DataComponents.CUSTOM_NAME);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) return;

        if (stack.has(DataComponents.CUSTOM_NAME)) {
            normalizeLabel(stack);
        }
    }

    public static void setLabelName(ItemStack stack, String name) {
        if (name == null || name.isBlank()) return;
        LegacyItemTags.getOrCreateTag(stack).putString(TAG_LABEL_NAME, name.trim());
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        String name = getLabelName(stack);
        if (!name.isEmpty()) {
            tooltip.add(Component.literal(name).withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("tobacconistmod.tooltip.rename_in_anvil").withStyle(ChatFormatting.GRAY));
        }
    }
}