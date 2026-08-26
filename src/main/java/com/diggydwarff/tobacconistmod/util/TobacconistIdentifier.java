package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.resources.ResourceLocation;

public final class TobacconistIdentifier {
    private TobacconistIdentifier() {}

    public static ResourceLocation of(String path) {
        return new ResourceLocation(TobacconistMod.MODID, path);
    }
}
