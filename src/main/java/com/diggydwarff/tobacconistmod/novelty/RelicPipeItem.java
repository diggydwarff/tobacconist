package com.diggydwarff.tobacconistmod.novelty;

import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.custom.WoodenSmokingPipeItem;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.SmokingDraws;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

/** Normal tobacco and pouch behavior, with a cosmetic flourish after successful draws. */
public final class RelicPipeItem extends WoodenSmokingPipeItem {
    private final NoveltyContent.Kind kind;
    public RelicPipeItem(Properties properties, NoveltyContent.Kind kind) { super(properties); this.kind = kind; }
    public NoveltyContent.Kind kind() { return kind; }
    @Override public boolean smokeFromMouthSlot(Player player, ServerLevel level, ItemStack stack, float strength) {
        return !player.getCooldowns().isOnCooldown(this) && super.smokeFromMouthSlot(player, level, stack, strength);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pipe = player.getItemInHand(hand);
        if (canSmoke(pipe) && player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(pipe);
        ItemStack tobacco = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (canPack(pipe) && tobacco.is(com.diggydwarff.tobacconistmod.datagen.items.ModTags.Items.LOOSE_TOBACCO)) {
            if (!level.isClientSide && packFromTobacco(pipe, tobacco) && !player.getAbilities().instabuild) tobacco.shrink(1);
            return InteractionResultHolder.sidedSuccess(pipe, level.isClientSide);
        }
        if (!canSmoke(pipe) && !(tobacco.getItem() instanceof com.diggydwarff.tobacconistmod.datagen.items.custom.TobaccoPouchItem)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("tobacconistmod.novelty.relic_pack"), true);
            return InteractionResultHolder.fail(pipe);
        }
        int before = puffs(pipe);
        var result = super.use(level, player, hand);
        if (!level.isClientSide && !TobacconistConfig.areSmokingDrawAnimationsEnabled() && puffs(pipe) < before) SmokingDraws.recover(player, pipe);
        return result;
    }
    private static int puffs(ItemStack pipe) { var tag = LegacyItemTags.getTag(pipe); return tag == null ? 0 : tag.getInt(NBT_PUFFS); }
    @Override public void triggerSmokingEffectPlayer(Player player, ServerLevel level, int smokeLevel, ItemStack tobacco) {
        triggerSmokingEffectPlayer(player, level, smokeLevel, tobacco, 1F);
    }
    @Override public void triggerSmokingEffectPlayer(Player player, ServerLevel level, int smokeLevel, ItemStack tobacco, float strength) {
        com.diggydwarff.tobacconistmod.datagen.items.SmokingItem.applyRelicSmokingEffects(player, level, tobacco, kind==NoveltyContent.Kind.WIZARD ? strength*.40F : strength);
        if (player instanceof ServerPlayer server) {
            int draw = Math.round(Math.clamp(strength, 0F, 1F) * 30);
            NoveltyEffects.queue(server, kind, draw);
            if (kind == NoveltyContent.Kind.WIZARD && draw >= 30) NoveltyAdvancements.ringmaster(server);
        }
    }
    @Override public boolean shouldEmitMouthSmoke(ItemStack stack) { return canSmoke(stack); }
    public String key() { return switch (kind) { case WIZARD -> "wizard"; case SOUL -> "soulglass"; case SUN -> "sunkeeper"; case ECHO -> "echo"; default -> "astral"; }; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable((kind == NoveltyContent.Kind.WIZARD || kind == NoveltyContent.Kind.SUN) ? "tobacconistmod.novelty.rare" : "tobacconistmod.novelty.mythic").withStyle((kind == NoveltyContent.Kind.WIZARD || kind == NoveltyContent.Kind.SUN) ? ChatFormatting.BLUE : ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tobacconistmod.novelty." + key() + "_lore").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tobacconistmod.novelty." + key() + "_source").withStyle(ChatFormatting.GRAY));
    }
}
