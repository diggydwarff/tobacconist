package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class VaporBottleItem extends Item {
    private final String kind;
    public VaporBottleItem(Properties properties, String kind) { super(properties); this.kind = kind; }

    @Override public Component getName(ItemStack stack) {
        LiquidBlend blend = VaporData.blend(stack);
        if (kind.equals("liquid") && blend != null && !blend.usable()) return Component.translatable("item.tobacconistmod.flavor_concentrate");
        String label = com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper.getProductLabel(stack);
        if (kind.equals("liquid") && !label.isEmpty()) return com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper.buildNamedProduct(label, super.getName(stack));
        return super.getName(stack);
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LiquidBlend blend = VaporData.blend(stack);
        if ((kind.equals("liquid") || kind.equals("card")) && blend != null) tooltip.addAll(VaporData.description(blend));
        else tooltip.add(Component.translatable("tobacconistmod.vapor.ingredient." + kind).withStyle(ChatFormatting.DARK_GRAY));
        if (!kind.equals("card")) tooltip.add(Component.translatable("tobacconistmod.vapor.volume", VaporData.volume(stack)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
