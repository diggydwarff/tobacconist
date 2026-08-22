package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Runtime-only ingredient used to capture the exact loose tobacco stacks selected by a Basin. */
public final class CreateTobaccoBlendIngredient implements ICustomIngredient {
    public static final MapCodec<CreateTobaccoBlendIngredient> CODEC =
            MapCodec.unit(new CreateTobaccoBlendIngredient(stack -> false, stack -> {}));

    private final Predicate<ItemStack> validator;
    private final Consumer<ItemStack> capture;

    public CreateTobaccoBlendIngredient(Predicate<ItemStack> validator, Consumer<ItemStack> capture) {
        this.validator = validator;
        this.capture = capture;
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
    public Stream<ItemStack> getItems() {
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
    public IngredientType<?> getType() {
        return CreateMixerCompat.TOBACCO_BLEND_CAPTURE.get();
    }
}
