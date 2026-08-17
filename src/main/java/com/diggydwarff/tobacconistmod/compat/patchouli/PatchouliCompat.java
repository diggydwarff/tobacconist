package com.diggydwarff.tobacconistmod.compat.patchouli;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import vazkii.patchouli.api.PatchouliAPI;

/** Loaded only after the caller has confirmed Patchouli is installed. */
public final class PatchouliCompat {
    private static final ResourceLocation MANUAL_ID =
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "tobacco_manual");

    private PatchouliCompat() {}

    public static void openManual(ServerPlayer player) {
        PatchouliAPI.get().openBookGUI(player, MANUAL_ID);
    }
}
