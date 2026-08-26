package com.diggydwarff.tobacconistmod.compat.create;

import net.minecraftforge.registries.ForgeRegistries;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Create Spout registration for metadata-preserving Flavoring Essence casing. */
public final class CreateFillingCompat {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TobacconistMod.MODID);

    public static final Supplier<RecipeSerializer<CreateTobaccoCasingRecipe>> TOBACCO_CASING =
            SERIALIZERS.register("create_tobacco_casing",
                    () -> new ProcessingRecipeSerializer<>(CreateTobaccoCasingRecipe::new));

    private CreateFillingCompat() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
        TobacconistMod.LOGGER.info("Create Spout aromatic-tobacco casing integration enabled.");
    }
}
