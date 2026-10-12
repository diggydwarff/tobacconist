package com.diggydwarff.tobacconistmod.compat.create;

import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Marker: the two fluid inputs are measured against a reusable Liquid Recipe Card. */
public final class CreateVaporMixingRecipe extends MixingRecipe {
    public CreateVaporMixingRecipe(ProcessingRecipeParams params) { super(params); }
    @Override public RecipeSerializer<?> getSerializer() { return CreateMixerCompat.VAPOR_MIXING.get(); }
}
