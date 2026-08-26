package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock;
import com.diggydwarff.tobacconistmod.block.entity.FlueFireboxBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

/** Native Create Mechanical Arm interaction points for Tobacconist processing blocks. */
public final class CreateArmCompat {
    private static final DeferredRegister<ArmInteractionPointType> ARM_POINTS =
            DeferredRegister.create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, TobacconistMod.MODID);

    private static final RegistryObject<TobacconistArmPointType> TOBACCONIST_MACHINES =
            ARM_POINTS.register("tobacconist_machines", TobacconistArmPointType::new);

    private CreateArmCompat() {}

    public static void register(IEventBus modEventBus) {
        ARM_POINTS.register(modEventBus);
        TobacconistMod.LOGGER.info("Create Mechanical Arm Tobacconist interaction points enabled.");
    }

    private static final class TobacconistArmPointType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            return blockEntity instanceof TobaccoDryingRackBlockEntity
                    || blockEntity instanceof TobaccoBarrelBlockEntity
                    || blockEntity instanceof FlueFireboxBlockEntity
                    || blockEntity instanceof HookahEntity;
        }

        @Nullable
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TobaccoDryingRackBlockEntity) {
                return new DryingRackPoint(this, level, pos, state);
            }
            if (blockEntity instanceof TobaccoBarrelBlockEntity) {
                return new TobaccoBarrelPoint(this, level, pos, state);
            }
            if (blockEntity instanceof FlueFireboxBlockEntity) {
                return new FlueFireboxPoint(this, level, pos, state);
            }
            if (blockEntity instanceof HookahEntity) {
                return new HookahPoint(this, level, pos, state);
            }
            return null;
        }

        @Override
        public int getPriority() {
            // Win over any generic point that may also recognize one of these blocks.
            return 100;
        }
    }

    private abstract static class TobacconistPoint extends ArmInteractionPoint {
        protected TobacconistPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public void updateCachedState() {
            BlockState previous = cachedState;
            super.updateCachedState();
            if (previous != cachedState) {
                cachedAngles = null;
            }
        }
    }

    private static final class DryingRackPoint extends TobacconistPoint {
        private DryingRackPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Nullable
        @Override
        protected IItemHandler getHandler() {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            return blockEntity instanceof TobaccoDryingRackBlockEntity rack
                    ? rack.getItemHandler(null)
                    : null;
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            boolean raised = cachedState.hasProperty(TobaccoDryingRackBlock.OVER_CAMPFIRE)
                    && cachedState.getValue(TobaccoDryingRackBlock.OVER_CAMPFIRE);
            return Vec3.atLowerCornerOf(pos).add(.5D, raised ? .66D : .48D, .5D);
        }
    }

    private static final class TobaccoBarrelPoint extends TobacconistPoint {
        private TobaccoBarrelPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Nullable
        @Override
        protected IItemHandler getHandler() {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            return blockEntity instanceof TobaccoBarrelBlockEntity barrel
                    ? barrel.getItemHandler(null)
                    : null;
        }

        @Override
        public ItemStack extract(int slot, int amount, boolean simulate) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TobaccoBarrelBlockEntity barrel)
                    || barrel.isAutomatedExtractionLocked()) {
                return ItemStack.EMPTY;
            }
            return super.extract(slot, amount, simulate);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            return Vec3.atLowerCornerOf(pos).add(.5D, .88D, .5D);
        }
    }

    private abstract static class DepositOnlyPoint extends TobacconistPoint {
        protected DepositOnlyPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public void cycleMode() {
            // These blocks only consume inputs; they have no meaningful Arm output.
        }

        @Override
        public ItemStack extract(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotCount() {
            return 0;
        }
    }

    private static final class FlueFireboxPoint extends DepositOnlyPoint {
        private FlueFireboxPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Nullable
        @Override
        protected IItemHandler getHandler() {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            return blockEntity instanceof FlueFireboxBlockEntity firebox
                    ? firebox.getItemHandler(null)
                    : null;
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            Direction facing = cachedState.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING)
                    .orElse(Direction.NORTH);
            Vec3 front = Vec3.atLowerCornerOf(facing.getNormal()).scale(.43D);
            return Vec3.atLowerCornerOf(pos).add(.5D, .55D, .5D).add(front);
        }

        @Override
        protected Direction getInteractionDirection() {
            return cachedState.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING)
                    .orElse(Direction.NORTH)
                    .getOpposite();
        }
    }

    private static final class HookahPoint extends DepositOnlyPoint {
        private HookahPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Nullable
        @Override
        protected IItemHandler getHandler() {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            return blockEntity instanceof HookahEntity hookah
                    ? hookah.getItemHandler()
                    : null;
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            Direction facing = cachedState.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING)
                    .orElse(Direction.NORTH);
            Vec3 front = Vec3.atLowerCornerOf(facing.getNormal()).scale(.38D);
            return Vec3.atLowerCornerOf(pos).add(.5D, .68D, .5D).add(front);
        }

        @Override
        protected Direction getInteractionDirection() {
            return cachedState.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING)
                    .orElse(Direction.NORTH)
                    .getOpposite();
        }
    }
}
