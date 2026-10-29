package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.compat.curios.CuriosSmokingHelper;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.SmokingDraws;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="tobacconistmod")
public final class NetworkEvents {
    private static final Map<UUID,Long> LAST_MANUAL_SMOKE_TICK=new HashMap<>();
    private NetworkEvents() {}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { LAST_MANUAL_SMOKE_TICK.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) { LAST_MANUAL_SMOKE_TICK.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { LAST_MANUAL_SMOKE_TICK.clear(); }

    public static void handleMouth(ServerPlayer player,SmokeMouthItemPayload payload) {
        if (!ModList.get().isLoaded("curios")) {
            player.displayClientMessage(Component.translatable("tobacconistmod.message.mouth_slot_requires_curios"),true); return;
        }
        if (!payload.drawing()) {
            if (payload.cancel()) SmokingDraws.cancelMouth(player); else SmokingDraws.releaseMouth(player);
            return;
        }
        if (TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            var stack=CuriosSmokingHelper.getMouthItem(player);
            if (!CuriosSmokingHelper.supported(stack)) player.displayClientMessage(Component.translatable("tobacconistmod.message.mouth.empty"),true);
            else if (!CuriosSmokingHelper.canDraw(stack)) player.displayClientMessage(Component.translatable("tobacconistmod.message.mouth.no_puffs"),true);
            else SmokingDraws.startMouth(player,stack);
            return;
        }
        if (!SmokingDraws.canStart(player,CuriosSmokingHelper.getMouthItem(player))) return;
        long now=player.serverLevel().getGameTime(),last=LAST_MANUAL_SMOKE_TICK.getOrDefault(player.getUUID(),Long.MIN_VALUE/2);
        if (now-last<8) return;
        Component failure=CuriosSmokingHelper.trySmokeMouthItem(player);
        if (failure==null) LAST_MANUAL_SMOKE_TICK.put(player.getUUID(),now); else player.displayClientMessage(failure,true);
    }
}
