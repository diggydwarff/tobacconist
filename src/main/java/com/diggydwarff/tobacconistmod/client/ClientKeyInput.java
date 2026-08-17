package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.network.SmokeMouthItemPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class ClientKeyInput {
    private ClientKeyInput() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        while (ClientKeyMappings.SMOKE_MOUTH_ITEM.consumeClick()) {
            PacketDistributor.sendToServer(SmokeMouthItemPayload.INSTANCE);
        }
    }
}
