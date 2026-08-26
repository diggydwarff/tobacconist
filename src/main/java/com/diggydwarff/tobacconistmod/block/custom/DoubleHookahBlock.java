package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.HookahHoseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;


public class DoubleHookahBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // The visible material Hookahs are narrow 1.5-block-tall models.  Do not give
    // either half a full-cube outline/collision/light footprint.
    private static final VoxelShape LOWER_SHAPE = Block.box(2.5, 0, 2.5, 13.5, 16, 13.5);
    private static final VoxelShape UPPER_SHAPE = Block.box(2.5, 0, 2.5, 13.5, 12, 13.5);
    public DoubleHookahBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : UPPER_SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();

        if (pos.getY() >= level.getMaxBuildHeight() - 1) {
            return null;
        }

        BlockPos above = pos.above();
        if (!level.getBlockState(above).canBeReplaced(ctx)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(LIT, false)
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            level.setBlock(pos.above(), state
                    .setValue(HALF, DoubleBlockHalf.UPPER), 3);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.LOWER) {
            return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        } else {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.LOWER) {
            if (direction == Direction.UP) {
                if (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.UPPER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }

            if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
                return Blocks.AIR.defaultBlockState();
            }
        } else {
            if (direction == Direction.DOWN) {
                if (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos lowerPos = half == DoubleBlockHalf.LOWER ? pos : pos.below();

        if (!level.isClientSide && player.getAbilities().instabuild) {
            BlockEntity blockEntity = level.getBlockEntity(lowerPos);
            if (blockEntity instanceof HookahEntity hookah) {
                hookah.clearContentsForCreativeBreak();
            }

            // Let vanilla/NeoForge perform the Creative break exactly once. The event
            // guards suppress loot from both this half and the partner teardown.
            super.playerWillDestroy(level, pos, state, player);
            return;
        }

        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(this) && otherState.getValue(HALF) != half) {
            level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
            level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        // Do not let either half's loot table produce a Hookah item in Creative.
        if (player.getAbilities().instabuild) {
            return;
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        boolean copperTransition = state.getBlock() instanceof CopperHookahBlock
                && newState.getBlock() instanceof CopperHookahBlock;
        if (state.getBlock() != newState.getBlock() && !copperTransition) {
            if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof HookahEntity hookahEntity) {
                    hookahEntity.drops();
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    private BlockPos getEntityPos(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return handleUse(state, level, pos, player, hand, hit);
    }

    private InteractionResult handleUse(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {

        BlockPos entityPos = getEntityPos(state, pos);
        BlockEntity blockEntity = level.getBlockEntity(entityPos);

        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(entityPos);

            if (blockEntity instanceof HookahEntity hookah && hookah.progress > 0) {
                for (ItemStack stack : player.getHandSlots()) {
                    if (stack.getItem() instanceof HookahHoseItem) {
                        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
                            return InteractionResult.sidedSuccess(false);
                        }

                        ItemStack shisha = hookah.getShishaForSmoking();
                        if (shisha.isEmpty()) return InteractionResult.PASS;

                        SmokingItem.applyHookahSmokingEffects(player, (ServerLevel) level, shisha);
                        hookah.applyDirtyWaterPenalty(player);
                        player.getCooldowns().addCooldown(stack.getItem(), 20);
                        return InteractionResult.sidedSuccess(false);
                    }
                }
            }

            if (entity instanceof HookahEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (HookahEntity) entity, entityPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Both halves expose the same BE type so capabilities/Display Links/Mechanical Arms can
        // discover a tall Hookah from either level. HookahEntity delegates upper-half access to
        // the lower master; only the lower half receives a ticker.
        return new HookahEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return null;
        }

        return createTickerHelper(type, ModBlockEntities.HOOKAH.get(), HookahEntity::tick);
    }
}