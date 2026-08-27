package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.TobaccoAromaticHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendComponent;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoText;
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
            base = TobaccoLabelHelper.buildNamedProduct(productLabel, Component.translatable("item.tobacconistmod.blended_tobacco"));
        } else if (!blendName.isEmpty()) {
            base = TobaccoBlendHelper.getIntrinsicBlendNameComponent(stack);
        } else {
            base = Component.translatable("item.tobacconistmod.blended_tobacco");
        }

        String cut = TobaccoCuringHelper.getCutType(stack);
        if (cut.isEmpty() || !blendName.isEmpty()) return base;
        return Component.translatable("tobacconistmod.product.cut_named", TobaccoText.cut(cut), base);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        List<TobaccoBlendComponent> components = TobaccoBlendHelper.getComponentData(stack);
        if (components.isEmpty()) return;

        if (!TobaccoBlendHelper.getIntrinsicBlendName(stack).isEmpty()) {
            tooltip.add(Component.translatable(
                    "tobacconistmod.ui.secret_cut_blend",
                    TobaccoText.cut(TobaccoCuringHelper.getCutType(stack))
            ).withStyle(style -> style.withColor(0xD4B96A)));
            TobaccoBlendHelper.appendLegendarySecretTooltip(stack, tooltip);
        }

        tooltip.add(Component.translatable("tobacconistmod.ui.blend_components").withStyle(ChatFormatting.DARK_GRAY));
        for (TobaccoBlendComponent component : components) {
            Component flavor = component.flavorId().isBlank() ? null : TobaccoText.flavor(component.flavorId());
            Integer quality = TobacconistConfig.isQualitySystemEnabled() ? component.quality() : null;
            tooltip.add(TobaccoText.blendComponent(
                    component.variety(), quality, component.cure(), flavor
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
