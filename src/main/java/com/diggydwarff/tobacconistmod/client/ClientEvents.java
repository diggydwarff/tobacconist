package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.client.render.MouthCurioLayer;
import com.diggydwarff.tobacconistmod.client.render.SpectaclesRenderLayer;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

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

    /** Forge-bus client events cannot live on the MOD-bus subscriber above. */
    @Mod.EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
    public static final class ForgeClientEvents {
        private ForgeClientEvents() {}

        @SubscribeEvent
        public static void styleSecretBlendTooltip(RenderTooltipEvent.Color event) {
            if (!TobaccoBlendHelper.hasSecretBlendStyle(event.getItemStack())) return;

            if (TobaccoBlendHelper.isLegendarySecretBlend(event.getItemStack())) {
                event.setBackgroundStart(0xF00B0804);
                event.setBackgroundEnd(0xF0181207);
                event.setBorderStart(TobaccoBlendHelper.getLegendaryTooltipBorderStartArgb(event.getItemStack()));
                event.setBorderEnd(TobaccoBlendHelper.getLegendaryTooltipBorderEndArgb(event.getItemStack()));
                return;
            }

            event.setBackgroundStart(0xF0100C07);
            event.setBackgroundEnd(0xF0161008);
            event.setBorderStart(0xFFE7CB72);
            event.setBorderEnd(0xFF8F681F);
        }
    }
}
