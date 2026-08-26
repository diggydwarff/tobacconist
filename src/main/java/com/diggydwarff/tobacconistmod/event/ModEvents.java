package com.diggydwarff.tobacconistmod.event;

import net.minecraftforge.fml.common.Mod;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.screen.FlueFireboxScreen;
import com.diggydwarff.tobacconistmod.screen.HookahScreen;
import com.diggydwarff.tobacconistmod.screen.ModMenuTypes;
import com.diggydwarff.tobacconistmod.screen.ProductionMonitorScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterMenuScreensEvent;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.HOOKAH_MENU.get(), HookahScreen::new);
        event.register(ModMenuTypes.FLUE_FIREBOX_MENU.get(), FlueFireboxScreen::new);
        event.register(ModMenuTypes.PRODUCTION_MONITOR_MENU.get(), ProductionMonitorScreen::new);
    }
}
