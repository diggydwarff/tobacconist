package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.network.SmokeMouthItemPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class ClientKeyInput {
    private static boolean wasDrawing;
    private ClientKeyInput() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            wasDrawing = false;
            return;
        }

        // Keep Curios-linked classes out of automatic event subscription so Curios
        // can remain a genuinely optional dependency.
        if (ModList.get().isLoaded("curios")) {
            ClientSmokingParticles.tick(minecraft);
        }

        if (TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            // Edge-triggered start/stop: holding the key takes one draw, and release must precede another.
            boolean drawing = minecraft.screen == null && !minecraft.isPaused()
                    && ClientKeyMappings.SMOKE_MOUTH_ITEM.isDown();
            if (drawing != wasDrawing) {
                PacketDistributor.sendToServer(drawing ? SmokeMouthItemPayload.START : SmokeMouthItemPayload.STOP);
                wasDrawing = drawing;
            }
            while (ClientKeyMappings.SMOKE_MOUTH_ITEM.consumeClick()) { /* Drain legacy click events. */ }
        } else {
            if (wasDrawing) {
                PacketDistributor.sendToServer(SmokeMouthItemPayload.STOP);
                wasDrawing = false;
            }
            while (ClientKeyMappings.SMOKE_MOUTH_ITEM.consumeClick()) {
                PacketDistributor.sendToServer(SmokeMouthItemPayload.START);
            }
        }
    }
}
