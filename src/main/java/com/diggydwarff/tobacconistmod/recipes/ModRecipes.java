package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TobacconistMod.MODID);

    public static final RegistryObject<RecipeSerializer<ShishaTobaccoRecipe>> SHISHA_TOBACCO_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_shishatobacco", () -> ShishaTobaccoRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<CigaretteRecipe>> CIGARETTE_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_cigarette", () -> CigaretteRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<CigarRecipe>> CIGAR_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_cigar", () -> CigarRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }


}
