package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.fluid.ModExtractionFluids;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class VaporData {
    public static final int BOTTLE = 250;
    public static final int SMALL_BOTTLE = 150;
    public static final int LARGE_BOTTLE = 1000;
    public static final int TANK = 100;
    public static final int MAX_CHARGE = 100;
    private record Fields(int vegetable, int clear, int nicotine, Map<String, Integer> flavors) {}
    private static final Codec<Fields> FIELDS = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, LiquidBlend.SCALE).fieldOf("vegetable").forGetter(Fields::vegetable),
            Codec.intRange(0, LiquidBlend.SCALE).fieldOf("clear").forGetter(Fields::clear),
            Codec.intRange(0, LiquidBlend.SCALE).fieldOf("nicotine").forGetter(Fields::nicotine),
            Codec.unboundedMap(Codec.STRING, Codec.intRange(1, LiquidBlend.SCALE))
                    .optionalFieldOf("flavors", Map.of()).forGetter(Fields::flavors)
    ).apply(instance, Fields::new));
    public static final Codec<LiquidBlend> CODEC = FIELDS.flatXmap(fields -> {
        try { return com.mojang.serialization.DataResult.success(new LiquidBlend(fields.vegetable, fields.clear, fields.nicotine, fields.flavors)); }
        catch (IllegalArgumentException exception) { return com.mojang.serialization.DataResult.error(exception::getMessage); }
    }, blend -> com.mojang.serialization.DataResult.success(new Fields(blend.vegetable(), blend.clear(), blend.nicotine(), blend.flavors())));

    private VaporData() {}

    public static LiquidBlend blend(ItemStack stack) {
        if (stack.getItem() instanceof SizedBottleItem) return blend(SizedBottleItem.fluid(stack));
        LiquidBlend saved = com.diggydwarff.tobacconistmod.backport.ComponentData.get(stack, VaporContent.BLEND.get());
        if (saved != null) return saved;
        if (stack.is(VaporContent.VEGETABLE_BASE.get())) return LiquidBlend.VEGETABLE;
        if (stack.is(VaporContent.CLEAR_BASE.get())) return LiquidBlend.CLEAR;
        if (stack.is(VaporContent.NICOTINE_EXTRACT.get())) return LiquidBlend.NICOTINE;
        BottledMolassesFlavors flavor = BottledMolassesFlavors.fromEssenceItem(stack.getItem());
        if (flavor != null) return LiquidBlend.essence(flavor.getFlavorPath());
        return null;
    }

    public static LiquidBlend blend(FluidStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.getFluid() == VaporContent.VEGETABLE_FLUID.get()) return LiquidBlend.VEGETABLE;
        if (stack.getFluid() == VaporContent.CLEAR_FLUID.get()) return LiquidBlend.CLEAR;
        if (stack.getFluid() == VaporContent.NICOTINE_FLUID.get()) return LiquidBlend.NICOTINE;
        if (stack.getFluid() == VaporContent.LIQUID_FLUID.get()) return com.diggydwarff.tobacconistmod.backport.ComponentData.get(stack, VaporContent.BLEND.get());
        return ModExtractionFluids.findEssenceFlavor(stack.getFluid())
                .map(flavor -> LiquidBlend.essence(flavor.getFlavorPath())).orElse(null);
    }

    public static FluidStack fluid(LiquidBlend blend, int amount) {
        FluidStack result = new FluidStack(VaporContent.LIQUID_FLUID.get(), amount);
        com.diggydwarff.tobacconistmod.backport.ComponentData.set(result, VaporContent.BLEND.get(), blend);
        return result;
    }

    public static ItemStack bottle(LiquidBlend blend, int amount) {
        ItemStack result = new ItemStack(VaporContent.VAPE_LIQUID.get());
        com.diggydwarff.tobacconistmod.backport.ComponentData.set(result, VaporContent.BLEND.get(), blend);
        com.diggydwarff.tobacconistmod.backport.ComponentData.set(result, VaporContent.VOLUME.get(), amount);
        return result;
    }

    public static int bottleCapacity(ItemStack stack) { return stack.getItem() instanceof SizedBottleItem item ? item.capacity() : BOTTLE; }
    public static boolean isEmptyGlass(ItemStack stack) { return stack.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || SizedBottleItem.isEmptyGlass(stack); }
    public static boolean isLiquidBottle(ItemStack stack) {
        return stack.is(VaporContent.VAPE_LIQUID.get()) || stack.getItem() instanceof SizedBottleItem
                && SizedBottleItem.fluid(stack).getFluid() == VaporContent.LIQUID_FLUID.get();
    }
    /** Finished liquid or pure vegetable base; other concentrates remain bench ingredients. */
    public static boolean isVapeRefill(ItemStack stack) {
        return isLiquidBottle(stack) || stack.is(VaporContent.VEGETABLE_BASE.get())
                || stack.getItem() instanceof SizedBottleItem
                && SizedBottleItem.fluid(stack).getFluid() == VaporContent.VEGETABLE_FLUID.get();
    }
    public static int volume(ItemStack stack) {
        if (stack.getItem() instanceof SizedBottleItem) return SizedBottleItem.fluid(stack).getAmount();
        return com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(com.diggydwarff.tobacconistmod.backport.ComponentData.getOrDefault(stack, VaporContent.VOLUME.get(), BOTTLE), 0, BOTTLE);
    }
    public static void setBottleVolume(ItemStack stack, int amount) {
        if (stack.getItem() instanceof SizedBottleItem) {
            BottledFluid fluid = com.diggydwarff.tobacconistmod.backport.ComponentData.get(stack, VaporContent.BOTTLED_FLUID.get());
            if (fluid != null && amount > 0) com.diggydwarff.tobacconistmod.backport.ComponentData.set(stack, VaporContent.BOTTLED_FLUID.get(), fluid.withAmount(Math.min(amount, bottleCapacity(stack))));
            else com.diggydwarff.tobacconistmod.backport.ComponentData.remove(stack, VaporContent.BOTTLED_FLUID.get());
        } else com.diggydwarff.tobacconistmod.backport.ComponentData.set(stack, VaporContent.VOLUME.get(), com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(amount, 0, BOTTLE));
    }
    public static ItemStack emptyGlass(ItemStack bottle) {
        return new ItemStack(bottleCapacity(bottle) == SMALL_BOTTLE ? VaporContent.SMALL_GLASS_BOTTLE.get()
                : bottleCapacity(bottle) == LARGE_BOTTLE ? VaporContent.LARGE_GLASS_BOTTLE.get() : net.minecraft.world.item.Items.GLASS_BOTTLE);
    }
    public static ItemStack bottle(LiquidBlend blend, int amount, ItemStack glass) {
        return glass.is(net.minecraft.world.item.Items.GLASS_BOTTLE) ? bottle(blend, amount)
                : SizedBottleItem.filled(glass, fluid(blend, amount), amount);
    }
    public static int charge(ItemStack stack) { return com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(com.diggydwarff.tobacconistmod.backport.ComponentData.getOrDefault(stack, VaporContent.CHARGE.get(), MAX_CHARGE), 0, MAX_CHARGE); }

    public static List<Component> description(LiquidBlend blend) {
        List<Component> result = new ArrayList<>();
        if (!blend.flavors().isEmpty()) {
            var names = Component.empty();
            for (String flavor : new java.util.TreeSet<>(blend.flavors().keySet())) {
                if (!names.getString().isEmpty()) names.append(" + ");
                names.append(flavorName(flavor));
            }
            result.add(names.copy().withStyle(ChatFormatting.GOLD));
        }
        int vg = blend.vegetablePercent();
        if (blend.vegetable() + blend.clear() > 0) result.add(Component.translatable("tobacconistmod.vapor.ratio", vg, 100 - vg).withStyle(ChatFormatting.GRAY));
        if (blend.usable()) result.add(Component.translatable("tobacconistmod.vapor.nicotine",
                Component.translatable("tobacconistmod.vapor.strength." + blend.strength())).withStyle(ChatFormatting.GRAY));
        return result;
    }

    public static Component flavorName(String path) {
        for (var flavor : BottledMolassesFlavors.values()) {
            if (!flavor.isPlain() && flavor.getFlavorPath().equals(path)) {
                return Component.literal(flavor.getFlavorDisplayName());
            }
        }
        return Component.literal(path.replace('_', ' '));
    }
}
