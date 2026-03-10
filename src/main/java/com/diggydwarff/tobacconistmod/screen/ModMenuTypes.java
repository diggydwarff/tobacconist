package com.diggydwarff.tobacconistmod.screen;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TobacconistMod.MODID);

    public static final RegistryObject<MenuType<HookahMenu>> HOOKAH_MENU =
            registerMenuType(HookahMenu::new, "hookah_menu");

    public static final RegistryObject<MenuType<FlueFireboxMenu>> FLUE_FIREBOX_MENU =
            MENUS.register("flue_firebox_menu", () -> IForgeMenuType.create(FlueFireboxMenu::new));

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory,
                                                                                                  String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
