package com.diggydwarff.tobacconistmod.recipes;

import net.minecraftforge.registries.ForgeRegistries;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TobacconistMod.MODID);

    public static final Supplier<RecipeSerializer<ShishaTobaccoRecipe>> SHISHA_TOBACCO_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_shishatobacco", () -> new SimpleRecipeSerializer<>(ShishaTobaccoRecipe::new));
    public static final Supplier<RecipeSerializer<FlavorMolassesRecipe>> FLAVOR_MOLASSES_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_flavor_molasses", () -> new SimpleRecipeSerializer<>(FlavorMolassesRecipe::new));
    public static final Supplier<RecipeSerializer<CigaretteRecipe>> CIGARETTE_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_cigarette", () -> new SimpleRecipeSerializer<>(CigaretteRecipe::new));
    public static final Supplier<RecipeSerializer<CigarRecipe>> CIGAR_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_cigar", () -> new SimpleRecipeSerializer<>(CigarRecipe::new));
    public static final Supplier<RecipeSerializer<AverageTobaccoLeavesRecipe>> AVERAGE_TOBACCO_LEAVES_SERIALIZER =
            SERIALIZERS.register("average_tobacco_leaves", () -> new SimpleRecipeSerializer<>(AverageTobaccoLeavesRecipe::new));
    public static final Supplier<RecipeSerializer<LooseTobaccoCuttingRecipe>> LOOSE_TOBACCO_CUTTING_SERIALIZER =
            SERIALIZERS.register("loose_tobacco_cutting", () -> new SimpleRecipeSerializer<>(LooseTobaccoCuttingRecipe::new));
    public static final Supplier<RecipeSerializer<TobaccoBoxFillRecipe>> TOBACCO_BOX_FILL_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_tobacco_box_fill", () -> new SimpleRecipeSerializer<>(TobaccoBoxFillRecipe::new));
    public static final Supplier<RecipeSerializer<TobaccoBoxLabelRecipe>> TOBACCO_BOX_LABEL_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_tobacco_box_label", () -> new SimpleRecipeSerializer<>(TobaccoBoxLabelRecipe::new));
    public static final Supplier<RecipeSerializer<LabelDuplicateRecipe>> LABEL_DUPLICATE_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_label_duplicate", () -> new SimpleRecipeSerializer<>(LabelDuplicateRecipe::new));
    public static final Supplier<RecipeSerializer<TobaccoBlendingRecipe>> TOBACCO_BLENDING_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_tobacco_blending", () -> new SimpleRecipeSerializer<>(TobaccoBlendingRecipe::new));
    public static final Supplier<RecipeSerializer<TobaccoCrateRecipe>> TOBACCO_CRATE_RECIPE_SERIALIZER =
            SERIALIZERS.register("crafting_special_tobacco_crate", () -> new SimpleRecipeSerializer<>(TobaccoCrateRecipe::new));

    public static void register(IEventBus eventBus) { SERIALIZERS.register(eventBus); }
}
