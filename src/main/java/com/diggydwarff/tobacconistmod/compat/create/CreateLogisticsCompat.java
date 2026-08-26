package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.DoubleHookahBlock;
import com.diggydwarff.tobacconistmod.block.custom.IndustrialDryingRackBlock;
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
        registerIndustrialRack();
        registerSingleInventory(ModBlocks.TOBACCO_BARREL.get());
        registerSingleInventory(ModBlocks.FLUE_FIREBOX.get());
        registerSingleInventory(ModBlocks.HOOKAH.get());

        registerDoubleHookah(ModBlocks.ORNATE_COPPER_HOOKAH.get());
        registerDoubleHookah(ModBlocks.ORNATE_GOLD_HOOKAH.get());
        registerDoubleHookah(ModBlocks.ORNATE_DIAMOND_HOOKAH.get());
        registerDoubleHookah(ModBlocks.ORNATE_IRON_HOOKAH.get());
        registerDoubleHookah(ModBlocks.ORNATE_AMETHYST_HOOKAH.get());
        registerDoubleHookah(ModBlocks.TALL_HOOKAH.get());
        registerDoubleHookah(ModBlocks.EXPOSED_COPPER_HOOKAH.get());
        registerDoubleHookah(ModBlocks.WEATHERED_COPPER_HOOKAH.get());
        registerDoubleHookah(ModBlocks.OXIDIZED_COPPER_HOOKAH.get());
        registerDoubleHookah(ModBlocks.WAXED_COPPER_HOOKAH.get());
        registerDoubleHookah(ModBlocks.WAXED_EXPOSED_COPPER_HOOKAH.get());
        registerDoubleHookah(ModBlocks.WAXED_WEATHERED_COPPER_HOOKAH.get());
        registerDoubleHookah(ModBlocks.WAXED_OXIDIZED_COPPER_HOOKAH.get());
        registerDoubleHookah(ModBlocks.REDSTONE_HOOKAH.get());
        registerDoubleHookah(ModBlocks.LAPIS_HOOKAH.get());
        registerDoubleHookah(ModBlocks.OBSIDIAN_HOOKAH.get());
        registerDoubleHookah(ModBlocks.EMERALD_HOOKAH.get());
        registerDoubleHookah(ModBlocks.NETHERITE_HOOKAH.get());

        // Use a custom rack unpacker because normal sided insertion rejects top/bottom faces.
        UnpackingHandler.REGISTRY.register(
                ModBlocks.TOBACCO_DRYING_RACK.get(),
                CreateLogisticsCompat::unpackDryingRack
        );
        UnpackingHandler.REGISTRY.register(
                ModBlocks.INDUSTRIAL_DRYING_RACK.get(),
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

    private static void registerIndustrialRack() {
        InventoryIdentifier.REGISTRY.register(
                ModBlocks.INDUSTRIAL_DRYING_RACK.get(),
                (level, state, face) -> {
                    BlockPos pos = state.hasProperty(IndustrialDryingRackBlock.HALF)
                            && state.getValue(IndustrialDryingRackBlock.HALF) == DoubleBlockHalf.UPPER
                            ? face.getPos().below()
                            : face.getPos();
                    return new InventoryIdentifier.Single(pos);
                }
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
        BlockPos targetPos = pos;
        BlockState targetState = state;
        if (state.hasProperty(IndustrialDryingRackBlock.HALF)
                && state.getValue(IndustrialDryingRackBlock.HALF) == DoubleBlockHalf.UPPER) {
            targetPos = pos.below();
            targetState = level.getBlockState(targetPos);
        }

        // Traditional racks keep vertical capability insertion closed. Industrial racks accept
        // top-down automation, but Packagers still normalize to a horizontal face for consistency.
        Direction effectiveSide = side == Direction.UP || side == Direction.DOWN
                ? Direction.NORTH
                : side;
        return UnpackingHandler.DEFAULT.unpack(
                level, targetPos, targetState, effectiveSide, items, orderContext, simulate
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
