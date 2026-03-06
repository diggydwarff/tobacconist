package com.diggydwarff.tobacconistmod.handlers;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientColorHandlers {

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {

        event.register((stack, tintIndex) -> {

            // Only tint layer0
            if (tintIndex != 0) return 0xFFFFFF;

            var tag = stack.getTag();
            if (tag == null || !tag.contains(WoodenPipeRecipe.NBT_WOOD_PLANK))
                return 0xFFFFFF;

            ResourceLocation id =
                    new ResourceLocation(tag.getString(WoodenPipeRecipe.NBT_WOOD_PLANK));

            var item = BuiltInRegistries.ITEM.get(id);

            if (item instanceof BlockItem bi) {

                var state = bi.getBlock().defaultBlockState();

                int color =
                        state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).col;

                // Extract RGB
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;

                // Saturation boost
                int avg = (r + g + b) / 3;
                float darken = 0.9F;  // lower for deeper wood tone

                r = clamp((int)(avg + (r - avg)));
                g = clamp((int)(avg + (g - avg)));
                b = clamp((int)(avg + (b - avg)));

                r = clamp((int)(r * darken));
                g = clamp((int)(g * darken));
                b = clamp((int)(b * darken));

                return (r << 16) | (g << 8) | b;
            }

            return 0xFFFFFF;

        }, BuiltInRegistries.ITEM.get(new ResourceLocation("tobacconistmod", "wooden_smoking_pipe")));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

}
