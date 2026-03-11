package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        String label = TobaccoLabelHelper.getProductLabel(stack);

        Component baseName;
        if (!label.isEmpty()) {
            baseName = TobaccoLabelHelper.buildNamedProduct(label, "Loose Tobacco");
        } else {
            baseName = super.getName(stack);
        }

        if (tag == null) {
            return baseName;
        }

        String cutType = TobaccoCuringHelper.getCutType(stack);
        if (cutType.isEmpty()) {
            return baseName;
        }

        return Component.literal(TobaccoCuringHelper.getCutDisplayName(cutType) + " ")
                .append(baseName);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        String productLabel = com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper.getProductLabel(stack);
        if (!productLabel.isEmpty()) {
            tooltip.add(Component.literal("Label: " + productLabel).withStyle(ChatFormatting.YELLOW));
        }

        int quality = TobaccoCuringHelper.getQuality(stack);
        if (quality > 0) {
            tooltip.add(Component.literal(
                    "Quality: " + quality + " (" + TobaccoCuringHelper.getQualityTier(quality) + ")"
            ).withStyle(ChatFormatting.GRAY));
        }

        String cureType = TobaccoCuringHelper.getCureType(stack);
        if (!cureType.isEmpty()) {
            tooltip.add(Component.literal(
                    "Cure: " + TobaccoCuringHelper.getCureDisplayName(cureType)
            ).withStyle(ChatFormatting.GRAY));
        }

        String cutType = TobaccoCuringHelper.getCutType(stack);
        if (!cutType.isEmpty()) {
            tooltip.add(Component.literal(
                    "Cut: " + TobaccoCuringHelper.getCutDisplayName(cutType)
            ).withStyle(ChatFormatting.GRAY));
        }

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.getBoolean("Fermented")) {
                tooltip.add(Component.literal("Fermented").withStyle(ChatFormatting.GOLD));
            }

            int aged = tag.getInt("AgedStages");
            if (aged > 0) {
                tooltip.add(Component.literal("Aged: " + aged).withStyle(ChatFormatting.GOLD));
            }

            if (tag.getBoolean("Ruined")) {
                tooltip.add(Component.literal("Ruined").withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack tobacco = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        if (!(offhand.getItem() instanceof WoodenSmokingPipeItem)) {
            return InteractionResultHolder.pass(tobacco);
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.success(tobacco);
        }

        CompoundTag pipeTag = offhand.getOrCreateTag();
        int puffsLeft = pipeTag.getInt(NBT_PUFFS);
        if (puffsLeft > 0) {
            return InteractionResultHolder.pass(tobacco);
        }

        pipeTag.putString(NBT_TOBACCO, BuiltInRegistries.ITEM.getKey(tobacco.getItem()).toString());
        pipeTag.putInt(NBT_PUFFS, this.maxPuffs);

        CompoundTag tobaccoData = tobacco.getTag();
        if (tobaccoData != null) {
            pipeTag.put("PackedTobaccoData", tobaccoData.copy());
        }

        if (!player.getAbilities().instabuild) {
            tobacco.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(tobacco, false);
    }

    public int getStrength() {
        return strength;
    }

    public int getMaxPuffs() {
        return maxPuffs;
    }
}