package com.diggydwarff.tobacconistmod.compat.create.ponder;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Ponder entry point loaded only when Create is present on the client. */
public final class TobacconistPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return TobacconistMod.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        TobacconistPonderScenes.register(helper);
    }
}
