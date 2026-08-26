package com.diggydwarff.tobacconistmod.client;

import net.minecraftforge.fml.common.Mod;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.client.render.MouthCurioLayer;
import com.diggydwarff.tobacconistmod.client.render.SpectaclesRenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        boolean curiosLoaded = ModList.get().isLoaded("curios");

        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                // Spectacles work with or without Curios (Curios Head or vanilla helmet slot).
                renderer.addLayer(new SpectaclesRenderLayer(renderer, Minecraft.getInstance().getItemRenderer()));

                // Mouth-slot rendering only exists when Curios supplies that slot.
                if (curiosLoaded) {
                    renderer.addLayer(new MouthCurioLayer(renderer, Minecraft.getInstance().getItemRenderer()));
                }
            }
        }
    }
}
