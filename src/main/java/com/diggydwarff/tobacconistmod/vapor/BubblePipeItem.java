package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.particle.ModParticles;
import net.minecraft.ChatFormatting;
import com.diggydwarff.tobacconistmod.backport.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import com.diggydwarff.tobacconistmod.backport.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import java.util.List;

/** A refillable toy: no tobacco, nicotine or smoking effects. */
public final class BubblePipeItem extends Item {
    public static final int MAX_BLOWS = 24;
    public BubblePipeItem(Properties properties) { super(properties); }
    public static int blows(ItemStack stack) { return com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(com.diggydwarff.tobacconistmod.backport.ComponentData.getOrDefault(stack, VaporContent.BUBBLE_CHARGE.get(), 0), 0, MAX_BLOWS); }

    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(com.diggydwarff.tobacconistmod.client.ClientDrawAnimations.extensions());
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pipe = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack source = player.getItemInHand(other);
            PotionContents potion = com.diggydwarff.tobacconistmod.backport.ComponentData.getOrDefault(source, DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            boolean mundane = source.is(Items.POTION) && potion.is(Potions.MUNDANE);
            if (blows(pipe) == MAX_BLOWS || !mundane) return InteractionResultHolder.fail(pipe);
            if (!level.isClientSide) {
                com.diggydwarff.tobacconistmod.backport.ComponentData.set(pipe, VaporContent.BUBBLE_CHARGE.get(), MAX_BLOWS);
                if (!player.getAbilities().instabuild) {
                    ItemStack empty = new ItemStack(Items.GLASS_BOTTLE);
                    if (source.getCount() == 1) player.setItemInHand(other, empty);
                    else { source.shrink(1); if (!player.addItem(empty)) player.drop(empty, false); }
                }
            }
            return InteractionResultHolder.sidedSuccess(pipe, level.isClientSide);
        }
        if (blows(pipe) == 0) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("tobacconistmod.vapor.bubble_refill_hint"), true);
            return InteractionResultHolder.fail(pipe);
        }
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(pipe);
        if (!com.diggydwarff.tobacconistmod.config.TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            if (!level.isClientSide) blow(pipe, player, 30);
            return InteractionResultHolder.consume(pipe);
        }
        player.startUsingItem(hand); return InteractionResultHolder.consume(pipe);
    }
    @Override public int getUseDuration(ItemStack stack) { return 30; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!level.isClientSide && entity instanceof Player player
                && !com.diggydwarff.tobacconistmod.util.SmokingDraws.canFinish(player)) player.stopUsingItem();
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) { blow(stack, entity, 30 - remaining); }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) { blow(stack, entity, 30); return stack; }
    public boolean blowFromMouth(ServerPlayer player, ItemStack stack, float strength) {
        return blow(stack, player, Math.round(com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(strength, 0F, 1F) * 30));
    }
    private boolean blow(ItemStack stack, LivingEntity entity, int draw) {
        if (!(entity instanceof ServerPlayer player) || !com.diggydwarff.tobacconistmod.util.SmokingDraws.canFinish(player) || draw < 4 || blows(stack) == 0 || player.getCooldowns().isOnCooldown(this)) return false;
        if (!player.getAbilities().instabuild) com.diggydwarff.tobacconistmod.backport.ComponentData.set(stack, VaporContent.BUBBLE_CHARGE.get(), blows(stack) - 1);
        var origin = player.getEyePosition().add(player.getLookAngle().scale(.35)).add(0, -.15, 0);
        player.getLevel().sendParticles(ModParticles.SOAP_BUBBLE.get(), origin.x, origin.y, origin.z,
                3 + com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(draw, 4, 30) / 3, .08, .03, .08, .035);
        player.getLevel().playSound(null, player.blockPosition(), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.PLAYERS, .25F, 1.3F);
        VaporAdvancements.onBubbles(player); player.getCooldowns().addCooldown(this, 10); player.getInventory().setChanged();
        return true;
    }
    @Override public void appendHoverText(ItemStack stack, Level context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tobacconistmod.vapor.bubble_status", blows(stack), MAX_BLOWS).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tobacconistmod.vapor.bubble_refill_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
