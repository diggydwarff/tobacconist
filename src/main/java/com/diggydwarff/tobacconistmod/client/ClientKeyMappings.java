package com.diggydwarff.tobacconistmod.client;

import net.minecraftforge.fml.common.Mod;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientKeyMappings {
    private ClientKeyMappings() {}

    public static final KeyMapping SMOKE_MOUTH_ITEM = new KeyMapping(
            "key.tobacconistmod.smoke_mouth_item",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.tobacconistmod"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SMOKE_MOUTH_ITEM);
    }
}
