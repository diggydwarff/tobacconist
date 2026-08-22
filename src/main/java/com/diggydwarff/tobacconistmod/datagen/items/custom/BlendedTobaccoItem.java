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
            StringBuilder base = new StringBuilder("  ")
                    .append(TobaccoBlendHelper.formatVariety(component.variety()));

            if (TobacconistConfig.isQualitySystemEnabled()) {
                base.append(" Q").append(component.quality());
            }

            if (!component.cure().isBlank()) {
                base.append(" • ").append(TobaccoCuringHelper.getCureDisplayName(component.cure()));
            }

            var line = Component.literal(base.toString()).withStyle(ChatFormatting.DARK_GRAY);
            String flavor = TobaccoAromaticHelper.formatFlavorId(component.flavorId());
            if (!flavor.isEmpty()) {
                line.append(Component.literal(" • " + flavor).withStyle(ChatFormatting.LIGHT_PURPLE));
            }

            tooltip.add(line);
        }
    }
}
