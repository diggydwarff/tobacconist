package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TobacconistMod.MODID);

    public static final RegistryObject<RecipeSerializer<?>> WOODEN_PIPE =
            SERIALIZERS.register("wooden_pipe", WoodenPipeRecipe.Serializer::new);
}
