package com.diggydwarff.tobacconistmod.vapor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import java.util.List;

/** Fixed-slot, data-pack-defined extraction. No tobacco-specific logic lives in the station. */
public record ExtractionRecipe(List<Input> inputs, ItemStack result, int duration) implements Recipe<ExtractionRecipe.Contents> {
    public record Input(Ingredient ingredient, int count) {
        static final Codec<Input> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(Input::ingredient),
                Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Input::count)
        ).apply(instance, Input::new));
    }
    public record Contents(List<ItemStack> items) implements RecipeInput {
        @Override public ItemStack getItem(int index) { return items.get(index); }
        @Override public int size() { return items.size(); }
    }
    static final MapCodec<ExtractionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Input.CODEC.listOf().fieldOf("inputs").forGetter(ExtractionRecipe::inputs),
            ItemStack.CODEC.fieldOf("result").forGetter(ExtractionRecipe::result),
            Codec.intRange(20, 12000).optionalFieldOf("duration", 200).forGetter(ExtractionRecipe::duration)
    ).apply(instance, ExtractionRecipe::new));

    public ExtractionRecipe {
        if (inputs.isEmpty() || inputs.size() > 3) throw new IllegalArgumentException("Extraction requires 1-3 inputs");
        inputs = List.copyOf(inputs); result = result.copy();
    }
    @Override public boolean matches(Contents contents, Level level) {
        for (int i = 0; i < 3; i++) {
            ItemStack stack = contents.getItem(i);
            if (i >= inputs.size()) { if (!stack.isEmpty()) return false; }
            else if (!inputs.get(i).ingredient.test(stack) || stack.getCount() < inputs.get(i).count) return false;
        }
        return true;
    }
    @Override public ItemStack assemble(Contents contents, HolderLookup.Provider registries) { return result.copy(); }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= inputs.size(); }
    @Override public RecipeType<?> getType() { return VaporContent.EXTRACTION.get(); }
    @Override public RecipeSerializer<?> getSerializer() { return VaporContent.EXTRACTION_SERIALIZER.get(); }
    @Override public boolean isSpecial() { return true; }
    @Override public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create(); inputs.forEach(input -> list.add(input.ingredient)); return list;
    }
    public static final class Serializer implements RecipeSerializer<ExtractionRecipe> {
        @Override public MapCodec<ExtractionRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, ExtractionRecipe> streamCodec() {
            return ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
        }
    }
}
