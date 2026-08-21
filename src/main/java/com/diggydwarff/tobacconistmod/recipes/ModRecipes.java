package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, TobacconistMod.MODID);

    public static final Supplier<RecipeSerializer<ShishaTobaccoRecipe>> SHISHA_TOBACCO_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_shishatobacco", () -> new SimpleCraftingRecipeSerializer<>(ShishaTobaccoRecipe::new));
    public static final Supplier<RecipeSerializer<CigaretteRecipe>> CIGARETTE_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_cigarette", () -> new SimpleCraftingRecipeSerializer<>(CigaretteRecipe::new));
    public static final Supplier<RecipeSerializer<CigarRecipe>> CIGAR_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_cigar", () -> new SimpleCraftingRecipeSerializer<>(CigarRecipe::new));
    public static final Supplier<RecipeSerializer<AverageTobaccoLeavesRecipe>> AVERAGE_TOBACCO_LEAVES_SERIALIZER =
            SERIALIZERS.register("average_tobacco_leaves", () -> new SimpleCraftingRecipeSerializer<>(AverageTobaccoLeavesRecipe::new));
    public static final Supplier<RecipeSerializer<LooseTobaccoCuttingRecipe>> LOOSE_TOBACCO_CUTTING_SERIALIZER =
            SERIALIZERS.register("loose_tobacco_cutting", () -> new SimpleCraftingRecipeSerializer<>(LooseTobaccoCuttingRecipe::new));
    public static final Supplier<RecipeSerializer<TobaccoBoxFillRecipe>> TOBACCO_BOX_FILL_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_tobacco_box_fill", () -> new SimpleCraftingRecipeSerializer<>(TobaccoBoxFillRecipe::new));
    public static final Supplier<RecipeSerializer<TobaccoBoxLabelRecipe>> TOBACCO_BOX_LABEL_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_tobacco_box_label", () -> new SimpleCraftingRecipeSerializer<>(TobaccoBoxLabelRecipe::new));
    public static final Supplier<RecipeSerializer<LabelDuplicateRecipe>> LABEL_DUPLICATE_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_label_duplicate", () -> new SimpleCraftingRecipeSerializer<>(LabelDuplicateRecipe::new));

    public static void register(IEventBus eventBus) { SERIALIZERS.register(eventBus); }
}
