package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Create Basin + Mechanical Mixer registration for fluid-based Shisha production. */
public final class CreateMixerCompat {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, TobacconistMod.MODID);
    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, TobacconistMod.MODID);

    public static final Supplier<RecipeSerializer<CreateTobaccoMixingRecipe>> TOBACCO_MIXING =
            SERIALIZERS.register(
                    "create_tobacco_mixing",
                    () -> new StandardProcessingRecipe.Serializer<>(CreateTobaccoMixingRecipe::new)
            );

    public static final Supplier<RecipeSerializer<CreateTobaccoHomogenizingRecipe>> TOBACCO_HOMOGENIZING =
            SERIALIZERS.register(
                    "create_tobacco_homogenizing",
                    () -> new StandardProcessingRecipe.Serializer<>(CreateTobaccoHomogenizingRecipe::new)
            );

    public static final Supplier<RecipeSerializer<CreateTobaccoBlendingRecipe>> TOBACCO_BLENDING =
            SERIALIZERS.register(
                    "create_tobacco_blending",
                    () -> new StandardProcessingRecipe.Serializer<>(CreateTobaccoBlendingRecipe::new)
            );

    public static final Supplier<IngredientType<CreateTobaccoCaptureIngredient>> TOBACCO_CAPTURE =
            INGREDIENT_TYPES.register(
                    "tobacco_mixing_capture",
                    () -> new IngredientType<>(CreateTobaccoCaptureIngredient.CODEC)
            );

    public static final Supplier<IngredientType<CreateTobaccoHomogenizingIngredient>> TOBACCO_HOMOGENIZING_CAPTURE =
            INGREDIENT_TYPES.register(
                    "tobacco_homogenizing_capture",
                    () -> new IngredientType<>(CreateTobaccoHomogenizingIngredient.CODEC)
            );

    public static final Supplier<IngredientType<CreateTobaccoBlendIngredient>> TOBACCO_BLEND_CAPTURE =
            INGREDIENT_TYPES.register(
                    "tobacco_blending_capture",
                    () -> new IngredientType<>(CreateTobaccoBlendIngredient.CODEC)
            );

    private CreateMixerCompat() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
        INGREDIENT_TYPES.register(modEventBus);
        TobacconistMod.LOGGER.info("Create Mechanical Mixer Shisha + homogenizing + tobacco blending integration enabled.");
    }
}
