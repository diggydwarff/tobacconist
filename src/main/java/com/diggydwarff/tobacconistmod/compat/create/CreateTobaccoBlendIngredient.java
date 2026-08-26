package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
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

/** Runtime-only ingredient used to capture the exact loose tobacco stacks selected by a Basin. */
public final class CreateTobaccoBlendIngredient extends AbstractIngredient {
    public static final Serializer SERIALIZER = new Serializer();

    private final Predicate<ItemStack> validator;
    private final Consumer<ItemStack> capture;

    public CreateTobaccoBlendIngredient(Predicate<ItemStack> validator, Consumer<ItemStack> capture) {
        super();
        this.validator = validator;
        this.capture = capture;
    }

    public Ingredient toVanilla() {
        return this;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!validator.test(stack)) return false;
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
        return Stream.of(
                ModItems.TOBACCO_LOOSE_WILD.get(),
                ModItems.TOBACCO_LOOSE_VIRGINIA.get(),
                ModItems.TOBACCO_LOOSE_BURLEY.get(),
                ModItems.TOBACCO_LOOSE_ORIENTAL.get(),
                ModItems.TOBACCO_LOOSE_DOKHA.get(),
                ModItems.TOBACCO_LOOSE_SHADE.get()
        ).map(item -> {
            ItemStack stack = new ItemStack(item);
            TobaccoCuringHelper.applyCureData(stack, TobaccoCuringHelper.CURE_AIR, 75);
            TobaccoCuringHelper.setCutType(stack, TobaccoCuringHelper.CUT_RIBBON);
            return stack;
        });
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TobacconistMod.MODID + ":tobacco_blending_capture");
        return json;
    }

    private static CreateTobaccoBlendIngredient clientFallback() {
        return new CreateTobaccoBlendIngredient(stack -> true, stack -> {});
    }

    public static final class Serializer implements IIngredientSerializer<CreateTobaccoBlendIngredient> {
        @Override
        public CreateTobaccoBlendIngredient parse(JsonObject json) {
            return clientFallback();
        }

        @Override
        public CreateTobaccoBlendIngredient parse(FriendlyByteBuf buffer) {
            return clientFallback();
        }

        @Override
        public void write(FriendlyByteBuf buffer, CreateTobaccoBlendIngredient ingredient) {
            // No callback state is serialized. The client only needs the display representation.
        }
    }
}
