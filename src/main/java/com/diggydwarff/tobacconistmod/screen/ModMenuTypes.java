package com.diggydwarff.tobacconistmod.screen;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, TobacconistMod.MODID);

    public static final Supplier<MenuType<HookahMenu>> HOOKAH_MENU =
            registerMenuType(HookahMenu::new, "hookah_menu");

    public static final Supplier<MenuType<FlueFireboxMenu>> FLUE_FIREBOX_MENU =
            registerMenuType(FlueFireboxMenu::new, "flue_firebox_menu");

    public static final Supplier<MenuType<ProductionMonitorMenu>> PRODUCTION_MONITOR_MENU =
            registerMenuType(ProductionMonitorMenu::new, "production_monitor_menu");

    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(
            IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
