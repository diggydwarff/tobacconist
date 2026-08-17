package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.network.TobacconistNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class ClientKeyInput {
    private ClientKeyInput() {}
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        if (ModList.get().isLoaded("curios")) ClientSmokingParticles.tick(minecraft);
        while (ClientKeyMappings.SMOKE_MOUTH_ITEM.consumeClick()) TobacconistNetwork.sendSmokeRequest();
    }
}
