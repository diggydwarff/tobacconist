package com.diggydwarff.tobacconistmod.datagen.items.custom;

import net.minecraft.world.level.Level;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.block.custom.HangingTobaccoBlock;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoText;
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
        // holding one complete 16-leaf batch. Raw leaves cure normally; cured leaves hang as storage/decoration.
        if (player == null
                || !player.isShiftKeyDown()
                || context.getClickedFace() != Direction.DOWN
                || (!TobaccoCuringHelper.isRawTobaccoLeaf(stack)
                && !TobaccoCuringHelper.isDryTobaccoLeaf(stack))) {
            return super.useOn(context);
        }

        if (stack.getCount() < 16 && !player.getAbilities().instabuild) {
            if (!context.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable("tobacconistmod.message.hanging.need_16"), true);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        BlockPos upperPos = context.getClickedPos().below();
        if (!HangingTobaccoBlock.canPlaceBundle(context.getLevel(), upperPos)) {
            if (!context.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable("tobacconistmod.message.hanging.need_space"), true);
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
            Component prefix = Component.empty();

            if (qualityEnabled) {
                prefix = TobaccoText.qualityTier(TobaccoCuringHelper.getQuality(stack));
            }

            if (!cureType.isEmpty()) {
                prefix = prefix.getString().isEmpty()
                        ? TobaccoText.cure(cureType)
                        : Component.empty().append(prefix).append(" ").append(TobaccoText.cure(cureType));
            }

            return prefix.getString().isEmpty()
                    ? baseName
                    : Component.empty().append(prefix).append(" ").append(baseName);
        }

        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack) && qualityEnabled) {
            int growth = getRawGrowthQuality(stack);
            return Component.empty().append(TobaccoText.rawLeafTier(growth)).append(" ").append(baseName);
        }

        return baseName;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack)) {
            if (TobacconistConfig.isQualitySystemEnabled()) {
                int growth = getRawGrowthQuality(stack);
                tooltip.add(Component.translatable(
                        "tobacconistmod.ui.growth_quality", growth, TobaccoText.rawLeafTier(growth)
                ).withStyle(ChatFormatting.GRAY));
            }
        } else {
            if (TobacconistConfig.isQualitySystemEnabled()) {
                int quality = TobaccoCuringHelper.getQuality(stack);
                tooltip.add(Component.translatable(
                        "tobacconistmod.ui.quality", quality, TobaccoText.qualityTier(quality)
                ).withStyle(ChatFormatting.GRAY));
            }

            String cureType = TobaccoCuringHelper.getCureType(stack);
            if (!cureType.isEmpty()) {
                tooltip.add(Component.translatable(
                        "tobacconistmod.ui.cure", TobaccoText.cure(cureType)
                ).withStyle(ChatFormatting.GRAY));
            }
        }

        if (TobaccoBarrelBlockEntity.isFermented(stack)) {
            tooltip.add(Component.translatable("tobacconistmod.ui.fermented").withStyle(ChatFormatting.GOLD));
        }

        int agedDays = TobaccoBarrelBlockEntity.getAgedDays(stack);
        if (agedDays > 0) {
            tooltip.add(Component.translatable(
                    "tobacconistmod.ui.age_detailed", TobaccoText.ageDuration(agedDays), TobaccoText.ageLabel(agedDays)
            ).withStyle(ChatFormatting.GOLD));
        }

        if (TobaccoBarrelBlockEntity.isRuined(stack)) {
            tooltip.add(Component.translatable("tobacconistmod.ui.ruined").withStyle(ChatFormatting.DARK_RED));
        }
    }

    private int getRawGrowthQuality(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag != null && tag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
            return Math.max(0, Math.min(70, tag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY)));
        }
        return 50;
    }

}
