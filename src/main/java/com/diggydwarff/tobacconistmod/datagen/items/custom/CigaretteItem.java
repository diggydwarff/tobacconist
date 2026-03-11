package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class CigaretteItem extends SmokingItem {

    public CigaretteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }

        this.triggerSmokingEffectPlayer(player, (ServerLevel) level, 0, stack);

        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        } else {
            stack.setDamageValue(stack.getDamageValue() + 1);
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean shouldEmitMouthSmoke(ItemStack stack) {
        return stack.getDamageValue() > 0 && stack.getDamageValue() < stack.getMaxDamage();
    }

    @Override
    public Component getName(ItemStack stack) {
        String label = TobaccoLabelHelper.getProductLabel(stack);
        if (!label.isEmpty()) {
            return TobaccoLabelHelper.buildNamedProduct(label, "Cigarette");
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getTag();

        if (tag != null) {
            String tobacco = tag.getString("tobacco");

            if (!tobacco.isEmpty()) {
                String summary = getCigaretteSummary(stack, tobacco, tag);
                tooltip.add(Component.literal(summary).withStyle(ChatFormatting.GOLD));
            } else {
                tooltip.add(Component.literal("Creative Tobacco").withStyle(ChatFormatting.GOLD));
            }

            ItemStack stored = TobaccoBoxHelper.getStoredItem(stack);

            int displayQuality = getDisplayQuality10(stack);
            if (displayQuality >= 0) {
                tooltip.add(Component.literal("Quality: " + displayQuality + "/10")
                        .withStyle(ChatFormatting.GRAY));
            }

            String cutType = tag.getString(TobaccoProductQualityHelper.TAG_INPUT_CUT_TYPE);
            if (cutType.isEmpty()) {
                cutType = tag.getString(TobaccoCuringHelper.TAG_CUT_TYPE);
            }
            if (!cutType.isEmpty()) {
                tooltip.add(Component.literal("Cut: " + TobaccoCuringHelper.getCutDisplayName(cutType)).withStyle(ChatFormatting.GRAY));
            }

            String cureType = tag.getString(TobaccoProductQualityHelper.TAG_INPUT_CURE_TYPE);
            if (cureType.isEmpty()) {
                cureType = tag.getString(TobaccoCuringHelper.TAG_CURE_TYPE);
            }
            if (!cureType.isEmpty()) {
                tooltip.add(Component.literal("Cure: " + TobaccoCuringHelper.getCureDisplayName(cureType)).withStyle(ChatFormatting.GRAY));
            }
        }

        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (flagIn.isAdvanced()) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("✿ Fermented").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("ᵐ Months aged, ʸ Years aged").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private int getDisplayQuality10(ItemStack stack) {
        int productQuality = TobaccoProductQualityHelper.getStoredProductQuality(stack);
        if (productQuality >= 0) {
            return productQuality;
        }

        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        if (packed != null && packed.contains(TobaccoCuringHelper.TAG_QUALITY)) {
            return Math.max(1, Math.round(packed.getInt(TobaccoCuringHelper.TAG_QUALITY) / 10.0f));
        }

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TobaccoCuringHelper.TAG_QUALITY)) {
            return Math.max(1, Math.round(tag.getInt(TobaccoCuringHelper.TAG_QUALITY) / 10.0f));
        }

        return -1;
    }

    private String getCigaretteSummary(ItemStack stack, String tobacco, CompoundTag tag) {
        int quality100 = 60;

        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        if (packed != null && packed.contains(TobaccoCuringHelper.TAG_QUALITY)) {
            quality100 = packed.getInt(TobaccoCuringHelper.TAG_QUALITY);
        }

        String qualityWord = TobaccoTooltipHelper.getQualityWord(quality100);

        String cleaned = TobaccoTooltipHelper.cleanTobaccoName(tobacco)
                .replace("Ribbon Cut ", "")
                .replace("Shag Cut ", "")
                .replace("Fine Cut ", "")
                .replace("Flake Cut ", "")
                .replace("Plug Cut ", "")
                .trim();

        String summary = qualityWord + " " + cleaned;
        summary += TobaccoTooltipHelper.getProcessSuffix(tag);

        return summary.trim();
    }
}