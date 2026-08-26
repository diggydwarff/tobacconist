package com.diggydwarff.tobacconistmod.compat.create;

import net.minecraftforge.registries.ForgeRegistries;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registration entry point for Create Mechanical Press integration.
 *
 * <p>This class is reflectively loaded only when Create is installed, keeping Create types out of
 * Tobacconist's always-loaded compatibility bootstrap.</p>
 */
public final class CreatePressCompat {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TobacconistMod.MODID);

    public static final Supplier<RecipeSerializer<CreateTobaccoPressingRecipe>> TOBACCO_PRESSING =
            SERIALIZERS.register(
                    "create_tobacco_pressing",
                    () -> new ProcessingRecipeSerializer<>(CreateTobaccoPressingRecipe::new)
            );

    public static final Supplier<RecipeSerializer<CreateTobaccoAssemblyPressingRecipe>> TOBACCO_ASSEMBLY_PRESSING =
            SERIALIZERS.register(
                    "create_tobacco_assembly_pressing",
                    () -> new ProcessingRecipeSerializer<>(CreateTobaccoAssemblyPressingRecipe::new)
            );

    private CreatePressCompat() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
        TobacconistMod.LOGGER.info("Create Mechanical Press tobacco + product assembly integration enabled.");
    }
}
