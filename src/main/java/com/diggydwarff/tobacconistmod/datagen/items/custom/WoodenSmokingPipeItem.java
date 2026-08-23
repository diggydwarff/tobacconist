package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.datagen.items.ModTags;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class WoodenSmokingPipeItem extends SmokingItem {

    public  static final String NBT_TOBACCO = "PackedTobacco";
    public static final String NBT_PUFFS = "PuffsLeft";
    public static final String NBT_PACKED_MAX_PUFFS = "PackedMaxPuffs";
    public static final int MAX_PUFFS = 40;
    private static final int MAX_POUCH_BONUS_PUFFS = 5;

    public WoodenSmokingPipeItem(Properties properties) {
        super(properties);
    }

    private boolean isTobacco(ItemStack stack) {
        return stack.is(ModTags.Items.LOOSE_TOBACCO);
    }

    @Override
    public Component getName(ItemStack stack) {
        var tag = LegacyItemTags.getTag(stack);

        if (tag != null && tag.contains(WoodenPipeRecipe.NBT_WOOD_PLANK)) {
            String idString = tag.getString(WoodenPipeRecipe.NBT_WOOD_PLANK);
            ResourceLocation id = ResourceLocation.parse(idString);

            var item = BuiltInRegistries.ITEM.get(id);
            ItemStack plankStack = new ItemStack(item);

            // Example: "Oak Planks" → "Oak"
            String plankName = plankStack.getHoverName().getString();
            plankName = plankName.replace(" Planks", "");

            return Component.literal(plankName + " Smoking Pipe");
        }

        return super.getName(stack);
    }

    @Override
    public boolean smokeFromMouthSlot(Player player, ServerLevel level, ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        int puffs = tag == null ? 0 : tag.getInt(NBT_PUFFS);
        if (puffs <= 0) return false;

        this.triggerSmokingEffectPlayer(player, level, 0, getPackedTobaccoStack(stack));

        puffs--;
        CompoundTag mutableTag = LegacyItemTags.getOrCreateTag(stack);
        if (puffs <= 0) {
            mutableTag.remove(NBT_PUFFS);
            mutableTag.remove(NBT_TOBACCO);
            mutableTag.remove(NBT_PACKED_MAX_PUFFS);
            mutableTag.remove("PackedTobaccoData");
        } else {
            mutableTag.putInt(NBT_PUFFS, puffs);
        }
        return true;
    }

    @Override
    public boolean shouldEmitMouthSmoke(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null || !tag.contains("PuffsLeft")) {
            return false;
        }

        int puffs = tag.getInt(NBT_PUFFS);
        int packedMax = getPackedMaxPuffs(stack);
        return puffs > 0 && puffs < packedMax;
    }

    private boolean isPacked(ItemStack pipe) {
        var tag = LegacyItemTags.getTag(pipe);
        return tag != null && tag.contains(NBT_TOBACCO) && tag.getInt(NBT_PUFFS) > 0;
    }

    public boolean canPack(ItemStack pipe) {
        return !isPacked(pipe);
    }

    /** Packs one exact tobacco batch into this pipe, preserving all custom tobacco data. */
    public boolean packFromTobacco(ItemStack pipe, ItemStack tobacco) {
        return packFromTobacco(pipe, tobacco, 0);
    }

    /**
     * Packing from a Tobacco Pouch is slightly more efficient than hand-packing: each reload
     * receives a small random 1-5 puff bonus while keeping the normal tobacco's base capacity.
     */
    public boolean packFromPouch(ItemStack pipe, ItemStack tobacco, RandomSource random) {
        if (random == null) return packFromTobacco(pipe, tobacco);
        return packFromTobacco(pipe, tobacco, 1 + random.nextInt(MAX_POUCH_BONUS_PUFFS));
    }

    private boolean packFromTobacco(ItemStack pipe, ItemStack tobacco, int bonusPuffs) {
        if (!canPack(pipe) || tobacco.isEmpty() || !isTobacco(tobacco)) return false;

        int basePuffs = tobacco.getItem() instanceof LooseTobaccoItem loose
                ? loose.getMaxPuffs()
                : MAX_PUFFS;
        int packedMaxPuffs = basePuffs + Math.max(0, bonusPuffs);

        CompoundTag pipeTag = LegacyItemTags.getOrCreateTag(pipe);
        pipeTag.putString(NBT_TOBACCO, BuiltInRegistries.ITEM.getKey(tobacco.getItem()).toString());
        pipeTag.putInt(NBT_PUFFS, packedMaxPuffs);
        pipeTag.putInt(NBT_PACKED_MAX_PUFFS, packedMaxPuffs);

        CompoundTag tobaccoData = LegacyItemTags.getTag(tobacco);
        if (tobaccoData != null) {
            pipeTag.put("PackedTobaccoData", tobaccoData.copy());
        } else {
            pipeTag.remove("PackedTobaccoData");
        }
        return true;
    }

    private static int getPackedMaxPuffs(ItemStack pipe) {
        CompoundTag tag = LegacyItemTags.getTag(pipe);
        if (tag == null) return MAX_PUFFS;
        int storedMax = tag.getInt(NBT_PACKED_MAX_PUFFS);
        return storedMax > 0 ? storedMax : MAX_PUFFS;
    }

    /** Rebuilds the tobacco stack stored inside a packed pipe for quality/age effects and tooltips. */
    public static ItemStack getPackedTobaccoStack(ItemStack pipe) {
        CompoundTag tag = LegacyItemTags.getTag(pipe);
        if (tag == null || !tag.contains(NBT_TOBACCO)) return ItemStack.EMPTY;

        ResourceLocation id = ResourceLocation.tryParse(tag.getString(NBT_TOBACCO));
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return ItemStack.EMPTY;

        ItemStack tobacco = new ItemStack(BuiltInRegistries.ITEM.get(id));
        if (tag.contains("PackedTobaccoData")) {
            LegacyItemTags.setTag(tobacco, tag.getCompound("PackedTobaccoData").copy());
        }
        return tobacco;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pipe = player.getItemInHand(hand);

        // A filled pouch in the offhand can pack an empty main-hand pipe directly.
        if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().getItem() instanceof TobaccoPouchItem) {
            ItemStack pouch = player.getOffhandItem();
            ItemStack stored = TobaccoPouchItem.getStoredStack(pouch);
            if (!stored.isEmpty() && canPack(pipe)) {
                if (!level.isClientSide() && packFromPouch(pipe, stored, level.random)) {
                    TobaccoPouchItem.consumeOne(pouch);
                }
                return InteractionResultHolder.sidedSuccess(pipe, level.isClientSide());
            }
        }

        // If pipe is in offhand AND player holding tobacco in main hand,
        // allow tobacco to handle packing instead of smoking
        if (hand == InteractionHand.OFF_HAND) {
            ItemStack main = player.getMainHandItem();
            if (!main.isEmpty() && main.getItem() instanceof LooseTobaccoItem) {
                return InteractionResultHolder.pass(pipe);
            }
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.consume(pipe); // prevents swing
        }

        CompoundTag tag = LegacyItemTags.getTag(pipe);
        int puffs = (tag == null) ? 0 : tag.getInt("PuffsLeft");

        if (puffs <= 0) {
            return InteractionResultHolder.pass(pipe);
        }

        this.triggerSmokingEffectPlayer(player, (ServerLevel) level, 0, getPackedTobaccoStack(pipe));

        puffs--;

        if (puffs <= 0) {
            LegacyItemTags.getOrCreateTag(pipe).remove(NBT_PUFFS);
            LegacyItemTags.getOrCreateTag(pipe).remove(NBT_TOBACCO);
            LegacyItemTags.getOrCreateTag(pipe).remove(NBT_PACKED_MAX_PUFFS);
            LegacyItemTags.getOrCreateTag(pipe).remove("PackedTobaccoData");
        } else {
            LegacyItemTags.getOrCreateTag(pipe).putInt("PuffsLeft", puffs);
        }

        return InteractionResultHolder.consume(pipe);
    }

    // Show "puffs left" on durability bar
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return isPacked(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (!isPacked(stack)) return 0;
        int puffs = LegacyItemTags.getOrCreateTag(stack).getInt(NBT_PUFFS);
        return Math.min(13, Math.round(13.0F * (puffs / (float) getPackedMaxPuffs(stack))));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55FF55; // green
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = LegacyItemTags.getTag(stack);

        if (tag != null && tag.contains("PuffsLeft")) {
            tooltip.add(Component.literal("Puffs left: " + tag.getInt("PuffsLeft")));
            if (tag.contains("PackedTobacco")) {
                String id = tag.getString("PackedTobacco");

                Item packedItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
                ItemStack packedStack = new ItemStack(packedItem);
                if (tag.contains("PackedTobaccoData")) {
                    LegacyItemTags.setTag(packedStack, tag.getCompound("PackedTobaccoData").copy());
                }

                tooltip.add(Component.literal("Packed: ")
                        .append(packedStack.getHoverName()));
            }
        } else {
            tooltip.add(Component.literal("Empty"));
            tooltip.add(Component.literal("Hold in offhand and right-click with tobacco in hand to pack"));
        }
    }

}
