package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.TobaccoAromaticHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendComponent;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BlendedTobaccoItem extends LooseTobaccoItem {
    public BlendedTobaccoItem(Item.Properties properties) {
        super(properties, 40, 12);
    }

    @Override
    public Component getName(ItemStack stack) {
        String productLabel = TobaccoLabelHelper.getProductLabel(stack);
        String blendName = TobaccoBlendHelper.getIntrinsicBlendName(stack);

        Component base;
        if (!productLabel.isEmpty()) {
            base = TobaccoLabelHelper.buildNamedProduct(productLabel, "Blended Tobacco");
        } else if (!blendName.isEmpty()) {
            base = Component.literal(blendName);
        } else {
            base = Component.translatable("item.tobacconistmod.blended_tobacco");
        }

        String cut = TobaccoCuringHelper.getCutType(stack);
        if (cut.isEmpty()) return base;
        return Component.literal(TobaccoCuringHelper.getCutDisplayName(cut) + " ").append(base);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        List<TobaccoBlendComponent> components = TobaccoBlendHelper.getComponentData(stack);
        if (components.isEmpty()) return;

        tooltip.add(Component.literal("Blend Components:").withStyle(ChatFormatting.DARK_GRAY));
        for (TobaccoBlendComponent component : components) {
            StringBuilder line = new StringBuilder("  ")
                    .append(TobaccoBlendHelper.formatVariety(component.variety()));

            if (TobacconistConfig.isQualitySystemEnabled()) {
                line.append(" Q").append(component.quality());
            }

            if (!component.cure().isBlank()) {
                line.append(" • ").append(TobaccoCuringHelper.getCureDisplayName(component.cure()));
            }

            String flavor = TobaccoAromaticHelper.formatFlavorId(component.flavorId());
            if (!flavor.isEmpty()) {
                line.append(" • ").append(flavor);
            }

            tooltip.add(Component.literal(line.toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
