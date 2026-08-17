package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, TobacconistMod.MODID);

    public static final Supplier<RecipeSerializer<WoodenPipeRecipe>> WOODEN_PIPE =
            SERIALIZERS.register("wooden_pipe", () -> new SimpleCraftingRecipeSerializer<>(WoodenPipeRecipe::new));
}
