package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.compat.curios.CuriosSmokingHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class NetworkEvents {
    private static final long MANUAL_SMOKE_COOLDOWN_TICKS = 8L;
    private static final Map<UUID, Long> LAST_MANUAL_SMOKE_TICK = new HashMap<>();

    private NetworkEvents() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                SmokeMouthItemPayload.TYPE,
                SmokeMouthItemPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;

                    if (!ModList.get().isLoaded("curios")) {
                        player.displayClientMessage(Component.literal("Manual mouth-slot smoking requires Curios."), true);
                        return;
                    }

                    long now = player.serverLevel().getGameTime();
                    long last = LAST_MANUAL_SMOKE_TICK.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2);
                    if (now - last < MANUAL_SMOKE_COOLDOWN_TICKS) {
                        return;
                    }

                    String failure = CuriosSmokingHelper.trySmokeMouthItem(player);
                    if (failure == null) {
                        LAST_MANUAL_SMOKE_TICK.put(player.getUUID(), now);
                    } else {
                        player.displayClientMessage(Component.literal(failure), true);
                    }
                }
        );
    }
}
