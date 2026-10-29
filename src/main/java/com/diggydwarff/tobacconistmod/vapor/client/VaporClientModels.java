package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.vapor.VaporContent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class VaporClientModels {
    private VaporClientModels() {}
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            var dyed = ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "dyed");
            for (var item : new net.minecraft.world.item.Item[]{VaporContent.VAPE_PEN.get(), VaporContent.BOX_MOD.get(), VaporContent.CIGALIKE.get()}) {
                ItemProperties.register(item, dyed, (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) ? 1F : 0F);
            }
        });
    }
}
