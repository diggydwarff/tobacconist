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
import com.diggydwarff.tobacconistmod.backport.PacketDistributor;
import java.util.List;

public final class VapePenItem extends Item implements net.minecraft.world.item.DyeableLeatherItem {
    public enum Kind {
        PEN(100), BOX_MOD(150), CIGALIKE(75);

        private final int capacity;
        Kind(int capacity) { this.capacity = capacity; }
    }

    private final Kind kind;

    public VapePenItem(Properties properties) { this(properties, Kind.PEN); }
    public VapePenItem(Properties properties, Kind kind) { super(properties); this.kind = kind; }

    public static boolean isDevice(ItemStack stack) { return stack.getItem() instanceof VapePenItem; }
    public static boolean hasPowerControl(ItemStack stack) {
        return stack.getItem() instanceof VapePenItem device && device.kind == Kind.BOX_MOD;
    }
    public static boolean isCigalike(ItemStack stack) {
        return stack.getItem() instanceof VapePenItem device && device.kind == Kind.CIGALIKE;
    }
    public static int maxCharge(ItemStack stack) { return isCigalike(stack) ? 40 : 100; }
    public static int charge(ItemStack stack) { return Math.min(VaporData.charge(stack), maxCharge(stack)); }
    public static int chargePercent(ItemStack stack) { return charge(stack) * 100 / maxCharge(stack); }
    public static int capacity(ItemStack stack) {
        return stack.getItem() instanceof VapePenItem device ? device.kind.capacity : VaporData.TANK;
    }
    public static int volume(ItemStack stack) {
        // Preserve legacy fills above the smaller tank limit; they drain normally and cannot refill.
        return com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(com.diggydwarff.tobacconistmod.backport.ComponentData.getOrDefault(stack, VaporContent.VOLUME.get(), 0), 0, 1000);
    }
    public static int wattage(ItemStack stack) {
        return hasPowerControl(stack) ? com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(com.diggydwarff.tobacconistmod.backport.ComponentData.getOrDefault(stack, VaporContent.WATTAGE.get(), 20) / 10, 1, 4) * 10 : isCigalike(stack) ? 8 : 20;
    }
    public static int puffCount(ItemStack stack) { return Math.max(0, com.diggydwarff.tobacconistmod.backport.ComponentData.getOrDefault(stack, VaporContent.PUFF_COUNT.get(), 0)); }

    /** A server-selected output scale; no client-supplied power affects consumption. */
    public static int outputPercent(ItemStack stack) {
        if (hasPowerControl(stack)) return 50 + wattage(stack) * 5 / 2;
        return isCigalike(stack) ? 40 : 100;
    }
    public static int liquidCost(ItemStack stack, int draw) {
        int base = 2 + com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(draw, 6, 40) / 10;
        return Math.max(1, (base * outputPercent(stack) + 99) / 100);
    }
    public static int chargeCost(ItemStack stack, int draw) {
        int base = 1 + com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(draw, 6, 40) / 20;
        return Math.max(1, (base * outputPercent(stack) + 99) / 100);
    }
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(com.diggydwarff.tobacconistmod.client.ClientDrawAnimations.extensions());
    }

    @Override public int getUseDuration(ItemStack stack) { return 40; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pen = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer server) {
                int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
                net.minecraftforge.network.NetworkHooks.openScreen(server, new SimpleMenuProvider((id, inventory, ignored) -> new VapePenMenu(id, inventory, slot), getName(pen)), buf -> buf.writeVarInt(slot));
            }
            return InteractionResultHolder.sidedSuccess(pen, level.isClientSide);
        }
        LiquidBlend blend = VaporData.blend(pen);
        if (blend == null || !blend.usable() || volume(pen) < liquidCost(pen, 6)
                || charge(pen) < chargeCost(pen, 6)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("tobacconistmod.vapor.refill_hint"), true);
            return InteractionResultHolder.fail(pen);
        }
        player.startUsingItem(hand); return InteractionResultHolder.consume(pen);
    }
    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!level.isClientSide && entity instanceof Player player
                && !com.diggydwarff.tobacconistmod.util.SmokingDraws.canFinish(player)) player.stopUsingItem();
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) { puff(stack, level, entity, 40 - remaining); }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) { puff(stack, level, entity, 40); return stack; }
    private boolean puff(ItemStack pen, Level level, LivingEntity entity, int draw) {
        if (!(entity instanceof ServerPlayer player) || !com.diggydwarff.tobacconistmod.util.SmokingDraws.canFinish(player) || draw < 6 || player.getCooldowns().isOnCooldown(this)) return false;
        LiquidBlend blend = VaporData.blend(pen);
        if (blend == null || !blend.usable()) return false;
        draw = com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(draw, 6, 40);
        int liquidCost = liquidCost(pen, draw), chargeCost = chargeCost(pen, draw);
        if (volume(pen) < liquidCost || charge(pen) < chargeCost) return false;
        if (!player.getAbilities().instabuild) {
            com.diggydwarff.tobacconistmod.backport.ComponentData.set(pen, VaporContent.VOLUME.get(), volume(pen) - liquidCost);
            com.diggydwarff.tobacconistmod.backport.ComponentData.set(pen, VaporContent.CHARGE.get(), charge(pen) - chargeCost);
        }
        // Count completed puffs only; saturate rather than wrapping on old devices.
        com.diggydwarff.tobacconistmod.backport.ComponentData.set(pen, VaporContent.PUFF_COUNT.get(), puffCount(pen) == Integer.MAX_VALUE ? Integer.MAX_VALUE : puffCount(pen) + 1);
        // Match the existing server toggle. Vapor never inherits leaf-quality regeneration.
        if (blend.nicotine() > 0 && TobacconistConfig.areNicotineEffectsEnabled()) {
            int duration = com.diggydwarff.tobacconistmod.backport.BackportMath.clamp((int) ((long) blend.nicotine() * liquidCost / 500), 60, 800);
            player.addEffect(new MobEffectInstance(ModEffects.NICOTINE.get(), duration, 0, false, false, true));
        }
        VaporAdvancements.onPuff(player, blend, draw);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new VaporPuffPayload(player.getUUID(), blend.vegetablePercent(), draw, outputPercent(pen)));
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, .12F, 1.7F);
        player.getCooldowns().addCooldown(this, 10); player.getInventory().setChanged();
        return true;
    }
    public static boolean canUseFromMouth(ItemStack stack) {
        LiquidBlend blend = VaporData.blend(stack);
        return isCigalike(stack) && blend != null && blend.usable()
                && volume(stack) >= liquidCost(stack, 6) && charge(stack) >= chargeCost(stack, 6);
    }
    public boolean puffFromMouth(ServerPlayer player, ItemStack stack, float strength) {
        return isCigalike(stack) && puff(stack, player.getLevel(), player, Math.round(com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(strength, 0F, 1F) * 40));
    }
    @Override public void appendHoverText(ItemStack stack, Level context, List<Component> tooltip, TooltipFlag flag) {
        LiquidBlend blend = VaporData.blend(stack);
        if (blend != null && volume(stack) > 0) tooltip.addAll(VaporData.description(blend));
        tooltip.add(Component.translatable("tobacconistmod.vapor.device_status", volume(stack), capacity(stack), chargePercent(stack)).withStyle(ChatFormatting.GRAY));
        if (volume(stack) > capacity(stack)) tooltip.add(Component.translatable("tobacconistmod.vapor.legacy_fill").withStyle(ChatFormatting.DARK_GRAY));
        if (hasPowerControl(stack)) {
            tooltip.add(Component.translatable("tobacconistmod.vapor.box_status", wattage(stack), puffCount(stack)).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (isCigalike(stack)) tooltip.add(Component.translatable("tobacconistmod.vapor.cigalike_detail").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tobacconistmod.vapor.dye_hint").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tobacconistmod.vapor.pen_controls").withStyle(ChatFormatting.DARK_GRAY));
    }
    @Override public boolean isBarVisible(ItemStack stack) { return volume(stack) > 0; }
    @Override public int getBarWidth(ItemStack stack) { return com.diggydwarff.tobacconistmod.backport.BackportMath.clamp((int) Math.round(13.0 * volume(stack) / capacity(stack)), 0, 13); }
    @Override public int getBarColor(ItemStack stack) { return 0x80BDAD; }
}
