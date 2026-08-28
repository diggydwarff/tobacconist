package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TobaccoDryingRackBlock extends BaseEntityBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty HAS_LEAVES = BooleanProperty.create("has_leaves");
    public static final BooleanProperty OVER_CAMPFIRE = BooleanProperty.create("over_campfire");
    /**
     * Coarse visual fill level for the four drying-rack models.
     * 0 = empty, 1 = lightly loaded, 2 = mostly loaded, 3 = completely full.
     */
    public static final IntegerProperty LOAD_STAGE = IntegerProperty.create("load_stage", 0, 3);
    /** Visual cure-color stage: 0 = raw green through 5 = fully cured brown. */
    public static final IntegerProperty CURE_STAGE = IntegerProperty.create("cure_stage", 0, 5);
    /** Tobacco variety tint, matching hanging bunches: 0 wild, 1 Virginia, 2 Burley, 3 Oriental, 4 Dokha, 5 Shade. */
    public static final IntegerProperty VARIETY = IntegerProperty.create("variety", 0, 5);

    // The wooden rack is deliberately contained inside one Minecraft block again.  The old
    // UPPER state is retained only as a migration shim so worlds saved with the temporary
    // two-block proxy can clean that proxy up safely.
    private static final VoxelShape LOWER_OUTLINE_SHAPE = Shapes.block();
    private static final VoxelShape UPPER_OUTLINE_SHAPE = Shapes.empty();

    // Collision follows the outer wooden frame while leaving the center open.
    private static final VoxelShape COLLISION_SHAPE = Shapes.or(
            box(0.562162, 0.0, 0.562162, 2.032432, 16.0, 2.032432),
            box(13.967568, 0.0, 0.562162, 15.437838, 16.0, 2.032432),
            box(0.562162, 0.0, 13.967568, 2.032432, 16.0, 15.437838),
            box(13.967568, 0.0, 13.967568, 15.437838, 16.0, 15.437838),
            box(0.648649, 1.0, 0.648649, 1.945946, 2.5, 15.351351),
            box(14.054054, 1.0, 0.648649, 15.351351, 2.5, 15.351351),
            box(1.945946, 1.0, 0.648649, 14.054054, 2.5, 1.945946),
            box(1.945946, 1.0, 14.054054, 14.054054, 2.5, 15.351351),
            box(0.648649, 13.5, 0.648649, 1.945946, 15.0, 15.351351),
            box(14.054054, 13.5, 0.648649, 15.351351, 15.0, 15.351351),
            box(1.945946, 13.5, 0.648649, 14.054054, 15.0, 1.945946),
            box(1.945946, 13.5, 14.054054, 14.054054, 15.0, 15.351351)
    );

    private static final VoxelShape UPPER_COLLISION_SHAPE = Shapes.empty();

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_OUTLINE_SHAPE : LOWER_OUTLINE_SHAPE;
    }
    public TobaccoDryingRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(HAS_LEAVES, false)
                .setValue(OVER_CAMPFIRE, false)
                .setValue(LOAD_STAGE, 0)
                .setValue(CURE_STAGE, 0)
                .setValue(VARIETY, 0));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_COLLISION_SHAPE : COLLISION_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // UPPER is a legacy state from the short-lived two-block wooden-rack implementation.
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /**
     * Wooden racks only retain UPPER as a migration shim. Genuine two-block subclasses override
     * this so the shared destruction path does not delete their legitimate upper half.
     */
    protected boolean hasPersistentUpperHalf() {
        return false;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!hasPersistentUpperHalf() && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            // Clean up a legacy upper proxy if this rack was saved while the two-block version existed.
            BlockPos upperPos = pos.above();
            BlockState upperState = level.getBlockState(upperPos);
            if (upperState.is(this) && upperState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                level.setBlock(upperPos, Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(OVER_CAMPFIRE) || random.nextFloat() >= 0.55F) {
            return;
        }

        // Continue the campfire plume above the rack's top rail.
        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.24D;
        double y = pos.getY() + 1.04D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.24D;
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0D, 0.07D, 0.0D);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return handleUse(state, level, pos, player, hand, hit);
    }

    private InteractionResult handleUse(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TobaccoDryingRackBlockEntity rackAtPos)) {
            return InteractionResult.PASS;
        }
        TobaccoDryingRackBlockEntity rack = rackAtPos.getMasterRack();

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {

                player.displayClientMessage(
                        Component.translatable(
                                "tobacconistmod.message.rack.inspect",
                                rack.getLeafCount(),
                                rack.getMaxLeaves(),
                                rack.getRackStatusComponent()
                        ),
                        true
                );
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty()) {
            if (rack.isBatchLocked()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("tobacconistmod.message.rack.batch_locked"),
                            true
                    );
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (rack.canAccept(held)) {
                if (!level.isClientSide) {
                    boolean inserted = rack.addOneLeaf(held);
                    if (inserted && !player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        if (held.isEmpty()) {
            if (rack.hasLeaves()) {
                if (!level.isClientSide) {
                    ItemStack removed = rack.removeAllLeaves();
                    if (!removed.isEmpty() && !player.addItem(removed)) {
                        player.drop(removed, false);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TobaccoDryingRackBlockEntity rack) {
                    rack.dropContents(level, pos);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TobaccoDryingRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return createTickerHelper(type, ModBlockEntities.TOBACCO_DRYING_RACK.get(),
                    (tickLevel, tickPos, tickState, rack) -> tickLevel.setBlock(
                            tickPos, Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS));
        }
        return createTickerHelper(type, ModBlockEntities.TOBACCO_DRYING_RACK.get(),
                TobaccoDryingRackBlockEntity::serverTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HALF, HAS_LEAVES, OVER_CAMPFIRE, LOAD_STAGE, CURE_STAGE, VARIETY);
    }
}