package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.datagen.items.custom.WoodenSmokingPipeItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LooseTobaccoItem extends Item {

    public static final String NBT_TOBACCO = "PackedTobacco";
    public static final String NBT_PUFFS = "PuffsLeft";

    private final int maxPuffs;
    private final int strength;

    public LooseTobaccoItem(Properties props, int maxPuffs, int strength) {
        super(props);
        this.maxPuffs = maxPuffs;
        this.strength = strength;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack tobacco = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        // Offhand must be a pipe
        if (!(offhand.getItem() instanceof WoodenSmokingPipeItem)) {
            return InteractionResultHolder.pass(tobacco);
        }

        // Client: make it feel responsive
        if (level.isClientSide()) {
            return InteractionResultHolder.success(tobacco);
        }

        // Server: pack if empty
        CompoundTag tag = offhand.getOrCreateTag();
        int puffsLeft = tag.getInt(NBT_PUFFS);
        if (puffsLeft > 0) {
            return InteractionResultHolder.pass(tobacco); // already packed
        }

        tag.putString(NBT_TOBACCO, BuiltInRegistries.ITEM.getKey(tobacco.getItem()).toString());
        tag.putInt(NBT_PUFFS, this.maxPuffs);

        if (!player.getAbilities().instabuild) {
            tobacco.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(tobacco, false);
    }

    public int getStrength() { return strength; }
    public int getMaxPuffs() { return maxPuffs; }
}
