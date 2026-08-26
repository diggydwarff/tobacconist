package com.diggydwarff.tobacconistmod.compat.create;

import net.minecraftforge.registries.ForgeRegistries;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Registers Tobacconist Create Basin and Mechanical Mixer recipe types. */
public final class CreateMixerCompat {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TobacconistMod.MODID);

    public static final Supplier<RecipeSerializer<CreateTobaccoMixingRecipe>> TOBACCO_MIXING =
            SERIALIZERS.register(
                    "create_tobacco_mixing",
                    () -> new ProcessingRecipeSerializer<>(CreateTobaccoMixingRecipe::new)
            );

    public static final Supplier<RecipeSerializer<CreateTobaccoHomogenizingRecipe>> TOBACCO_HOMOGENIZING =
            SERIALIZERS.register(
                    "create_tobacco_homogenizing",
                    () -> new ProcessingRecipeSerializer<>(CreateTobaccoHomogenizingRecipe::new)
            );

    public static final Supplier<RecipeSerializer<CreateTobaccoBlendingRecipe>> TOBACCO_BLENDING =
            SERIALIZERS.register(
                    "create_tobacco_blending",
                    () -> new ProcessingRecipeSerializer<>(CreateTobaccoBlendingRecipe::new)
            );


    private CreateMixerCompat() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
        modEventBus.addListener(CreateMixerCompat::commonSetup);
        TobacconistMod.LOGGER.info("Create Mechanical Mixer Shisha, homogenizing, and tobacco blending integration enabled.");
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CraftingHelper.register(
                    new ResourceLocation(TobacconistMod.MODID, "tobacco_mixing_capture"),
                    CreateTobaccoCaptureIngredient.SERIALIZER);
            CraftingHelper.register(
                    new ResourceLocation(TobacconistMod.MODID, "tobacco_blending_capture"),
                    CreateTobaccoBlendIngredient.SERIALIZER);
        });
    }
}
