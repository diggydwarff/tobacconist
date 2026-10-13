package com.diggydwarff.tobacconistmod.novelty;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;

/** Refillable toys share draw controls, but never enter the tobacco/effect pipeline. */
public final class ToyPipeItem extends Item {
    private final NoveltyContent.Kind kind;
    public ToyPipeItem(Properties properties, NoveltyContent.Kind kind) { super(properties); this.kind = kind; }
    public NoveltyContent.Kind kind() { return kind; }
    public String refillKey() {
        return "tobacconistmod.novelty." + switch(kind) { case PETAL -> "petal_refill"; case WHISTLE -> "whistle_refill"; default -> "confetti_refill"; };
    }
    private boolean acceptsRefill(ItemStack source) {
        return switch(kind) {
            case PETAL -> source.is(net.minecraft.tags.ItemTags.SMALL_FLOWERS);
            case WHISTLE -> source.is(Items.POTION) && source.getOrDefault(DataComponents.POTION_CONTENTS,
                    net.minecraft.world.item.alchemy.PotionContents.EMPTY).is(net.minecraft.world.item.alchemy.Potions.WATER);
            default -> source.is(Items.PAPER);
        };
    }
    public static ItemStack loaded(Item item) { ItemStack stack = new ItemStack(item); stack.set(NoveltyContent.CHARGES.get(), 24); return stack; }
    public static int charges(ItemStack stack) { return Math.clamp(stack.getOrDefault(NoveltyContent.CHARGES.get(), 0), 0, 24); }
    public static boolean canBlow(ItemStack stack) { return stack.getItem() instanceof ToyPipeItem && charges(stack) > 0; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pipe = player.getItemInHand(hand);
        ItemStack source = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        boolean refillSource = acceptsRefill(source);
        if (player.isShiftKeyDown() || charges(pipe) == 0 && refillSource) {
            boolean refill = refillSource;
            if (!refill || charges(pipe) == 24) return InteractionResultHolder.fail(pipe);
            if (!level.isClientSide) {
                pipe.set(NoveltyContent.CHARGES.get(), 24);
                pipe.remove(DataComponents.DYED_COLOR);
                if (!player.getAbilities().instabuild) {
                    source.shrink(1);
                    if(kind==NoveltyContent.Kind.WHISTLE) {
                        ItemStack glass=new ItemStack(Items.GLASS_BOTTLE);
                        InteractionHand other=hand==InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                        if(source.isEmpty()) player.setItemInHand(other,glass);
                        else if(!player.addItem(glass)) player.drop(glass,false);
                    }
                }
            }
            return InteractionResultHolder.sidedSuccess(pipe, level.isClientSide);
        }
        if (!canBlow(pipe)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable(refillKey()), true);
            return InteractionResultHolder.fail(pipe);
        }
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(pipe);
        if (!com.diggydwarff.tobacconistmod.config.TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            if (!level.isClientSide) blow(pipe, player, 30);
            return InteractionResultHolder.consume(pipe);
        }
        player.startUsingItem(hand); return InteractionResultHolder.consume(pipe);
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 30; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!level.isClientSide && entity instanceof Player player && !com.diggydwarff.tobacconistmod.util.SmokingDraws.canFinish(player)) player.stopUsingItem();
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) { blow(stack, entity, 30 - remaining); }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) { blow(stack, entity, 30); return stack; }
    public boolean blowFromMouth(ServerPlayer player, ItemStack stack, float strength) { return blow(stack, player, Math.round(Math.clamp(strength, 0F, 1F) * 30)); }
    private boolean blow(ItemStack stack, LivingEntity entity, int draw) {
        if (!(entity instanceof ServerPlayer player) || draw < 4 || !canBlow(stack) || !com.diggydwarff.tobacconistmod.util.SmokingDraws.canFinish(player) || player.getCooldowns().isOnCooldown(this)) return false;
        if (!player.getAbilities().instabuild) stack.set(NoveltyContent.CHARGES.get(), charges(stack) - 1);
        NoveltyEffects.emit(player, kind, draw, 0);
        player.getCooldowns().addCooldown(this, 10); player.getInventory().setChanged(); return true;
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tobacconistmod.novelty.toy_status", charges(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(refillKey()).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tobacconistmod.novelty.toy_controls").withStyle(ChatFormatting.DARK_GRAY));
    }
}
