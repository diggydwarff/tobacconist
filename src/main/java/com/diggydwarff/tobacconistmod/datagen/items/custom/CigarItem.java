package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
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
            String tobacco = tag.getString("tobacco");
            String wrapper = tag.getString("wrapper");

            String productLabel = com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper.getProductLabel(stack);
            if (!productLabel.isEmpty()) {
                tooltip.add(Component.literal("Label: " + productLabel).withStyle(ChatFormatting.YELLOW));
            }

            if (!wrapper.isEmpty()) {
                tooltip.add(Component.literal(wrapper.replace("[", "").replace("]", "") + " Wrapper").withStyle(ChatFormatting.GOLD));
            }

            if (!tobacco.isEmpty()) {
                tooltip.add(Component.literal(tobacco.replace("[", "").replace("]", "")).withStyle(ChatFormatting.GOLD));
            } else {
                tooltip.add(Component.literal("Creative Tobacco").withStyle(ChatFormatting.GOLD));
            }

            int productQuality = TobaccoProductQualityHelper.getStoredProductQuality(stack);
            if (productQuality >= 0) {
                tooltip.add(Component.literal("Quality: " + productQuality + "/10").withStyle(ChatFormatting.GRAY));
            }

            String cutType = tag.getString(TobaccoCuringHelper.TAG_CUT_TYPE);
            if (!cutType.isEmpty()) {
                tooltip.add(Component.literal("Filler Cut: " + TobaccoCuringHelper.getCutDisplayName(cutType)).withStyle(ChatFormatting.GRAY));
            }

            String cureType = tag.getString(TobaccoCuringHelper.TAG_CURE_TYPE);
            if (!cureType.isEmpty()) {
                tooltip.add(Component.literal("Cure: " + TobaccoCuringHelper.getCureDisplayName(cureType)).withStyle(ChatFormatting.GRAY));
            }
        }

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}