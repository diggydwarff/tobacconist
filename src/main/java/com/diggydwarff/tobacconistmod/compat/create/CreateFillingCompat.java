package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Create Spout registration for metadata-preserving Flavoring Essence casing. */
public final class CreateFillingCompat {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, TobacconistMod.MODID);

    public static final Supplier<RecipeSerializer<CreateTobaccoCasingRecipe>> TOBACCO_CASING =
            SERIALIZERS.register("create_tobacco_casing",
                    () -> new StandardProcessingRecipe.Serializer<>(CreateTobaccoCasingRecipe::new));

    private CreateFillingCompat() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
        TobacconistMod.LOGGER.info("Create Spout aromatic-tobacco casing integration enabled.");
    }
}
