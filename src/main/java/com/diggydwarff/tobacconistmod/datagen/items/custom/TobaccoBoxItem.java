package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TobaccoBoxItem extends Item {

    private static final String TAG_USE_COOLDOWN = "UseCooldown";

    public TobaccoBoxItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack stored = TobaccoBoxHelper.getStoredItem(stack);
        String label = TobaccoBoxHelper.getLabel(stack);

        if (stored.isEmpty()) {
            if (!label.isEmpty()) {
                return Component.translatable("tobacconistmod.box.named_empty", label);
            }
            return Component.translatable("item.tobacconistmod.tobacco_box");
        }

        Component typeName = TobaccoBoxHelper.getContentPluralComponent(stored);
        if (!label.isEmpty()) {
            return Component.translatable("tobacconistmod.box.named", label, typeName);
        }
        return Component.translatable("tobacconistmod.box.of", typeName);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;

        int cooldown = LegacyItemTags.getOrCreateTag(stack).getInt(TAG_USE_COOLDOWN);
        if (cooldown > 0) {
            LegacyItemTags.getOrCreateTag(stack).putInt(TAG_USE_COOLDOWN, cooldown - 1);
        }

        ItemStack offhand = player.getOffhandItem();
        ItemStack mainhand = player.getMainHandItem();

        if (offhand != stack) return;
        if (!mainhand.isEmpty()) return;
        if (!player.swinging && !player.isUsingItem()) return;
        if (LegacyItemTags.getOrCreateTag(stack).getInt(TAG_USE_COOLDOWN) > 0) return;

        ItemStack extracted = tryExtract(stack, player);
        if (!extracted.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, extracted);
            LegacyItemTags.getOrCreateTag(stack).putInt(TAG_USE_COOLDOWN, 6);
        }
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack box, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;

        ItemStack other = slot.getItem();
        if (other.isEmpty()) {
            ItemStack candidate = getExtractableItem(box);
            if (candidate.isEmpty() || !slot.mayPlace(candidate) || slot.getMaxStackSize(candidate) < 1) {
                return false;
            }
            if (!slot.safeInsert(candidate).isEmpty()) return false;
            tryExtract(box, player);
            return true;
        }

        if (tryInsert(box, other, player)) {
            slot.set(other);
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack box, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;
        if (other.isEmpty()) return false;

        if (tryInsert(box, other, player)) {
            access.set(other);
            return true;
        }

        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack box = player.getItemInHand(hand);
        ItemStack otherHand = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

        if (!otherHand.isEmpty() && TobaccoBoxHelper.isSupportedContent(otherHand)) {
            if (!level.isClientSide) {
                tryInsert(box, otherHand, player);
            }
            return InteractionResultHolder.sidedSuccess(box, level.isClientSide);
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ItemStack extracted = tryExtract(box, player);
                if (!extracted.isEmpty()) {
                    if (!player.addItem(extracted)) {
                        player.drop(extracted, false);
                    }
                }
            }
            return InteractionResultHolder.sidedSuccess(box, level.isClientSide);
        }

        return InteractionResultHolder.pass(box);
    }

    private boolean tryInsert(ItemStack box, ItemStack incoming, Player player) {
        int moved = TobaccoBoxHelper.insert(box, incoming, 1);
        if (moved == 0) return false;
        incoming.shrink(moved);
        playSound(player);
        return true;
    }

    private ItemStack getExtractableItem(ItemStack box) {
        ItemStack stored = TobaccoBoxHelper.getStoredItem(box);
        int count = TobaccoBoxHelper.getStoredCount(box);

        if (stored.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack out = stored.copy();
        out.setCount(1);

        String label = TobaccoBoxHelper.getLabel(box);
        if (!label.isEmpty()) {
            TobaccoLabelHelper.setProductLabel(out, label);
        }

        return out;
    }

    private ItemStack tryExtract(ItemStack box, Player player) {
        ItemStack out = getExtractableItem(box);
        if (out.isEmpty()) return ItemStack.EMPTY;
        int count = TobaccoBoxHelper.getStoredCount(box);
        ItemStack stored = TobaccoBoxHelper.getStoredItem(box);
        if (count == 1) {
            TobaccoBoxHelper.clearStored(box);
        } else {
            TobaccoBoxHelper.setStored(box, stored, count - 1);
        }

        playSound(player);
        return out;
    }

    private void playSound(Player player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemStack stored = TobaccoBoxHelper.getStoredItem(stack);
        int count = TobaccoBoxHelper.getStoredCount(stack);
        String label = TobaccoBoxHelper.getLabel(stack);

        if (!label.isEmpty()) {
            tooltip.add(Component.translatable("tobacconistmod.ui.label", label).withStyle(ChatFormatting.YELLOW));
        }

        if (stored.isEmpty()) {
            tooltip.add(Component.translatable("tobacconistmod.ui.empty").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tobacconistmod.tooltip.box.capacity")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.translatable("tobacconistmod.ui.contents", TobaccoBoxHelper.getBoxContentsComponent(stored))
                .withStyle(ChatFormatting.GRAY));

        Component blendLine = TobaccoBoxHelper.getBlendComponent(stored);
        if (!blendLine.getString().isEmpty()) {
            tooltip.add(Component.translatable("tobacconistmod.ui.blend", blendLine).withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.translatable("tobacconistmod.ui.stored", count, TobaccoBoxHelper.getCapacity(stored))
                .withStyle(ChatFormatting.GRAY));
    }
}