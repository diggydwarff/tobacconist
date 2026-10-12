package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.fluid.EssenceFlavorColors;
import com.diggydwarff.tobacconistmod.vapor.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class VaporColors {
    private VaporColors() {}
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, index) -> {
            if (index != 0) return -1;
            if (stack.is(VaporContent.CRUDE_EXTRACT.get())) return 0xFF71552C;
            LiquidBlend blend = VaporData.blend(stack);
            if (blend == null) return 0xFFE8D8AD;
            if (!blend.flavors().isEmpty()) {
                long red = 0, green = 0, blue = 0, total = 0;
                for (var entry : blend.flavors().entrySet()) for (var flavor : BottledMolassesFlavors.values()) {
                    if (!flavor.isPlain() && flavor.getFlavorPath().equals(entry.getKey())) {
                        int color = EssenceFlavorColors.fluidTint(flavor), weight = entry.getValue();
                        red += ((color >> 16) & 255L) * weight; green += ((color >> 8) & 255L) * weight; blue += (color & 255L) * weight; total += weight;
                    }
                }
                if (total > 0) return 0xFF000000 | (int) (red / total) << 16 | (int) (green / total) << 8 | (int) (blue / total);
            }
            if (blend.nicotine() == LiquidBlend.SCALE) return 0xFF9CBA68;
            return blend.vegetablePercent() >= 50 ? 0xFFE8DF9C : 0xFFD8EDF1;
        }, VaporContent.VEGETABLE_BASE.get(), VaporContent.CLEAR_BASE.get(), VaporContent.CRUDE_EXTRACT.get(), VaporContent.NICOTINE_EXTRACT.get(), VaporContent.VAPE_LIQUID.get());
    }
}
