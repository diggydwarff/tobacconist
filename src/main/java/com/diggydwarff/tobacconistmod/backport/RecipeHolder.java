package com.diggydwarff.tobacconistmod.backport;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
public record RecipeHolder<T extends Recipe<?>>(ResourceLocation id,T value) {
    public RecipeHolder(T recipe) { this(recipe.getId(),recipe); }
}
