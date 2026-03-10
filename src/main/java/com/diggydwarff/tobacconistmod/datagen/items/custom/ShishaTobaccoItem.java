package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingProduct;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
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
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getTag();

        if (tag != null) {
            String tobacco = tag.getString("tobacco");
            String shishaFlavor1 = tag.getString("flavor1");
            String shishaFlavor2 = tag.getString("flavor2");
            String shishaFlavor3 = tag.getString("flavor3");

            if (!tobacco.isEmpty()) {
                tooltip.add(Component.literal(tobacco.replace("[", "").replace("]", "")).withStyle(ChatFormatting.GOLD));
            }

            int productQuality = TobaccoProductQualityHelper.getStoredProductQuality(stack);
            if (productQuality >= 0) {
                tooltip.add(Component.literal("Quality: " + productQuality + "/10").withStyle(ChatFormatting.GRAY));
            }

            String cutType = tag.getString(TobaccoCuringHelper.TAG_CUT_TYPE);
            if (!cutType.isEmpty()) {
                tooltip.add(Component.literal("Cut: " + TobaccoCuringHelper.getCutDisplayName(cutType)).withStyle(ChatFormatting.GRAY));
            }

            String cureType = tag.getString(TobaccoCuringHelper.TAG_CURE_TYPE);
            if (!cureType.isEmpty()) {
                tooltip.add(Component.literal("Cure: " + TobaccoCuringHelper.getCureDisplayName(cureType)).withStyle(ChatFormatting.GRAY));
            }

            for (String shishaFlavor : Arrays.asList(shishaFlavor1, shishaFlavor2, shishaFlavor3)) {
                if (shishaFlavor.contains("Molasses")) {
                    Pattern r = Pattern.compile("\\(([^)]+)\\)");
                    Matcher m = r.matcher(shishaFlavor);
                    if (m.find()) {
                        String match = m.group(0);
                        tooltip.add(Component.literal("* " + match).withStyle(ChatFormatting.GOLD));
                    }
                }
            }
        }

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}