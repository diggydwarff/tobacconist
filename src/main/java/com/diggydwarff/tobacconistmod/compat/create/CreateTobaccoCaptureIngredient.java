package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Runtime-only ingredient that captures the exact Shisha source stack selected by a Basin.
 * It accepts either Rough loose tobacco for the first flavor or unused Shisha with fewer than
 * three flavors for progressive blending. The wrapper lets arbitrary per-stack metadata drive
 * the output without hard-coding it into recipe JSON.
 */
public final class CreateTobaccoCaptureIngredient implements ICustomIngredient {
    public static final MapCodec<CreateTobaccoCaptureIngredient> CODEC =
            MapCodec.unit(new CreateTobaccoCaptureIngredient(stack -> false, stack -> {}));

    private final Predicate<ItemStack> validator;
    private final Consumer<ItemStack> capture;

    public CreateTobaccoCaptureIngredient(Predicate<ItemStack> validator, Consumer<ItemStack> capture) {
        this.validator = validator;
        this.capture = capture;
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
    public Stream<ItemStack> getItems() {
        Stream<ItemStack> roughTobaccos = Stream.of(
                ModItems.TOBACCO_LOOSE_WILD.get(),
                ModItems.TOBACCO_LOOSE_VIRGINIA.get(),
                ModItems.TOBACCO_LOOSE_BURLEY.get(),
                ModItems.TOBACCO_LOOSE_ORIENTAL.get(),
                ModItems.TOBACCO_LOOSE_DOKHA.get(),
                ModItems.TOBACCO_LOOSE_SHADE.get()
        ).map(item -> {
            ItemStack stack = new ItemStack(item);
            TobaccoCuringHelper.setCutType(stack, TobaccoCuringHelper.CUT_ROUGH);
            return stack;
        });

        ItemStack shisha = new ItemStack(ModItems.SHISHA_TOBACCO.get());
        com.diggydwarff.tobacconistmod.util.LegacyItemTags.getOrCreateTag(shisha)
                .putString("flavor1", "Bottle of Molasses");
        return Stream.concat(roughTobaccos, Stream.of(shisha));
    }

    @Override
    public IngredientType<?> getType() {
        return CreateMixerCompat.TOBACCO_CAPTURE.get();
    }
}
