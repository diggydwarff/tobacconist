package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.effect.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.List;

public final class VapePenItem extends Item {
    public VapePenItem(Properties properties) { super(properties); }
    public static int volume(ItemStack pen) { return Math.clamp(pen.getOrDefault(VaporContent.VOLUME.get(), 0), 0, 250); }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 40; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pen = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer server) {
                int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
                server.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new VapePenMenu(id, inventory, slot), getName(pen)), buf -> buf.writeVarInt(slot));
            }
            return InteractionResultHolder.sidedSuccess(pen, level.isClientSide);
        }
        LiquidBlend blend = VaporData.blend(pen);
        if (blend == null || !blend.usable() || volume(pen) < 2 || VaporData.charge(pen) == 0) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("tobacconistmod.vapor.refill_hint"), true);
            return InteractionResultHolder.fail(pen);
        }
        player.startUsingItem(hand); return InteractionResultHolder.consume(pen);
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) { puff(stack, level, entity, 40 - remaining); }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) { puff(stack, level, entity, 40); return stack; }
    private void puff(ItemStack pen, Level level, LivingEntity entity, int draw) {
        if (!(entity instanceof ServerPlayer player) || draw < 6 || player.getCooldowns().isOnCooldown(this)) return;
        LiquidBlend blend = VaporData.blend(pen);
        if (blend == null || !blend.usable()) return;
        draw = Math.clamp(draw, 6, 40);
        int liquidCost = 2 + draw / 10, chargeCost = 1 + draw / 20;
        if (volume(pen) < liquidCost || VaporData.charge(pen) < chargeCost) return;
        if (!player.getAbilities().instabuild) {
            pen.set(VaporContent.VOLUME.get(), volume(pen) - liquidCost);
            pen.set(VaporContent.CHARGE.get(), VaporData.charge(pen) - chargeCost);
        }
        // Match the existing server toggle. Vapor never inherits leaf-quality regeneration.
        if (blend.nicotine() > 0 && TobacconistConfig.areNicotineEffectsEnabled()) {
            int duration = Math.clamp((int) ((long) blend.nicotine() * liquidCost / 500), 60, 800);
            player.addEffect(new MobEffectInstance(ModEffects.NICOTINE, duration, 0, false, false, true));
        }
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new VaporPuffPayload(player.getUUID(), blend.vegetablePercent(), draw));
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, .12F, 1.7F);
        player.getCooldowns().addCooldown(this, 10); player.getInventory().setChanged();
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LiquidBlend blend = VaporData.blend(stack);
        if (blend != null && volume(stack) > 0) tooltip.addAll(VaporData.description(blend));
        tooltip.add(Component.translatable("tobacconistmod.vapor.pen_status", volume(stack), VaporData.charge(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tobacconistmod.vapor.pen_controls").withStyle(ChatFormatting.DARK_GRAY));
    }
    @Override public boolean isBarVisible(ItemStack stack) { return volume(stack) > 0; }
    @Override public int getBarWidth(ItemStack stack) { return (int) Math.round(13.0 * volume(stack) / 250); }
    @Override public int getBarColor(ItemStack stack) { return 0x80BDAD; }
}
