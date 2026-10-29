package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.compat.curios.CuriosSmokingHelper;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.CigarItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.CigaretteItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.HookahHoseItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.WoodenSmokingPipeItem;
import com.diggydwarff.tobacconistmod.network.SmokingDrawStatePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import com.diggydwarff.tobacconistmod.backport.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Short-lived server draw state. Nothing here is stored in an item or a saved world. */
@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class SmokingDraws {
    public static final int MIN_DRAW_TICKS = 1;
    public static final int RECOVERY_TICKS = 10;
    public static final int EXHALE_DELAY_TICKS = 6;
    private static final Map<UUID, HookahDraw> HOOKAH_DRAWS = new HashMap<>();
    private static final Map<UUID, MouthDraw> MOUTH_DRAWS = new HashMap<>();
    private static final Map<UUID, Exhale> EXHALES = new HashMap<>();
    private static final Map<UUID, Integer> RECOVERY = new HashMap<>();

    private SmokingDraws() {}

    public static boolean isSupported(ItemStack stack) {
        return stack.getItem() instanceof CigaretteItem || stack.getItem() instanceof CigarItem
                || stack.getItem() instanceof WoodenSmokingPipeItem || stack.getItem() instanceof HookahHoseItem;
    }

    public static int duration(ItemStack stack) {
        if (com.diggydwarff.tobacconistmod.vapor.VapePenItem.isCigalike(stack)) return 40;
        if (stack.getItem() instanceof HookahHoseItem) return 50;
        if (stack.getItem() instanceof CigarItem) return 35;
        if (stack.getItem() instanceof CigaretteItem) return 25;
        return 30;
    }

    public static float strength(int elapsed, ItemStack stack) {
        return Math.max(0.0F, Math.min(1.0F, elapsed / (float) duration(stack)));
    }

    public static boolean canFinish(Player player) {
        return player.isAlive() && !player.isSpectator() && player.containerMenu == player.inventoryMenu;
    }

    public static boolean canStart(Player player, ItemStack stack) {
        return canFinish(player) && !player.isUsingItem()
                && !player.getCooldowns().isOnCooldown(stack.getItem())
                && (player.level().isClientSide() || (!MOUTH_DRAWS.containsKey(player.getUUID())
                && !RECOVERY.containsKey(player.getUUID())));
    }

    public static void recover(Player player, ItemStack stack) {
        player.getCooldowns().addCooldown(stack.getItem(), RECOVERY_TICKS);
        RECOVERY.put(player.getUUID(), RECOVERY_TICKS);
    }

    /** The block identifies the hookah; the hose then uses Minecraft's normal held-item timer. */
    public static InteractionResult startHookah(Level level, BlockPos pos, Player player, InteractionHand preferredHand) {
        if (!(level.getBlockEntity(pos) instanceof HookahEntity hookah)) return InteractionResult.PASS;
        for (InteractionHand hand : new InteractionHand[]{preferredHand,
                preferredHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND}) {
            ItemStack hose = player.getItemInHand(hand);
            if (!(hose.getItem() instanceof HookahHoseItem)) continue;
            // LIT is synchronized to clients; inventory and exact timer checks remain server-side.
            if (level.isClientSide() && !hookah.getMasterEntity().getBlockState().getValue(BlockStateProperties.LIT)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide() && (hookah.getMasterEntity().progress <= 0
                    || hookah.getShishaForSmoking().isEmpty())) return InteractionResult.PASS;
            if (!canStart(player, hose)) return InteractionResult.sidedSuccess(level.isClientSide());
            if (!TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    SmokingItem.applyHookahSmokingEffects(serverPlayer, serverPlayer.serverLevel(), hookah.getShishaForSmoking());
                    hookah.applyDirtyWaterPenalty(player);
                    player.getCooldowns().addCooldown(hose.getItem(), 20);
                }
            } else {
                if (!level.isClientSide()) {
                    HOOKAH_DRAWS.put(player.getUUID(), new HookahDraw(hookah.getMasterEntity(), hand));
                }
                player.startUsingItem(hand);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    public static void finishHookah(ServerPlayer player, ItemStack hose, int elapsed) {
        HookahDraw draw = HOOKAH_DRAWS.remove(player.getUUID());
        if (elapsed < MIN_DRAW_TICKS || draw == null || !validHookah(player, draw)
                || !player.isAlive() || player.isSpectator() || player.containerMenu != player.inventoryMenu) return;
        ItemStack shisha = draw.hookah.getShishaForSmoking();
        if (shisha.isEmpty()) return;
        SmokingItem.applyHookahSmokingEffects(player, player.serverLevel(), shisha, strength(elapsed, hose));
        draw.hookah.applyDirtyWaterPenalty(player);
        recover(player, hose);
    }

    private static boolean validHookah(ServerPlayer player, HookahDraw draw) {
        HookahEntity hookah = draw.hookah;
        return !hookah.isRemoved() && hookah.getLevel() == player.level() && hookah.progress > 0
                && player.distanceToSqr(hookah.getBlockPos().getCenter()) <= 36.0D
                && player.getUsedItemHand() == draw.hand
                && player.getItemInHand(draw.hand).getItem() instanceof HookahHoseItem;
    }

    public static void startMouth(ServerPlayer player, ItemStack stack) {
        if (!canStart(player, stack)) return;
        MOUTH_DRAWS.put(player.getUUID(), new MouthDraw(stack.copy(), duration(stack), player.level()));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SmokingDrawStatePayload(player.getId(), duration(stack)));
    }

    public static void cancelMouth(ServerPlayer player) {
        if (MOUTH_DRAWS.remove(player.getUUID()) != null) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SmokingDrawStatePayload(player.getId(), 0));
        }
    }

    public static void releaseMouth(ServerPlayer player) {
        MouthDraw draw = MOUTH_DRAWS.get(player.getUUID());
        if (draw == null) return;
        if (!TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            cancelMouth(player);
            return;
        }
        ItemStack stack = CuriosSmokingHelper.getMouthItem(player);
        int elapsed = duration(draw.snapshot) - draw.remaining;
        cancelMouth(player);
        int minimum = MIN_DRAW_TICKS;
        if (elapsed >= minimum && player.isAlive() && !player.isSpectator()
                && player.containerMenu == player.inventoryMenu && player.level() == draw.level
                && !player.isUsingItem() && ItemStack.matches(stack, draw.snapshot)) {
            ItemStack used = stack.copy();
            if (CuriosSmokingHelper.trySmokeMouthItem(player, strength(elapsed, used)) == null) recover(player, used);
        }
    }

    public static boolean isDrawingFromMouth(Player player) {
        return MOUTH_DRAWS.containsKey(player.getUUID());
    }

    public static void queueExhale(Player player, ItemStack tobacco, boolean hookah, float strength) {
        EXHALES.put(player.getUUID(), new Exhale(tobacco.copy(), hookah, strength, EXHALE_DELAY_TICKS, player.level()));
    }

    public static void drawSound(Level level, LivingEntity entity, ItemStack stack, int elapsed) {
        if (level.isClientSide() || elapsed <= 4) return;
        if (stack.getItem() instanceof com.diggydwarff.tobacconistmod.vapor.BubblePipeItem
                || stack.getItem() instanceof com.diggydwarff.tobacconistmod.novelty.ToyPipeItem) return;
        if (stack.getItem() instanceof HookahHoseItem) {
            if (elapsed % 10 == 5) level.playSound(null, entity.blockPosition(), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP,
                    SoundSource.PLAYERS, 0.12F, 0.65F + level.random.nextFloat() * 0.20F);
        } else if (elapsed == 8) {
            level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.08F, 1.4F);
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();
        Integer recovery = RECOVERY.get(id);
        if (recovery != null) {
            if (recovery <= 1) RECOVERY.remove(id);
            else RECOVERY.put(id, recovery - 1);
        }
        if (!player.isAlive() || player.isSpectator()) {
            clear(player);
            return;
        }
        HookahDraw hookah = HOOKAH_DRAWS.get(id);
        if (hookah != null && (!player.isUsingItem() || !validHookah(player, hookah)
                || player.containerMenu != player.inventoryMenu || !TobacconistConfig.areSmokingDrawAnimationsEnabled())) {
            HOOKAH_DRAWS.remove(id);
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof HookahHoseItem) player.stopUsingItem();
        }
        MouthDraw mouth = MOUTH_DRAWS.get(id);
        if (mouth != null) {
            ItemStack stack = CuriosSmokingHelper.getMouthItem(player);
            if (!TobacconistConfig.areSmokingDrawAnimationsEnabled() || player.level() != mouth.level
                    || player.isUsingItem() || player.containerMenu != player.inventoryMenu
                    || !ItemStack.matches(stack, mouth.snapshot)) {
                cancelMouth(player);
            } else {
                mouth.remaining--;
                if (mouth.remaining <= 0) releaseMouth(player);
                else drawSound(player.level(), player, stack, duration(stack) - mouth.remaining);
            }
        }
        Exhale exhale = EXHALES.get(id);
        if (exhale != null) {
            if (exhale.level != player.level()) EXHALES.remove(id);
            else if (--exhale.remaining <= 0) {
                EXHALES.remove(id);
                SmokingItem.emitSmokingSmoke(player, player.serverLevel(), exhale.tobacco, exhale.hookah, exhale.strength);
            }
        }
    }

    private static void clear(ServerPlayer player) {
        cancelMouth(player);
        HOOKAH_DRAWS.remove(player.getUUID());
        EXHALES.remove(player.getUUID());
        RECOVERY.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        HOOKAH_DRAWS.clear();
        MOUTH_DRAWS.clear();
        EXHALES.clear();
        RECOVERY.clear();
    }

    private record HookahDraw(HookahEntity hookah, InteractionHand hand) {}

    private static final class MouthDraw {
        private final ItemStack snapshot;
        private final Level level;
        private int remaining;

        private MouthDraw(ItemStack snapshot, int remaining, Level level) {
            this.snapshot = snapshot;
            this.remaining = remaining;
            this.level = level;
        }
    }

    private static final class Exhale {
        private final ItemStack tobacco;
        private final boolean hookah;
        private final float strength;
        private final Level level;
        private int remaining;

        private Exhale(ItemStack tobacco, boolean hookah, float strength, int remaining, Level level) {
            this.tobacco = tobacco;
            this.hookah = hookah;
            this.strength = strength;
            this.remaining = remaining;
            this.level = level;
        }
    }
}
