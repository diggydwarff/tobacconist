package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TobaccoDryingRackBlock extends BaseEntityBlock {

    public static final BooleanProperty HAS_LEAVES = BooleanProperty.create("has_leaves");
    public static final BooleanProperty OVER_CAMPFIRE = BooleanProperty.create("over_campfire");

    private static final VoxelShape SHAPE = Shapes.or(
            box(2, 0, 2, 3, 8, 3),
            box(13, 0, 2, 14, 8, 3),
            box(2, 0, 13, 3, 8, 14),
            box(13, 0, 13, 14, 8, 14),

            box(3, 7, 2, 13, 8, 3),
            box(3, 7, 13, 13, 8, 14),
            box(2, 7, 3, 3, 8, 13),
            box(13, 7, 3, 14, 8, 13),

            box(3, 7, 5, 13, 8, 6),
            box(3, 7, 8, 13, 8, 9),
            box(3, 7, 11, 13, 8, 12),

            box(3, 2, 2, 13, 3, 3),
            box(3, 2, 13, 13, 3, 14),
            box(2, 2, 3, 3, 3, 13),
            box(13, 2, 3, 14, 3, 13)
    );

    private static final VoxelShape SHAPE_FIRE = Shapes.or(
            // posts
            box(2, 0, 2, 3, 12, 3),
            box(13, 0, 2, 14, 12, 3),
            box(2, 0, 13, 3, 12, 14),
            box(13, 0, 13, 14, 12, 14),

            // top frame
            box(3, 11, 2, 13, 12, 3),
            box(3, 11, 13, 13, 12, 14),
            box(2, 11, 3, 3, 12, 13),
            box(13, 11, 3, 14, 12, 13),

            // hanging rails
            box(3, 10, 5, 13, 11, 6),
            box(3, 10, 8, 13, 11, 9),
            box(3, 10, 11, 13, 11, 12),

            // lower supports
            box(3, 2, 2, 13, 3, 3),
            box(3, 2, 13, 13, 3, 14),
            box(2, 2, 3, 3, 3, 13),
            box(13, 2, 3, 14, 3, 13)
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(OVER_CAMPFIRE) ? SHAPE_FIRE : SHAPE;
    }

    public TobaccoDryingRackBlock() {
        super(Properties.of().strength(1.5F).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HAS_LEAVES, false)
                .setValue(OVER_CAMPFIRE, false));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(OVER_CAMPFIRE) ? SHAPE_FIRE : SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TobaccoDryingRackBlockEntity rack)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {

                if (!rack.hasLeaves()) {
                    player.displayClientMessage(
                            Component.literal("Rack: Empty"),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            Component.literal(
                                    "Rack: " + rack.getLeafCount() + "/16 | " + rack.getRackStatusText()
                            ),
                            true
                    );
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty()) {
            if (rack.isBatchLocked()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.literal("This batch is already drying. Remove it to restart."),
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

        if (!held.isEmpty()) {
            if (rack.canAccept(held)) {
                if (!level.isClientSide) {
                    boolean inserted = rack.addOneLeaf(held);
                    if (inserted && !player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        } else {
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
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TobaccoDryingRackBlockEntity rack) {
                rack.dropContents(level, pos);
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
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.TOBACCO_DRYING_RACK.get(),
                TobaccoDryingRackBlockEntity::serverTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HAS_LEAVES, OVER_CAMPFIRE);
    }
}