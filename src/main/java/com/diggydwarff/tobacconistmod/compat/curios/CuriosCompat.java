package com.diggydwarff.tobacconistmod.compat.curios;

import net.neoforged.fml.ModList;

public class CuriosCompat {

    public static boolean loaded() {
        return ModList.get().isLoaded("curios");
    }

    public static void init() {
        if (!loaded()) return;
        MouthSlot.register();
    }
}