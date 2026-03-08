package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.client.render.MouthCurioLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {

        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);

            renderer.addLayer(new MouthCurioLayer(renderer, Minecraft.getInstance().getItemRenderer()));
        }
    }
}