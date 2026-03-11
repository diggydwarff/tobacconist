package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingProduct;
import com.diggydwarff.tobacconistmod.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShishaTobaccoItem extends SmokingProduct {

    public ShishaTobaccoItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        String label = TobaccoLabelHelper.getProductLabel(stack);
        if (!label.isEmpty()) {
            return TobaccoLabelHelper.buildNamedProduct(label, "Shisha Tobacco");
        }
        return super.getName(stack);
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getTag();

        String label = TobaccoLabelHelper.getProductLabel(stack);
        if (!label.isEmpty()) {
            tooltip.add(Component.literal("Label: " + label).withStyle(ChatFormatting.YELLOW));
        }

        if (tag != null) {
            String tobacco = tag.getString("tobacco");
            if (!tobacco.isEmpty()) {
                String summary = tobacco + TobaccoTooltipHelper.getProcessSuffix(tag);
                tooltip.add(Component.literal(summary).withStyle(ChatFormatting.GOLD));
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

            for (String shishaFlavor : Arrays.asList(
                    tag.getString("flavor1"),
                    tag.getString("flavor2"),
                    tag.getString("flavor3")
            )) {
                if (shishaFlavor.contains("Molasses")) {
                    Pattern r = Pattern.compile("\\(([^)]+)\\)");
                    Matcher m = r.matcher(shishaFlavor);
                    if (m.find()) {
                        tooltip.add(Component.literal("* " + m.group(0)).withStyle(ChatFormatting.GOLD));
                    }
                }
            }
        }

        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (flagIn.isAdvanced()) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("✿ Fermented").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("ᵐ Months aged, ʸ Years aged").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}