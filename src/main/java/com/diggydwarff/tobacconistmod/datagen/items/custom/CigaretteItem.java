package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import static com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper.TAG_GROWTH_QUALITY;
import static com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper.TAG_QUALITY;

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
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        } else {
            stack.setDamageValue(stack.getDamageValue() + 1);
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean smokeFromMouthSlot(Player player, ServerLevel level, ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem() || stack.getDamageValue() >= stack.getMaxDamage()) {
            return false;
        }

        this.triggerSmokingEffectPlayer(player, level, 0, stack);

        int nextDamage = stack.getDamageValue() + 1;
        if (nextDamage >= stack.getMaxDamage()) {
            stack.shrink(1);
        } else {
            stack.setDamageValue(nextDamage);
        }
        return true;
    }

    @Override
    public boolean shouldEmitMouthSmoke(ItemStack stack) {
        return stack.getDamageValue() > 0 && stack.getDamageValue() < stack.getMaxDamage();
    }

    @Override
    public Component getName(ItemStack stack) {
        boolean flavored = TobaccoAromaticHelper.getProductAromaticProfile(stack).isAromatic();
        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        boolean blended = packed != null && !TobaccoBlendHelper.getComponentData(packed).isEmpty();

        Component productName = Component.translatable(blended
                ? (flavored ? "tobacconistmod.product.flavored_blended_cigarette" : "tobacconistmod.product.blended_cigarette")
                : (flavored ? "tobacconistmod.product.flavored_cigarette" : "item.tobacconistmod.cigarette"));

        String label = TobaccoLabelHelper.getProductLabel(stack);
        if (!label.isEmpty()) {
            return TobaccoLabelHelper.buildNamedProduct(label, productName);
        }

        String blendName = packed == null ? "" : packed.getString(TobaccoBlendHelper.TAG_BLEND_NAME);
        if (!blendName.isEmpty()) {
            Component cigaretteName = Component.translatable(flavored
                    ? "tobacconistmod.product.flavored_cigarette"
                    : "item.tobacconistmod.cigarette");
            return Component.translatable(
                    "tobacconistmod.product.named",
                    TobaccoBlendHelper.getIntrinsicBlendNameComponent(packed),
                    cigaretteName
            );
        }

        if (blended || flavored) return productName;
        return super.getName(stack);
    }


    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = LegacyItemTags.getTag(stack);

        if (tag != null) {
            String tobacco = tag.getString("tobacco");

            if (!tobacco.isEmpty()) {
                tooltip.add(getCigaretteSummaryComponent(stack, tobacco, tag).copy().withStyle(ChatFormatting.GOLD));
                int agedDays = TobaccoBarrelBlockEntity.getAgedDays(stack);
                if (agedDays > 0) {
                    tooltip.add(Component.translatable(
                            "tobacconistmod.ui.age_detailed",
                            TobaccoText.ageDuration(agedDays),
                            TobaccoText.ageLabel(agedDays)
                    ).withStyle(ChatFormatting.GOLD));
                }
                tooltip.add(Component.empty());
            } else {
                tooltip.add(Component.translatable("tobacconistmod.ui.creative_tobacco")
                        .withStyle(ChatFormatting.GOLD));
            }

            if (TobacconistConfig.isQualitySystemEnabled()) {
                int displayQuality = getDisplayQuality10(stack);
                if (displayQuality >= 0) {
                    tooltip.add(Component.translatable("tobacconistmod.ui.quality_10", displayQuality)
                            .withStyle(ChatFormatting.GRAY));
                }
            }

            ItemStack temp = new ItemStack(stack.getItem());
            LegacyItemTags.setTag(temp, tag.copy());
            TobaccoCuringHelper.ensureDefaultTobaccoData(temp);

            String cutType = TobaccoCuringHelper.getCutType(temp);
            if (!cutType.isEmpty()) {
                tooltip.add(Component.translatable("tobacconistmod.ui.cut", TobaccoText.cut(cutType))
                        .withStyle(ChatFormatting.GRAY));
            }

            String cureType = TobaccoCuringHelper.getCureType(temp);
            if (!cureType.isEmpty()) {
                tooltip.add(Component.translatable("tobacconistmod.ui.cure", TobaccoText.cure(cureType))
                        .withStyle(ChatFormatting.GRAY));
            }

            CompoundTag packedBlend = TobaccoTooltipHelper.getPackedTobaccoData(stack);
            boolean hasBlendComponents = packedBlend != null
                    && !TobaccoBlendHelper.getComponentData(packedBlend).isEmpty();
            TobaccoAromaticHelper.AromaticProfile aromatic = TobaccoAromaticHelper.getProductAromaticProfile(stack);
            if (aromatic.isAromatic() && !hasBlendComponents) {
                tooltip.add(aromatic.tooltipComponent().copy().withStyle(ChatFormatting.LIGHT_PURPLE));
            }

            if (TobaccoBarrelBlockEntity.isRuined(stack)) {
                tooltip.add(Component.translatable("tobacconistmod.ui.ruined").withStyle(ChatFormatting.DARK_RED));
            }

            if (packedBlend != null) {
                List<TobaccoBlendComponent> blendComponents = TobaccoBlendHelper.getComponentData(packedBlend);
                if (!blendComponents.isEmpty()) {
                    String blendName = packedBlend.getString(TobaccoBlendHelper.TAG_BLEND_NAME);
                    tooltip.add((blendName.isEmpty()
                            ? Component.translatable("tobacconistmod.ui.blend_components")
                            : Component.translatable(
                                    "tobacconistmod.ui.blend",
                                    TobaccoBlendHelper.getIntrinsicBlendNameComponent(packedBlend)
                            ))
                            .withStyle(ChatFormatting.DARK_GRAY));
                    if (!blendName.isEmpty()) {
                        TobaccoBlendHelper.appendLegendarySecretTooltip(stack, tooltip);
                    }
                    for (TobaccoBlendComponent component : blendComponents) {
                        Component flavor = component.flavorId().isBlank()
                                ? Component.translatable("tobacconistmod.ui.plain")
                                : TobaccoText.flavor(component.flavorId());
                        Integer quality = TobacconistConfig.isQualitySystemEnabled() ? component.quality() : null;
                        tooltip.add(TobaccoText.blendComponent(
                                component.variety(), quality, component.cure(), flavor
                        ).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            }
        }

        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (flagIn.isAdvanced()) {
            boolean fermented = TobaccoBarrelBlockEntity.isFermented(stack);
            int agedDays = TobaccoBarrelBlockEntity.getAgedDays(stack);
            if (fermented || agedDays > 0) {
                tooltip.add(Component.empty());
                if (fermented) {
                    tooltip.add(Component.translatable("tobacconistmod.ui.fermented_marker")
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
                if (agedDays > 0) {
                    tooltip.add(Component.translatable("tobacconistmod.ui.age_legend")
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }


    private int getDisplayQuality10(ItemStack stack) {
        int productQuality = TobaccoProductQualityHelper.getStoredProductQuality(stack);
        if (productQuality >= 0) {
            return productQuality;
        }

        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        if (packed != null) {
            int quality = packed.contains(TAG_QUALITY)
                    ? packed.getInt(TAG_QUALITY)
                    : packed.contains(TAG_GROWTH_QUALITY)
                    ? packed.getInt(TAG_GROWTH_QUALITY)
                    : 60;

            return Math.max(1, Math.round(quality / 10.0f));
        }

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag != null && tag.contains(TAG_QUALITY)) {
            return Math.max(1, Math.round(tag.getInt(TAG_QUALITY) / 10.0f));
        }

        return 6;
    }

    private Component getCigaretteSummaryComponent(ItemStack stack, String tobacco, CompoundTag tag) {
        int quality = 60;
        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        if (packed != null) {
            quality = packed.contains(TAG_QUALITY)
                    ? packed.getInt(TAG_QUALITY)
                    : packed.contains(TAG_GROWTH_QUALITY)
                    ? packed.getInt(TAG_GROWTH_QUALITY)
                    : 60;
        }
        String suffix = TobaccoTooltipHelper.getProcessSuffix(tag);
        return Component.translatable(
                "tobacconistmod.tooltip.cigarette_summary",
                TobaccoText.qualityDescriptor(quality),
                TobaccoText.varietyFromStoredName(tobacco),
                suffix
        );
    }





}