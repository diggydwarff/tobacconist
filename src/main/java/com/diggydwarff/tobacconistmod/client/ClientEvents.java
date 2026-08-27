package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.client.render.MouthCurioLayer;
import com.diggydwarff.tobacconistmod.client.render.SpectaclesRenderLayer;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void styleSecretBlendTooltip(RenderTooltipEvent.Color event) {
        if (!TobaccoBlendHelper.hasSecretBlendStyle(event.getItemStack())) return;

        if (TobaccoBlendHelper.isLegendarySecretBlend(event.getItemStack())) {
            // Legendary secrets use their own blend accent grading into gilt instead of the normal gold frame.
            event.setBackgroundStart(0xF00B0804);
            event.setBackgroundEnd(0xF0181207);
            event.setBorderStart(TobaccoBlendHelper.getLegendaryTooltipBorderStartArgb(event.getItemStack()));
            event.setBorderEnd(TobaccoBlendHelper.getLegendaryTooltipBorderEndArgb(event.getItemStack()));
            return;
        }

        // Almost-vanilla opacity, warmed slightly so the gold border feels built into the tooltip.
        event.setBackgroundStart(0xF0100C07);
        event.setBackgroundEnd(0xF0161008);
        // Bright antique gold at the top grading into bronze at the bottom: a restrained 1px gilt edge.
        event.setBorderStart(0xFFE7CB72);
        event.setBorderEnd(0xFF8F681F);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        boolean curiosLoaded = ModList.get().isLoaded("curios");

        for (PlayerSkin.Model skin : event.getSkins()) {
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