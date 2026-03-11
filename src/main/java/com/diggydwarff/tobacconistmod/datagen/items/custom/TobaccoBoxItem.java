package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
            return Component.translatable("item.tobacconistmod.tobacco_box");
        }

        String typeName = TobaccoBoxHelper.getContentPluralName(stored);

        if (!label.isEmpty()) {
            return Component.literal("Box of " + label + " " + typeName);
        }

        return Component.literal("Box of " + typeName);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;

        int cooldown = stack.getOrCreateTag().getInt(TAG_USE_COOLDOWN);
        if (cooldown > 0) {
            stack.getOrCreateTag().putInt(TAG_USE_COOLDOWN, cooldown - 1);
        }

        ItemStack offhand = player.getOffhandItem();
        ItemStack mainhand = player.getMainHandItem();

        if (offhand != stack) return;
        if (!mainhand.isEmpty()) return;
        if (!player.swinging && !player.isUsingItem()) return;
        if (stack.getOrCreateTag().getInt(TAG_USE_COOLDOWN) > 0) return;

        ItemStack extracted = tryExtract(stack, player);
        if (!extracted.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, extracted);
            stack.getOrCreateTag().putInt(TAG_USE_COOLDOWN, 6);
        }
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack box, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;

        ItemStack other = slot.getItem();
        if (other.isEmpty()) {
            ItemStack extracted = tryExtract(box, player);
            if (!extracted.isEmpty()) {
                slot.set(extracted);
                return true;
            }
            return false;
        }

        if (tryInsert(box, other, player)) {
            slot.set(other);
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack box, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY) return false;
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
        if (incoming.isEmpty()) return false;
        if (!TobaccoBoxHelper.canAccept(box, incoming)) return false;

        ItemStack stored = TobaccoBoxHelper.getStoredItem(box);
        int count = TobaccoBoxHelper.getStoredCount(box);

        ItemStack comparisonBase = stored.isEmpty() ? incoming : stored;
        int cap = TobaccoBoxHelper.getCapacity(comparisonBase);
        if (count >= cap) return false;

        if (stored.isEmpty()) {
            TobaccoBoxHelper.setStored(box, incoming, 1);
        } else {
            TobaccoBoxHelper.setStored(box, stored, count + 1);
        }

        incoming.shrink(1);
        playSound(player);
        return true;
    }

    private ItemStack tryExtract(ItemStack box, Player player) {

        ItemStack stored = TobaccoBoxHelper.getStoredItem(box);
        int count = TobaccoBoxHelper.getStoredCount(box);

        if (stored.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack storedPrototype = stored;

        ItemStack out = storedPrototype.copy();
        out.setCount(1);

        String label = TobaccoLabelHelper.getBoxLabel(box);
        if (!label.isEmpty()) {
            TobaccoLabelHelper.setProductLabel(out, label);
        }

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

    public static String getBlendLine(ItemStack stored) {
        if (stored.isEmpty()) return "";

        CompoundTag tag = stored.getTag();
        if (tag == null) return "";

        int quality = tag.contains("Quality") ? tag.getInt("Quality") : -1;

        String cure = formatCureType(tag.getString("CureType"));
        String cut = formatCutType(tag.getString("CutType"));
        String tobacco = formatTobaccoType(tag.getString("TobaccoType"));

        StringBuilder out = new StringBuilder();

        if (quality >= 0) {
            out.append(quality).append("/10");
        }

        if (!cure.isEmpty()) {
            if (!out.isEmpty()) out.append(" ");
            out.append(cure);
        }

        if (!cut.isEmpty()) {
            if (!out.isEmpty()) out.append(" ");
            out.append(cut);
        }

        if (!tobacco.isEmpty()) {
            if (!out.isEmpty()) out.append(" ");
            out.append(tobacco);
        }

        return out.toString();
    }

    private static String formatTobaccoType(String type) {
        return switch (type) {
            case "wild" -> "Wild";
            case "virginia" -> "Virginia";
            case "burley" -> "Burley";
            case "oriental" -> "Oriental";
            case "dokha" -> "Dokha";
            case "shade" -> "Shade";
            default -> "";
        };
    }

    private static String formatCutType(String type) {
        return switch (type) {
            case "ribbon" -> "Ribbon Cut";
            case "shag" -> "Shag Cut";
            case "fine" -> "Fine Cut";
            case "flake" -> "Flake Cut";
            case "plug" -> "Plug Cut";
            default -> "";
        };
    }

    private static String formatCureType(String type) {
        return switch (type) {
            case "air_cured" -> "Air-Cured";
            case "fire_cured" -> "Fire-Cured";
            case "flue_cured" -> "Flue-Cured";
            case "sun_cured" -> "Sun-Cured";
            default -> "";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemStack stored = TobaccoBoxHelper.getStoredItem(stack);
        int count = TobaccoBoxHelper.getStoredCount(stack);
        String label = TobaccoBoxHelper.getLabel(stack);

        if (stored.isEmpty()) {
            tooltip.add(Component.literal("Empty").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Holds cigars, cigarettes, loose tobacco, or shisha")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (!label.isEmpty()) {
            tooltip.add(Component.literal("Label: " + label).withStyle(ChatFormatting.YELLOW));
        }

        tooltip.add(Component.literal("Contents: " + TobaccoBoxHelper.getContentPluralName(stored))
                .withStyle(ChatFormatting.GRAY));

        String blendLine = TobaccoBoxHelper.getBlendLine(stored);
        if (!blendLine.isEmpty()) {
            tooltip.add(Component.literal("Blend: " + blendLine).withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.literal("Stored: " + count + " / " + TobaccoBoxHelper.getCapacity(stored))
                .withStyle(ChatFormatting.GRAY));
    }
}