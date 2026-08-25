package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** Client-only item-model predicates for loose tobacco cut types. Ribbon is the default model. */
public final class LooseTobaccoModelProperties {
    private static final ResourceLocation SHAG_CUT =
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "shag_cut");
    private static final ResourceLocation ROUGH_CUT =
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "rough_cut");
    private static final ResourceLocation FLAKE_CUT =
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "flake_cut");

    private LooseTobaccoModelProperties() {}

    public static void register() {
        register(ModItems.TOBACCO_LOOSE_WILD.get());
        register(ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        register(ModItems.TOBACCO_LOOSE_BURLEY.get());
        register(ModItems.TOBACCO_LOOSE_ORIENTAL.get());
        register(ModItems.TOBACCO_LOOSE_DOKHA.get());
        register(ModItems.TOBACCO_LOOSE_SHADE.get());
        register(ModItems.BLENDED_TOBACCO.get());
    }

    private static void register(Item item) {
        ItemProperties.register(item, SHAG_CUT,
                (stack, level, entity, seed) -> TobaccoCuringHelper.CUT_SHAG.equals(TobaccoCuringHelper.getCutType(stack))
                        ? 1.0F : 0.0F);
        ItemProperties.register(item, ROUGH_CUT,
                (stack, level, entity, seed) -> TobaccoCuringHelper.CUT_ROUGH.equals(TobaccoCuringHelper.getCutType(stack))
                        ? 1.0F : 0.0F);
        ItemProperties.register(item, FLAKE_CUT,
                (stack, level, entity, seed) -> TobaccoCuringHelper.CUT_FLAKE.equals(TobaccoCuringHelper.getCutType(stack))
                        ? 1.0F : 0.0F);
    }
}
