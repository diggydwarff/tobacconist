package com.diggydwarff.tobacconistmod.vapor;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import java.util.*;

/** Ordered, datapack-defined extraction using Forge's native 1.20.1 recipe transport. */
public record ExtractionRecipe(ResourceLocation id, List<Input> inputs, ItemStack result, int duration) implements Recipe<Container> {
    public record Input(Ingredient ingredient, int count) {
        public Input { if (count < 1 || count > 64) throw new IllegalArgumentException("Invalid ingredient count"); }
    }
    public static final class Contents extends SimpleContainer {
        public Contents(List<ItemStack> items) { super(items.toArray(ItemStack[]::new)); }
    }
    public ExtractionRecipe {
        if (inputs.isEmpty() || inputs.size() > 3 || duration < 20 || duration > 12000)
            throw new IllegalArgumentException("Invalid extraction recipe");
        inputs = List.copyOf(inputs); result = result.copy();
    }
    @Override public boolean matches(Container contents, Level level) {
        for (int i = 0; i < 3; i++) {
            ItemStack stack = contents.getItem(i);
            if (i >= inputs.size()) { if (!stack.isEmpty()) return false; }
            else if (!inputs.get(i).ingredient().test(stack) || stack.getCount() < inputs.get(i).count()) return false;
        }
        return true;
    }
    @Override public ItemStack assemble(Container contents, RegistryAccess registries) { return result.copy(); }
    @Override public ItemStack getResultItem(RegistryAccess registries) { return result.copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public boolean canCraftInDimensions(int width,int height) { return width*height >= inputs.size(); }
    @Override public RecipeType<?> getType() { return VaporContent.EXTRACTION.get(); }
    @Override public RecipeSerializer<?> getSerializer() { return VaporContent.EXTRACTION_SERIALIZER.get(); }
    @Override public boolean isSpecial() { return true; }
    @Override public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result=NonNullList.create();inputs.forEach(input->result.add(input.ingredient()));return result;
    }
    public static final class Serializer implements RecipeSerializer<ExtractionRecipe> {
        @Override public ExtractionRecipe fromJson(ResourceLocation id,JsonObject json) {
            List<Input> inputs=new ArrayList<>();
            for (var value:GsonHelper.getAsJsonArray(json,"inputs")) {
                JsonObject input=value.getAsJsonObject();
                inputs.add(new Input(Ingredient.fromJson(input.get("ingredient")),GsonHelper.getAsInt(input,"count",1)));
            }
            return new ExtractionRecipe(id,inputs,ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json,"result")),GsonHelper.getAsInt(json,"duration",200));
        }
        @Override public ExtractionRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buffer) {
            int count=buffer.readVarInt();if(count<1 || count>3)throw new IllegalArgumentException("Invalid extraction inputs");
            List<Input> inputs=new ArrayList<>();for(int i=0;i<count;i++)inputs.add(new Input(Ingredient.fromNetwork(buffer),buffer.readVarInt()));
            return new ExtractionRecipe(id,inputs,buffer.readItem(),buffer.readVarInt());
        }
        @Override public void toNetwork(FriendlyByteBuf buffer,ExtractionRecipe recipe) {
            buffer.writeVarInt(recipe.inputs().size());
            recipe.inputs().forEach(input->{input.ingredient().toNetwork(buffer);buffer.writeVarInt(input.count());});
            buffer.writeItem(recipe.result());buffer.writeVarInt(recipe.duration());
        }
    }
}
