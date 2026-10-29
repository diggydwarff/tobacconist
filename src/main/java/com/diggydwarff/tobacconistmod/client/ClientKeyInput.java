package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.network.SmokeMouthItemPayload;
import net.minecraft.client.Minecraft;
import com.diggydwarff.tobacconistmod.backport.PacketDistributor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class ClientKeyInput {
    private static boolean wasDrawing;
    private ClientKeyInput() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            wasDrawing = false;
            return;
        }

        if (ModList.get().isLoaded("curios")) {
            ClientSmokingParticles.tick(minecraft);
        }

        if (TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            // Edge-triggered start/stop: holding the key takes one draw, and release must precede another.
            boolean drawing = minecraft.screen == null && !minecraft.isPaused()
                    && ClientKeyMappings.SMOKE_MOUTH_ITEM.isDown();
            if (drawing != wasDrawing) {
                boolean cancel = minecraft.screen != null || minecraft.isPaused();
                PacketDistributor.sendToServer(drawing ? SmokeMouthItemPayload.START
                        : cancel ? SmokeMouthItemPayload.CANCEL : SmokeMouthItemPayload.STOP);
                wasDrawing = drawing;
            }
            while (ClientKeyMappings.SMOKE_MOUTH_ITEM.consumeClick()) { /* Drain legacy click events. */ }
        } else {
            if (wasDrawing) {
                PacketDistributor.sendToServer(SmokeMouthItemPayload.CANCEL);
                wasDrawing = false;
            }
            while (ClientKeyMappings.SMOKE_MOUTH_ITEM.consumeClick()) {
                PacketDistributor.sendToServer(SmokeMouthItemPayload.START);
            }
        }
    }
}
