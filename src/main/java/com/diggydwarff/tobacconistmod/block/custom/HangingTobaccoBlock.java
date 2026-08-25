package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.entity.HangingTobaccoBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/** Two-block traditional tobacco bunch attached to the underside of a solid block. */
public class HangingTobaccoBlock extends BaseEntityBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty CURE_STAGE = IntegerProperty.create("cure_stage", 0, 5);
    public static final IntegerProperty VARIETY = IntegerProperty.create("variety", 0, 5);

    // Selection bounds cover the upper knot and lower leaf mass.
    private static final VoxelShape UPPER_OUTLINE_SHAPE = box(3.5D, 2.75D, 3.5D, 12.5D, 15.25D, 12.5D);
    private static final VoxelShape LOWER_OUTLINE_SHAPE = box(1.5D, 8.0D, 1.5D, 14.5D, 16.0D, 14.5D);
    private static final ThreadLocal<Set<BlockPos>> REMOVING = ThreadLocal.withInitial(HashSet::new);

    public HangingTobaccoBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(CURE_STAGE, 0)
                .setValue(VARIETY, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(HangingTobaccoBlock::new);
    }

    public static int getVarietyIndex(ItemStack stack) {
        if (stack.is(ModItems.VIRGINIA_TOBACCO_LEAF.get()) || stack.is(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get())) return 1;
        if (stack.is(ModItems.BURLEY_TOBACCO_LEAF.get()) || stack.is(ModItems.BURLEY_TOBACCO_LEAF_DRY.get())) return 2;
        if (stack.is(ModItems.ORIENTAL_TOBACCO_LEAF.get()) || stack.is(ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get())) return 3;
        if (stack.is(ModItems.DOKHA_TOBACCO_LEAF.get()) || stack.is(ModItems.DOKHA_TOBACCO_LEAF_DRY.get())) return 4;
        if (stack.is(ModItems.SHADE_TOBACCO_LEAF.get()) || stack.is(ModItems.SHADE_TOBACCO_LEAF_DRY.get())) return 5;
        return 0; // Wild / fallback
    }

    public static boolean canPlaceBundle(Level level, BlockPos upperPos) {
        BlockPos lowerPos = upperPos.below();
        BlockPos supportPos = upperPos.above();
        BlockState support = level.getBlockState(supportPos);

        return level.getBlockState(upperPos).isAir()
                && level.getBlockState(lowerPos).isAir()
                && support.isFaceSturdy(level, supportPos, Direction.DOWN);
    }

    public static boolean placeBundle(Level level, BlockPos upperPos, ItemStack leaves) {
        if (!canPlaceBundle(level, upperPos) || leaves.isEmpty()) return false;

        Block block = ModBlocks.HANGING_TOBACCO_LEAVES.get();
        int variety = getVarietyIndex(leaves);
        int cureStage = com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper.isDryTobaccoLeaf(leaves) ? 5 : 0;
        BlockState upper = block.defaultBlockState()
                .setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(CURE_STAGE, cureStage)
                .setValue(VARIETY, variety);
        BlockState lower = block.defaultBlockState()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(CURE_STAGE, cureStage)
                .setValue(VARIETY, variety);

        // Install both halves before normal neighbor updates can validate the partner.
        level.setBlock(upperPos, upper, 2);
        level.setBlock(upperPos.below(), lower, 3);

        BlockEntity be = level.getBlockEntity(upperPos);
        if (be instanceof HangingTobaccoBlockEntity hanging) {
            hanging.setLeaves(leaves);
            return true;
        }

        level.removeBlock(upperPos.below(), false);
        level.removeBlock(upperPos, false);
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_OUTLINE_SHAPE : LOWER_OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos supportPos = pos.above();
            BlockState support = level.getBlockState(supportPos);
            return support.isFaceSturdy(level, supportPos, Direction.DOWN);
        }

        BlockState upper = level.getBlockState(pos.above());
        return upper.is(ModBlocks.HANGING_TOBACCO_LEAVES.get())
                && upper.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.UPPER) {
            if (direction == Direction.UP && !canSurvive(state, level, pos)) {
                return Blocks.AIR.defaultBlockState();
            }
            if (direction == Direction.DOWN) {
                BlockState lower = level.getBlockState(pos.below());
                if (!lower.is(ModBlocks.HANGING_TOBACCO_LEAVES.get())
                        || lower.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        } else if (direction == Direction.UP) {
            BlockState upper = level.getBlockState(pos.above());
            if (!upper.is(ModBlocks.HANGING_TOBACCO_LEAVES.get())
                    || upper.getValue(HALF) != DoubleBlockHalf.UPPER) {
                return Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    private static BlockPos upperPos(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos : pos.above();
    }

    @Nullable
    public static HangingTobaccoBlockEntity getBundleEntity(Level level, BlockState state, BlockPos pos) {
        BlockPos upper = upperPos(state, pos);
        BlockEntity be = level.getBlockEntity(upper);
        return be instanceof HangingTobaccoBlockEntity hanging ? hanging : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        HangingTobaccoBlockEntity hanging = getBundleEntity(level, state, pos);
        if (hanging == null) return InteractionResult.PASS;

        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(
                    "tobacconistmod.message.hanging.inspect",
                    hanging.getLeafCount(),
                    16,
                    hanging.getStatusComponent()
            ), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.getAbilities().instabuild) {
            HangingTobaccoBlockEntity hanging = getBundleEntity(level, state, pos);
            if (hanging != null) {
                hanging.discardContents();
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }

        BlockPos upper = upperPos(state, pos).immutable();
        Set<BlockPos> removing = REMOVING.get();
        boolean owner = removing.add(upper);
        try {
            if (owner) {
                if (!level.isClientSide) {
                    BlockEntity be = level.getBlockEntity(upper);
                    if (be instanceof HangingTobaccoBlockEntity hanging) {
                        hanging.dropContents(level, upper.below());
                    }
                }

                BlockPos partner = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos.above();
                BlockState partnerState = level.getBlockState(partner);
                if (partnerState.is(ModBlocks.HANGING_TOBACCO_LEAVES.get())) {
                    level.removeBlock(partner, false);
                }
            }
        } finally {
            if (owner) {
                removing.remove(upper);
                if (removing.isEmpty()) REMOVING.remove();
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER
                ? new HangingTobaccoBlockEntity(pos, state)
                : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(HALF) != DoubleBlockHalf.UPPER) return null;
        return createTickerHelper(type, ModBlockEntities.HANGING_TOBACCO.get(), HangingTobaccoBlockEntity::serverTick);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, CURE_STAGE, VARIETY);
    }
}
