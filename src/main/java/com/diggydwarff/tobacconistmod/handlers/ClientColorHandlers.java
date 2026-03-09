package com.diggydwarff.tobacconistmod.handlers;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.HookahBlock;
import com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientColorHandlers {

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 0) return 0xFFFFFF;

            DyeColor color = state.getValue(HookahBlock.COLOR);
            int c = color.getTextColor();

            int r = (c >> 16) & 255;
            int g = (c >> 8) & 255;
            int b = c & 255;

            float strength = 0.6f; // lower = less intense

            r = (int)(255 * (1 - strength) + r * strength);
            g = (int)(255 * (1 - strength) + g * strength);
            b = (int)(255 * (1 - strength) + b * strength);

            return (r << 16) | (g << 8) | b;
        }, ModBlocks.HOOKAH.get());
    }

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

                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;

                int avg = (r + g + b) / 3;
                float darken = 0.9F;

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

        event.register((stack, tintIndex) -> {
            if (tintIndex != 0) return 0xFFFFFF;

            // default inventory color for undyed/base hookah item
            return 0xB0B0B0;
        }, ModBlocks.HOOKAH.get());
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}