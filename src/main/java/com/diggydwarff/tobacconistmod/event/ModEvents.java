package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.screen.FlueFireboxScreen;
import com.diggydwarff.tobacconistmod.screen.HookahScreen;
import com.diggydwarff.tobacconistmod.screen.ModMenuTypes;
import com.diggydwarff.tobacconistmod.screen.ProductionMonitorScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(com.diggydwarff.tobacconistmod.vapor.VaporContent.STATION_MENU.get(), com.diggydwarff.tobacconistmod.vapor.client.LiquidStationScreen::new);
            MenuScreens.register(com.diggydwarff.tobacconistmod.vapor.VaporContent.PEN_MENU.get(), com.diggydwarff.tobacconistmod.vapor.client.VapePenScreen::new);
            MenuScreens.register(ModMenuTypes.HOOKAH_MENU.get(), HookahScreen::new);
            MenuScreens.register(ModMenuTypes.FLUE_FIREBOX_MENU.get(), FlueFireboxScreen::new);
            MenuScreens.register(ModMenuTypes.PRODUCTION_MONITOR_MENU.get(), ProductionMonitorScreen::new);
        });
    }
}
