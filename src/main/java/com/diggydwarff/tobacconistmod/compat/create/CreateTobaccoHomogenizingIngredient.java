package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Runtime-only ingredient used to capture the exact raw/cured leaf pair selected by a Create Basin. */
public final class CreateTobaccoHomogenizingIngredient implements ICustomIngredient {
    public static final MapCodec<CreateTobaccoHomogenizingIngredient> CODEC =
            MapCodec.unit(new CreateTobaccoHomogenizingIngredient(stack -> false, stack -> {}));

    private final Predicate<ItemStack> validator;
    private final Consumer<ItemStack> capture;

    public CreateTobaccoHomogenizingIngredient(Predicate<ItemStack> validator, Consumer<ItemStack> capture) {
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
        return Stream.of(
                ModItems.WILD_TOBACCO_LEAF.get(),
                ModItems.VIRGINIA_TOBACCO_LEAF.get(),
                ModItems.BURLEY_TOBACCO_LEAF.get(),
                ModItems.ORIENTAL_TOBACCO_LEAF.get(),
                ModItems.DOKHA_TOBACCO_LEAF.get(),
                ModItems.SHADE_TOBACCO_LEAF.get(),
                ModItems.WILD_TOBACCO_LEAF_DRY.get(),
                ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(),
                ModItems.BURLEY_TOBACCO_LEAF_DRY.get(),
                ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get(),
                ModItems.DOKHA_TOBACCO_LEAF_DRY.get(),
                ModItems.SHADE_TOBACCO_LEAF_DRY.get()
        ).map(item -> {
            ItemStack stack = new ItemStack(item);
            if (TobaccoCuringHelper.isRawTobaccoLeaf(stack)) {
                TobaccoGrowthHelper.applyGrowthQuality(stack, 50);
            } else {
                TobaccoCuringHelper.applyCureData(stack, TobaccoCuringHelper.CURE_AIR, 70);
            }
            return stack;
        });
    }

    @Override
    public IngredientType<?> getType() {
        return CreateMixerCompat.TOBACCO_HOMOGENIZING_CAPTURE.get();
    }
}
