package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.custom.DoubleHookahBlock;
import com.diggydwarff.tobacconistmod.block.custom.HookahBlock;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Handles Creative-mode drop suppression and dropped-hookah item protection. */
@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class HookahDropProtectionEvents {
    private HookahDropProtectionEvents() {}

    /** Tracks recent Creative breaks so drops from a two-block partner can also be suppressed. */
    private static final Map<ServerLevel, Map<BlockPos, Long>> CREATIVE_BREAK_POSITIONS = new WeakHashMap<>();
    private static final long CREATIVE_BREAK_GUARD_TICKS = 2L;

    @SubscribeEvent
    public static void suppressCreativeHookahContents(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().getAbilities().instabuild || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockState state = event.getState();
        boolean tall = state.getBlock() instanceof DoubleHookahBlock;
        if (!tall && !(state.getBlock() instanceof HookahBlock)) {
            return;
        }

        BlockPos lowerPos = event.getPos();
        if (tall && state.hasProperty(DoubleHookahBlock.HALF)
                && state.getValue(DoubleHookahBlock.HALF) == DoubleBlockHalf.UPPER) {
            lowerPos = lowerPos.below();
        }

        BlockEntity blockEntity = level.getBlockEntity(lowerPos);
        if (blockEntity instanceof HookahEntity hookah) {
            hookah.clearContentsForCreativeBreak();
        }

        markCreativeBreak(level, lowerPos);
        if (tall) {
            markCreativeBreak(level, lowerPos.above());
        }

        // Let normal block removal run; drop events handle suppression for both halves.
    }

    @SubscribeEvent
    public static void suppressCreativeHookahBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof net.minecraft.world.entity.player.Player player)
                || !player.getAbilities().instabuild) {
            return;
        }

        if (isHookah(event.getState())) {
            event.getDrops().clear();
            event.setDroppedExperience(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void protectDroppedHookahs(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)
                || !(itemEntity.getItem().getItem() instanceof BlockItem blockItem)
                || !isHookah(blockItem.getBlock().defaultBlockState())) {
            return;
        }

        // Suppress secondary-half loot created immediately after a Creative break.
        if (event.getLevel() instanceof ServerLevel serverLevel
                && wasJustBrokenInCreative(serverLevel, itemEntity.blockPosition())) {
            event.setCanceled(true);
            return;
        }

        // Protect legitimate dropped Hookah items from environmental damage.
        itemEntity.setInvulnerable(true);
    }

    private static boolean isHookah(BlockState state) {
        return state.getBlock() instanceof HookahBlock || state.getBlock() instanceof DoubleHookahBlock;
    }

    private static void markCreativeBreak(ServerLevel level, BlockPos pos) {
        CREATIVE_BREAK_POSITIONS
                .computeIfAbsent(level, ignored -> new HashMap<>())
                .put(pos.immutable(), level.getGameTime());
    }

    private static boolean wasJustBrokenInCreative(ServerLevel level, BlockPos itemPos) {
        Map<BlockPos, Long> positions = CREATIVE_BREAK_POSITIONS.get(level);
        if (positions == null || positions.isEmpty()) {
            return false;
        }

        long now = level.getGameTime();
        positions.entrySet().removeIf(entry -> now - entry.getValue() > CREATIVE_BREAK_GUARD_TICKS);

        // Include vertical neighbors because either half may spawn the item entity.
        return positions.containsKey(itemPos)
                || positions.containsKey(itemPos.below())
                || positions.containsKey(itemPos.above());
    }
}
