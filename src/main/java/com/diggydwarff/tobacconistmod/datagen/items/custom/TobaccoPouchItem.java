package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.datagen.items.ModTags;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Small no-GUI container for pipe tobacco. A pouch holds one exact tobacco batch at a time
 * so variety, cure, cut, quality, age, flavor and blend metadata remain intact.
 */
public class TobaccoPouchItem extends Item {
    public static final int CAPACITY = 16;

    private static final String TAG_ITEM = "StoredTobacco";
    private static final String TAG_DATA = "StoredTobaccoData";
    private static final String TAG_COUNT = "StoredCount";

    public TobaccoPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);

        // Sneak-right-clicking the pouch in the main hand withdraws one item at a time.
        if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
            if (level.isClientSide()) return InteractionResultHolder.success(pouch);
            return withdrawOne(player, pouch)
                    ? InteractionResultHolder.sidedSuccess(pouch, false)
                    : InteractionResultHolder.pass(pouch);
        }

        if (hand != InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(pouch);
        }

        ItemStack main = player.getMainHandItem();

        // Main-hand loose tobacco -> offhand pouch.
        if (main.is(ModTags.Items.LOOSE_TOBACCO)) {
            if (level.isClientSide()) return InteractionResultHolder.success(pouch);
            return storeOne(player, pouch, main)
                    ? InteractionResultHolder.sidedSuccess(pouch, false)
                    : InteractionResultHolder.pass(pouch);
        }

        // Main-hand empty pipe -> offhand pouch.
        if (main.getItem() instanceof WoodenSmokingPipeItem pipeItem) {
            ItemStack stored = getStoredStack(pouch);
            if (!stored.isEmpty() && pipeItem.canPack(main)) {
                if (level.isClientSide()) return InteractionResultHolder.success(pouch);
                if (pipeItem.packFromTobacco(main, stored)) {
                    setStoredCount(pouch, getStoredCount(pouch) - 1);
                    return InteractionResultHolder.sidedSuccess(pouch, false);
                }
            }
        }

        return InteractionResultHolder.pass(pouch);
    }

    public static boolean storeOne(Player player, ItemStack pouch, ItemStack tobacco) {
        int count = getStoredCount(pouch);
        if (count >= CAPACITY) return false;

        ItemStack stored = getStoredStack(pouch);
        if (!stored.isEmpty() && !sameBatch(stored, tobacco)) return false;

        CompoundTag tag = LegacyItemTags.getOrCreateTag(pouch);
        if (stored.isEmpty()) {
            tag.putString(TAG_ITEM, BuiltInRegistries.ITEM.getKey(tobacco.getItem()).toString());
            CompoundTag tobaccoData = LegacyItemTags.getTag(tobacco);
            if (tobaccoData != null) {
                tag.put(TAG_DATA, tobaccoData.copy());
            } else {
                tag.remove(TAG_DATA);
            }
        }
        tag.putInt(TAG_COUNT, count + 1);

        if (!player.getAbilities().instabuild) tobacco.shrink(1);
        return true;
    }

    private static boolean withdrawOne(Player player, ItemStack pouch) {
        ItemStack stored = getStoredStack(pouch);
        if (stored.isEmpty()) return false;

        ItemStack one = stored.copy();
        one.setCount(1);
        if (!player.getInventory().add(one)) {
            player.drop(one, false);
        }
        setStoredCount(pouch, getStoredCount(pouch) - 1);
        return true;
    }

    public static void consumeOne(ItemStack pouch) {
        setStoredCount(pouch, getStoredCount(pouch) - 1);
    }

    private static boolean sameBatch(ItemStack a, ItemStack b) {
        if (!a.is(b.getItem())) return false;
        CompoundTag aTag = LegacyItemTags.getTag(a);
        CompoundTag bTag = LegacyItemTags.getTag(b);
        if (aTag == null) return bTag == null;
        return aTag.equals(bTag);
    }

    public static int getStoredCount(ItemStack pouch) {
        CompoundTag tag = LegacyItemTags.getTag(pouch);
        return tag == null ? 0 : Math.max(0, Math.min(CAPACITY, tag.getInt(TAG_COUNT)));
    }

    public static ItemStack getStoredStack(ItemStack pouch) {
        CompoundTag tag = LegacyItemTags.getTag(pouch);
        if (tag == null || !tag.contains(TAG_ITEM) || getStoredCount(pouch) <= 0) return ItemStack.EMPTY;

        ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_ITEM));
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id));
        if (tag.contains(TAG_DATA)) {
            LegacyItemTags.setTag(stack, tag.getCompound(TAG_DATA).copy());
        }
        return stack;
    }

    private static void setStoredCount(ItemStack pouch, int count) {
        CompoundTag tag = LegacyItemTags.getOrCreateTag(pouch);
        if (count <= 0) {
            tag.remove(TAG_ITEM);
            tag.remove(TAG_DATA);
            tag.remove(TAG_COUNT);
        } else {
            tag.putInt(TAG_COUNT, Math.min(CAPACITY, count));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredCount(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getStoredCount(stack) / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xA86E3F;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ItemStack stored = getStoredStack(stack);
        int count = getStoredCount(stack);

        if (stored.isEmpty()) {
            tooltip.add(Component.literal("Empty (0/" + CAPACITY + ")").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Hold in offhand to store loose tobacco").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.literal(count + "/" + CAPACITY + " • ").append(stored.getHoverName())
                .withStyle(ChatFormatting.GRAY));

        String cure = TobaccoCuringHelper.getCureType(stored);
        String cut = TobaccoCuringHelper.getCutType(stored);
        if (!cure.isEmpty() || !cut.isEmpty()) {
            StringBuilder line = new StringBuilder();
            if (!cure.isEmpty()) line.append(TobaccoCuringHelper.getCureDisplayName(cure));
            if (!cure.isEmpty() && !cut.isEmpty()) line.append(" • ");
            if (!cut.isEmpty()) line.append(TobaccoCuringHelper.getCutDisplayName(cut));
            tooltip.add(Component.literal(line.toString()).withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.literal("Use from offhand to pack an empty pipe").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Sneak-right-click to withdraw").withStyle(ChatFormatting.DARK_GRAY));
    }
}
