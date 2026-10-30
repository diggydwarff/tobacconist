package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.util.List;

public final class SizedBottleItem extends Item {
    private final int capacity;
    private final boolean filled;

    public SizedBottleItem(Properties properties, int capacity, boolean filled) {
        super(properties); this.capacity = capacity; this.filled = filled;
    }
    public int capacity() { return capacity; }
    public static boolean isEmptyGlass(ItemStack stack) { return stack.getItem() instanceof SizedBottleItem item && !item.filled; }
    public static FluidStack fluid(ItemStack stack) {
        if (!(stack.getItem() instanceof SizedBottleItem item) || !item.filled) return FluidStack.EMPTY;
        BottledFluid data = com.diggydwarff.tobacconistmod.backport.ComponentData.get(stack, VaporContent.BOTTLED_FLUID.get());
        return data == null ? FluidStack.EMPTY : data.asFluid(item.capacity);
    }
    public static ItemStack filled(ItemStack glass, FluidStack fluid, int amount) {
        if (!BottledFluid.supported(fluid) || amount <= 0) return ItemStack.EMPTY;
        int capacity = VaporData.bottleCapacity(glass);
        ItemStack result = new ItemStack(capacity == VaporData.SMALL_BOTTLE ? VaporContent.SMALL_FILLED_BOTTLE.get() : VaporContent.LARGE_FILLED_BOTTLE.get());
        com.diggydwarff.tobacconistmod.backport.ComponentData.set(result, VaporContent.BOTTLED_FLUID.get(), BottledFluid.of(fluid, Math.min(amount, capacity)));
        return result;
    }
    @Override public Component getName(ItemStack stack) {
        if (!filled || fluid(stack).isEmpty()) return super.getName(stack);
        Component name = Component.translatable(capacity == VaporData.SMALL_BOTTLE ? "tobacconistmod.vapor.small_bottle_of" : "tobacconistmod.vapor.large_bottle_of", fluid(stack).getDisplayName());
        String label = TobaccoLabelHelper.getProductLabel(stack);
        return label.isEmpty() ? name : TobaccoLabelHelper.buildNamedProduct(label, name);
    }
    @Override public void appendHoverText(ItemStack stack, Level context, List<Component> tooltip, TooltipFlag flag) {
        LiquidBlend blend = VaporData.blend(stack);
        if (blend != null) tooltip.addAll(VaporData.description(blend));
        tooltip.add(Component.translatable("tobacconistmod.vapor.bottle_status", VaporData.volume(stack), capacity).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tobacconistmod.vapor.bottle_pour_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack glass = player.getItemInHand(hand);
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack source = player.getItemInHand(other);
        if (pour(player, hand, other, level.isClientSide)) return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        var sourceHandler = source.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (sourceHandler != null && !sourceHandler.getFluidInTank(0).isEmpty()) return InteractionResultHolder.fail(glass);
        if (!isEmptyGlass(glass)) return InteractionResultHolder.pass(glass);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(glass);
        BlockPos pos = hit.getBlockPos();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos.relative(hit.getDirection()), hit.getDirection(), glass)
                || level.getFluidState(pos).getType() != Fluids.WATER) return InteractionResultHolder.pass(glass);
        if (!level.isClientSide) receive(player, hand, filled(glass, new FluidStack(Fluids.WATER, capacity), capacity));
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
    public static boolean pour(Player player, InteractionHand targetHand, InteractionHand sourceHand, boolean simulate) {
        ItemStack target = player.getItemInHand(targetHand), source = player.getItemInHand(sourceHand);
        var out = com.diggydwarff.tobacconistmod.backport.BackportStacks.copyWithCount(source, 1).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        var in = com.diggydwarff.tobacconistmod.backport.BackportStacks.copyWithCount(target, 1).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (out == null || in == null) return false;
        boolean emptyTarget = in.getFluidInTank(0).isEmpty();
        FluidStack fluid = out.getFluidInTank(0);
        int amount = in.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
        if (amount <= 0 || out.drain(amount, IFluidHandler.FluidAction.SIMULATE).getAmount() != amount) return false;
        if (!simulate) {
            in.fill(out.drain(amount, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            ItemStack filled = in.getContainer();
            if (emptyTarget) {
                var name = com.diggydwarff.tobacconistmod.backport.ComponentData.has(target, com.diggydwarff.tobacconistmod.backport.DataComponents.CUSTOM_NAME)
                        ? com.diggydwarff.tobacconistmod.backport.ComponentData.get(target, com.diggydwarff.tobacconistmod.backport.DataComponents.CUSTOM_NAME)
                        : com.diggydwarff.tobacconistmod.backport.ComponentData.get(source, com.diggydwarff.tobacconistmod.backport.DataComponents.CUSTOM_NAME);
                if (name != null) com.diggydwarff.tobacconistmod.backport.ComponentData.set(filled, com.diggydwarff.tobacconistmod.backport.DataComponents.CUSTOM_NAME, name);
                String label = TobaccoLabelHelper.getProductLabel(target);
                if (label.isEmpty()) label = TobaccoLabelHelper.getProductLabel(source);
                if (!label.isEmpty()) TobaccoLabelHelper.setProductLabel(filled, label);
            }
            receive(player, targetHand, filled);
            receive(player, sourceHand, out.getContainer());
        }
        return true;
    }
    private static void receive(Player player, InteractionHand hand, ItemStack filled) {
        ItemStack original = player.getItemInHand(hand);
        if (original.getCount() == 1) player.setItemInHand(hand, filled);
        else {
            if (!player.getAbilities().instabuild) original.shrink(1);
            if (!player.addItem(filled)) player.drop(filled, false);
        }
    }
}
