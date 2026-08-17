package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.compat.curios.CuriosSmokingHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class NetworkEvents {
    private NetworkEvents() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                SmokeMouthItemPayload.TYPE,
                SmokeMouthItemPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (!ModList.get().isLoaded("curios")) return;
                    if (context.player() instanceof ServerPlayer player) {
                        CuriosSmokingHelper.trySmokeMouthItem(player);
                    }
                }
        );
    }
}
