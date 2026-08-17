package com.diggydwarff.tobacconistmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
            // chunky corner feet + posts
            box(1.5, 0, 1.5, 4.5, 1, 4.5),
            box(11.5, 0, 1.5, 14.5, 1, 4.5),
            box(1.5, 0, 11.5, 4.5, 1, 14.5),
            box(11.5, 0, 11.5, 14.5, 1, 14.5),
            box(2, 1, 2, 4, 8.5, 4),
            box(12, 1, 2, 14, 8.5, 4),
            box(2, 1, 12, 4, 8.5, 14),
            box(12, 1, 12, 14, 8.5, 14),

            // upper frame
            box(4, 6.5, 2, 12, 8, 4),
            box(4, 6.5, 12, 12, 8, 14),
            box(2, 6.5, 4, 4, 8, 12),
            box(12, 6.5, 4, 14, 8, 12),

            // drying slats
            box(4, 6.75, 5, 12, 7.5, 6.5),
            box(4, 6.75, 7.25, 12, 7.5, 8.75),
            box(4, 6.75, 9.5, 12, 7.5, 11),

            // lower braces
            box(4, 2, 2.5, 12, 3.5, 3.5),
            box(4, 2, 12.5, 12, 3.5, 13.5),
            box(2.5, 2, 4, 3.5, 3.5, 12),
            box(12.5, 2, 4, 13.5, 3.5, 12)
    );

    private static final VoxelShape SHAPE_FIRE = Shapes.or(
            // tall posts; renderer continues these downward into the campfire block below
            box(2, 0, 2, 4, 12.5, 4),
            box(12, 0, 2, 14, 12.5, 4),
            box(2, 0, 12, 4, 12.5, 14),
            box(12, 0, 12, 14, 12.5, 14),

            // upper frame
            box(4, 10.5, 2, 12, 12, 4),
            box(4, 10.5, 12, 12, 12, 14),
            box(2, 10.5, 4, 4, 12, 12),
            box(12, 10.5, 4, 14, 12, 12),

            // hanging slats
            box(4, 9.75, 5, 12, 10.5, 6.5),
            box(4, 9.75, 7.25, 12, 10.5, 8.75),
            box(4, 9.75, 9.5, 12, 10.5, 11),

            // lower braces
            box(4, 2, 2.5, 12, 3.5, 3.5),
            box(4, 2, 12.5, 12, 3.5, 13.5),
            box(2.5, 2, 4, 3.5, 3.5, 12),
            box(12.5, 2, 4, 13.5, 3.5, 12)
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(OVER_CAMPFIRE) ? SHAPE_FIRE : SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TobaccoDryingRackBlock::new);
    }

    public TobaccoDryingRackBlock(Properties properties) {
        super(properties);
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
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = handleUse(state, level, pos, player, hand, hit);
        return result.consumesAction() ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return handleUse(state, level, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    private InteractionResult handleUse(BlockState state, Level level, BlockPos pos,
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