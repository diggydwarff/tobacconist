package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Runtime-only ingredient that captures the exact tobacco stack selected by Create processing.
 *
 * <p>Forge 1.20.1 represents custom ingredients as {@link AbstractIngredient}s rather than
 * NeoForge's ICustomIngredient/IngredientType API. The serializer is only needed when Create
 * synchronizes the already-built processing recipe to the client; runtime capture remains
 * server-side on the recipe instance that installed this ingredient.</p>
 */
public final class CreateTobaccoCaptureIngredient extends AbstractIngredient {
    public static final Serializer SERIALIZER = new Serializer();

    private final Predicate<ItemStack> validator;
    private final Consumer<ItemStack> capture;

    public CreateTobaccoCaptureIngredient(Predicate<ItemStack> validator, Consumer<ItemStack> capture) {
        super();
        this.validator = validator;
        this.capture = capture;
    }

    public Ingredient toVanilla() {
        return this;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!validator.test(stack)) {
            return false;
        }
        capture.accept(stack);
        return true;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public ItemStack[] getItems() {
        return displayItems().toArray(ItemStack[]::new);
    }

    private static Stream<ItemStack> displayItems() {
        Stream<ItemStack> looseTobaccos = Stream.of(
                ModItems.TOBACCO_LOOSE_WILD.get(),
                ModItems.TOBACCO_LOOSE_VIRGINIA.get(),
                ModItems.TOBACCO_LOOSE_BURLEY.get(),
                ModItems.TOBACCO_LOOSE_ORIENTAL.get(),
                ModItems.TOBACCO_LOOSE_DOKHA.get(),
                ModItems.TOBACCO_LOOSE_SHADE.get(),
                ModItems.BLENDED_TOBACCO.get()
        ).map(ItemStack::new);

        ItemStack shisha = new ItemStack(ModItems.SHISHA_TOBACCO.get());
        com.diggydwarff.tobacconistmod.util.LegacyItemTags.getOrCreateTag(shisha)
                .putString("flavor1", "Apple");
        return Stream.concat(looseTobaccos, Stream.of(shisha));
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TobacconistMod.MODID + ":tobacco_mixing_capture");
        return json;
    }

    private static CreateTobaccoCaptureIngredient clientFallback() {
        return new CreateTobaccoCaptureIngredient(stack -> true, stack -> {});
    }

    public static final class Serializer implements IIngredientSerializer<CreateTobaccoCaptureIngredient> {
        @Override
        public CreateTobaccoCaptureIngredient parse(JsonObject json) {
            return clientFallback();
        }

        @Override
        public CreateTobaccoCaptureIngredient parse(FriendlyByteBuf buffer) {
            return clientFallback();
        }

        @Override
        public void write(FriendlyByteBuf buffer, CreateTobaccoCaptureIngredient ingredient) {
            // No callback state is serialized. The client only needs the display representation.
        }
    }
}
