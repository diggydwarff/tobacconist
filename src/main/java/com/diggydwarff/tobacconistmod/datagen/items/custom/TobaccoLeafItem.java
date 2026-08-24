package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.block.custom.HangingTobaccoBlock;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TobaccoLeafItem extends Item {
    public TobaccoLeafItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        var player = context.getPlayer();

        // Traditional hanging bunch placement: sneak-use the underside of a sturdy block while
        // holding one complete 16-leaf raw batch. Cured leaves never place a new bunch.
        if (player == null
                || !player.isShiftKeyDown()
                || context.getClickedFace() != Direction.DOWN
                || !TobaccoCuringHelper.isRawTobaccoLeaf(stack)) {
            return super.useOn(context);
        }

        if (stack.getCount() < 16) {
            if (!context.getLevel().isClientSide) {
                player.displayClientMessage(Component.literal("You need 16 matching raw tobacco leaves to hang a curing bunch."), true);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        BlockPos upperPos = context.getClickedPos().below();
        if (!HangingTobaccoBlock.canPlaceBundle(context.getLevel(), upperPos)) {
            if (!context.getLevel().isClientSide) {
                player.displayClientMessage(Component.literal("The bunch needs two open blocks below a sturdy underside."), true);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        if (!context.getLevel().isClientSide) {
            ItemStack batch = stack.copyWithCount(16);
            if (HangingTobaccoBlock.placeBundle(context.getLevel(), upperPos, batch)
                    && !player.getAbilities().instabuild) {
                stack.shrink(16);
            }
        }

        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = super.getName(stack);
        boolean qualityEnabled = TobacconistConfig.isQualitySystemEnabled();

        if (TobaccoCuringHelper.isDryTobaccoLeaf(stack)) {
            String cureType = TobaccoCuringHelper.getCureType(stack);
            String prefix = "";

            if (qualityEnabled) {
                int quality = TobaccoCuringHelper.getQuality(stack);
                prefix = TobaccoCuringHelper.getQualityTier(quality);
            }

            if (!cureType.isEmpty()) {
                if (!prefix.isEmpty()) prefix += " ";
                prefix += TobaccoCuringHelper.getCureDisplayName(cureType);
            }

            return prefix.isEmpty() ? baseName : Component.literal(prefix + " ").append(baseName);
        }

        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack) && qualityEnabled) {
            int growth = getRawGrowthQuality(stack);
            return Component.literal(TobaccoCuringHelper.getRawLeafTier(growth) + " ").append(baseName);
        }

        return baseName;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack)) {
            if (TobacconistConfig.isQualitySystemEnabled()) {
                int growth = getRawGrowthQuality(stack);
                tooltip.add(Component.literal(
                        "Growth Quality: " + growth + " (" + TobaccoCuringHelper.getRawLeafTier(growth) + ")"
                ).withStyle(ChatFormatting.GRAY));
            }
        } else {
            if (TobacconistConfig.isQualitySystemEnabled()) {
                int quality = TobaccoCuringHelper.getQuality(stack);
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
        }

        if (TobaccoBarrelBlockEntity.isFermented(stack)) {
            tooltip.add(Component.literal("Fermented").withStyle(ChatFormatting.GOLD));
        }

        int agedDays = TobaccoBarrelBlockEntity.getAgedDays(stack);
        if (agedDays > 0) {
            tooltip.add(Component.literal(
                    "Age: " + formatAge(agedDays) + " (" + getAgeLabel(agedDays) + ")"
            ).withStyle(ChatFormatting.GOLD));
        }

        if (TobaccoBarrelBlockEntity.isRuined(stack)) {
            tooltip.add(Component.literal("Ruined").withStyle(ChatFormatting.DARK_RED));
        }
    }

    private int getRawGrowthQuality(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag != null && tag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
            return Math.max(0, Math.min(70, tag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY)));
        }
        return 50;
    }

    private String formatAge(int agedDays) {
        int years = agedDays / 365;
        int days = agedDays % 365;

        if (years > 0) {
            return years + "y " + days + "d";
        }
        return days + "d";
    }

    private String getAgeLabel(int agedDays) {
        if (agedDays < 7) return "Fresh";
        if (agedDays < 30) return "Light Aged";
        if (agedDays < 90) return "Deep Aged";
        if (agedDays < 365) return "Vintage";
        return "Cellared";
    }
}