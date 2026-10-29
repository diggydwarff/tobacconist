package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.vapor.VaporContent;
import net.minecraft.client.renderer.item.ItemProperties;
import com.diggydwarff.tobacconistmod.backport.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class VaporClientModels {
    private VaporClientModels() {}
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            var dyed = new ResourceLocation(TobacconistMod.MODID, "dyed");
            for (var item : new net.minecraft.world.item.Item[]{VaporContent.VAPE_PEN.get(), VaporContent.BOX_MOD.get(), VaporContent.CIGALIKE.get()}) {
                ItemProperties.register(item, dyed, (stack, level, entity, seed) -> com.diggydwarff.tobacconistmod.backport.ComponentData.has(stack, DataComponents.DYED_COLOR) ? 1F : 0F);
            }
        });
    }
}
