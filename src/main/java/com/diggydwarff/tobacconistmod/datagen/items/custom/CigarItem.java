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

public class CigarItem extends SmokingItem {

    public CigarItem(Properties properties) {
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
            return TobaccoLabelHelper.buildNamedProduct(label, "Cigar");
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getTag();

        if (tag != null) {
            String wrapper = TobaccoTooltipHelper.cleanTobaccoName(tag.getString("wrapper"));
            String filler = TobaccoTooltipHelper.getPackedLeafName(stack);

            if (wrapper.isEmpty()) {
                wrapper = "Unknown";
            }
            if (filler.isEmpty()) {
                filler = "Unknown";
            }

            tooltip.add(Component.empty());
            tooltip.add(Component.literal(getCigarSummary(stack)).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.empty());

            ItemStack stored = TobaccoBoxHelper.getStoredItem(stack);

            int displayQuality = getDisplayQuality10(stack);
            if (displayQuality >= 0) {
                tooltip.add(Component.literal("Quality: " + displayQuality + "/10")
                        .withStyle(ChatFormatting.GRAY));
            }

            tooltip.add(Component.literal("Filler: " + getFillerLine(stack)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Wrapper: " + getWrapperLine(stack)).withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (flagIn.isAdvanced()) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("✿ Fermented").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("ᵐ Months aged, ʸ Years aged").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("Wrapper aging/fermentation unavailable until wrapper NBT is stored on cigar recipes.")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private String getFillerLine(ItemStack stack) {
        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        if (packed == null) {
            return "Unknown";
        }

        int quality100 = packed.contains(TobaccoCuringHelper.TAG_QUALITY)
                ? packed.getInt(TobaccoCuringHelper.TAG_QUALITY)
                : 60;

        String qualityWord = TobaccoTooltipHelper.getQualityWord(quality100);

        String cureType = packed.contains(TobaccoCuringHelper.TAG_CURE_TYPE)
                ? packed.getString(TobaccoCuringHelper.TAG_CURE_TYPE)
                : "";

        String cutType = packed.contains(TobaccoCuringHelper.TAG_CUT_TYPE)
                ? packed.getString(TobaccoCuringHelper.TAG_CUT_TYPE)
                : "";

        String cureWord = TobaccoCuringHelper.getCureDisplayName(cureType);
        String cutWord = TobaccoCuringHelper.getCutDisplayName(cutType);

        String leaf = TobaccoTooltipHelper.cleanTobaccoName(
                        TobaccoTooltipHelper.getPackedLeafName(stack)
                )
                .replace("Premium ", "")
                .replace("Fine ", "")
                .replace("Standard ", "")
                .replace("Harsh ", "")
                .replace("Poor ", "")
                .replace("Flue-Cured ", "")
                .replace("Fire-Cured ", "")
                .replace("Air-Cured ", "")
                .replace("Sun-Cured ", "")
                .replace("Ribbon Cut ", "")
                .replace("Shag Cut ", "")
                .replace("Fine Cut ", "")
                .replace("Flake Cut ", "")
                .replace("Plug Cut ", "")
                .replace("Rough Cut ", "")
                .replace("Loose ", "")
                .trim();

        StringBuilder out = new StringBuilder();
        out.append(qualityWord);

        if (!cureWord.isEmpty()) {
            out.append(" ").append(cureWord);
        }

        if (!cutWord.isEmpty() && !cutWord.equals("Uncut")) {
            out.append(" ").append(cutWord);
        }

        if (!leaf.isEmpty()) {
            out.append(" ").append(leaf);
        }

        out.append(TobaccoTooltipHelper.getProcessSuffix(packed));
        return out.toString().trim();
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

    private String getCigarSummary(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return "Unknown Blend";
        }

        String wrapper = TobaccoTooltipHelper.cleanTobaccoName(tag.getString("wrapper"))
                .replace("Premium ", "")
                .replace("Fine ", "")
                .replace("Standard ", "")
                .replace("Harsh ", "")
                .replace("Poor ", "")
                .replace("Flue-Cured ", "")
                .replace("Fire-Cured ", "")
                .replace("Air-Cured ", "")
                .replace("Sun-Cured ", "")
                .replace("Loose ", "")
                .trim();

        String filler = TobaccoTooltipHelper.cleanTobaccoName(tag.getString("tobacco"))
                .replace("Premium ", "")
                .replace("Fine ", "")
                .replace("Standard ", "")
                .replace("Harsh ", "")
                .replace("Poor ", "")
                .replace("Flue-Cured ", "")
                .replace("Fire-Cured ", "")
                .replace("Air-Cured ", "")
                .replace("Sun-Cured ", "")
                .replace("Loose ", "")
                .trim();

        if (wrapper.isEmpty()) wrapper = "Unknown";
        if (filler.isEmpty()) filler = "Unknown";

        return wrapper + " Wrapper - " + filler;
    }

    private String getWrapperLine(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return "Unknown";
        }

        CompoundTag wrapperData = tag.contains("WrapperLeafData")
                ? tag.getCompound("WrapperLeafData")
                : null;

        String wrapperName = TobaccoTooltipHelper.cleanTobaccoName(tag.getString("wrapper"))
                .replace("Premium ", "")
                .replace("Fine ", "")
                .replace("Standard ", "")
                .replace("Harsh ", "")
                .replace("Poor ", "")
                .replace("Flue-Cured ", "")
                .replace("Fire-Cured ", "")
                .replace("Air-Cured ", "")
                .replace("Sun-Cured ", "")
                .replace("Loose ", "")
                .trim();

        if (wrapperName.isEmpty()) {
            wrapperName = "Unknown";
        }

        int quality100 = 60;
        String cureType = "";

        if (wrapperData != null) {
            if (wrapperData.contains(TobaccoCuringHelper.TAG_QUALITY)) {
                quality100 = wrapperData.getInt(TobaccoCuringHelper.TAG_QUALITY);
            }
            if (wrapperData.contains(TobaccoCuringHelper.TAG_CURE_TYPE)) {
                cureType = wrapperData.getString(TobaccoCuringHelper.TAG_CURE_TYPE);
            }
        }

        String qualityWord = TobaccoTooltipHelper.getQualityWord(quality100);
        String cureWord = TobaccoCuringHelper.getCureDisplayName(cureType);

        StringBuilder out = new StringBuilder();
        out.append(qualityWord);

        if (!cureWord.isEmpty()) {
            out.append(" ").append(cureWord);
        }

        out.append(" ").append(wrapperName);

        if (wrapperData != null) {
            out.append(TobaccoTooltipHelper.getProcessSuffix(wrapperData));
        }

        return out.toString().trim();
    }
}