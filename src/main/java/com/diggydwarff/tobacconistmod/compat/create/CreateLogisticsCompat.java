package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.DoubleHookahBlock;
import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.api.packager.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Create 6 package/stock-network integration for Tobacconist processing inventories. */
public final class CreateLogisticsCompat {
    private CreateLogisticsCompat() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CreateLogisticsCompat::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        // Tobacconist's blocks are DeferredRegister entries, so query the suppliers only after
        // registry setup has completed. Create's SimpleRegistry is thread-safe, but enqueueWork
        // also keeps this alongside Create's own default InventoryIdentifier/Unpacking setup.
        event.enqueueWork(CreateLogisticsCompat::registerLogisticsHandlers);
    }

    private static void registerLogisticsHandlers() {
        registerSingleInventory(ModBlocks.TOBACCO_DRYING_RACK.get());
        registerSingleInventory(ModBlocks.TOBACCO_BARREL.get());
        registerSingleInventory(ModBlocks.FLUE_FIREBOX.get());
        registerSingleInventory(ModBlocks.HOOKAH.get());

        registerDoubleHookah(ModBlocks.ORNATE_COPPER_HOOKAH.get());
        registerDoubleHookah(ModBlocks.ORNATE_GOLD_HOOKAH.get());
        registerDoubleHookah(ModBlocks.ORNATE_DIAMOND_HOOKAH.get());
        registerDoubleHookah(ModBlocks.ORNATE_IRON_HOOKAH.get());
        registerDoubleHookah(ModBlocks.ORNATE_AMETHYST_HOOKAH.get());

        // The normal Create unpacker already respects our NeoForge item handlers, including
        // metadata-sensitive barrel batches, fuel validation and Hookah slot validation.
        // The rack is the one special case: its normal automation rejects UP/DOWN insertion,
        // while a Packager is a deliberate bulk-loading endpoint and should work from any face.
        UnpackingHandler.REGISTRY.register(
                ModBlocks.TOBACCO_DRYING_RACK.get(),
                CreateLogisticsCompat::unpackDryingRack
        );

        TobacconistMod.LOGGER.info("Create logistics integration enabled for Tobacconist processing inventories.");
    }

    private static void registerSingleInventory(Block block) {
        InventoryIdentifier.REGISTRY.register(
                block,
                (level, state, face) -> new InventoryIdentifier.Single(face.getPos())
        );
    }

    private static void registerDoubleHookah(Block block) {
        InventoryIdentifier.REGISTRY.register(block, CreateLogisticsCompat::identifyDoubleHookah);
        UnpackingHandler.REGISTRY.register(block, CreateLogisticsCompat::unpackDoubleHookah);
    }

    private static InventoryIdentifier identifyDoubleHookah(Level level, BlockState state, BlockFace face) {
        BlockPos lower = state.hasProperty(DoubleHookahBlock.HALF)
                && state.getValue(DoubleHookahBlock.HALF) == DoubleBlockHalf.UPPER
                ? face.getPos().below()
                : face.getPos();
        return new InventoryIdentifier.Pair(lower, lower.above());
    }

    private static boolean unpackDryingRack(Level level,
                                             BlockPos pos,
                                             BlockState state,
                                             Direction side,
                                             List<ItemStack> items,
                                             @Nullable PackageOrderWithCrafts orderContext,
                                             boolean simulate) {
        // DryingRackItemHandler treats every horizontal face identically. Normalize vertical
        // Packager access to one horizontal face so Create's proven all-or-nothing default
        // unpacker can still perform the actual capacity/metadata simulation and insertion.
        Direction effectiveSide = side == Direction.UP || side == Direction.DOWN
                ? Direction.NORTH
                : side;
        return UnpackingHandler.DEFAULT.unpack(
                level, pos, state, effectiveSide, items, orderContext, simulate
        );
    }

    private static boolean unpackDoubleHookah(Level level,
                                               BlockPos pos,
                                               BlockState state,
                                               Direction side,
                                               List<ItemStack> items,
                                               @Nullable PackageOrderWithCrafts orderContext,
                                               boolean simulate) {
        BlockPos targetPos = pos;
        if (state.hasProperty(DoubleHookahBlock.HALF)
                && state.getValue(DoubleHookahBlock.HALF) == DoubleBlockHalf.UPPER) {
            targetPos = pos.below();
        }

        BlockState targetState = level.getBlockState(targetPos);
        if (!(targetState.getBlock() instanceof DoubleHookahBlock)
                || !targetState.hasProperty(DoubleHookahBlock.HALF)
                || targetState.getValue(DoubleHookahBlock.HALF) != DoubleBlockHalf.LOWER) {
            return false;
        }

        // HookahEntity's three-slot ItemStackHandler already routes fuel, Shisha and water to
        // their proper slots. Delegating keeps Create's transactional package simulation intact.
        return UnpackingHandler.DEFAULT.unpack(
                level, targetPos, targetState, side, items, orderContext, simulate
        );
    }
}
