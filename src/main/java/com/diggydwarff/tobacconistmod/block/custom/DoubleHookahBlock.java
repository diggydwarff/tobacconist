package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.datagen.items.custom.HookahHoseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class DoubleHookahBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape LOWER_SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    private static final VoxelShape UPPER_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) return;

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.98D;
        double z = pos.getZ() + 0.5D;

        if (random.nextFloat() < 0.7F) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x + (random.nextDouble() - 0.5D) * 0.05D,
                    y,
                    z + (random.nextDouble() - 0.5D) * 0.05D,
                    0.0D, 0.03D, 0.0D);
        }

        if (random.nextFloat() < 0.25F) {
            level.addParticle(ParticleTypes.SMOKE,
                    x + (random.nextDouble() - 0.5D) * 0.04D,
                    y + 0.03D,
                    z + (random.nextDouble() - 0.5D) * 0.04D,
                    0.0D, 0.02D, 0.0D);
        }
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
        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.is(this) && otherState.getValue(HALF) != half) {
            level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
            level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {

        BlockPos entityPos = getEntityPos(state, pos);
        BlockEntity blockEntity = level.getBlockEntity(entityPos);

        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(entityPos);

            try {
                boolean isSmoking = ((HookahEntity) blockEntity).progress > 0;
                if (isSmoking) {
                    for (ItemStack stack : player.getHandSlots()) {
                        if (stack.getItem() instanceof HookahHoseItem) {
                            Vec3 look = player.getLookAngle();
                            Vec3 eyePos = new Vec3(player.getX(), player.getY() + 1.4, player.getZ());

                            look = look.multiply(0.3D, 0.3D, 0.3D);
                            eyePos = eyePos.add(look);
                            look = look.multiply(0.066D, 0.066D, 0.066D);

                            Random rand = new Random();
                            for (int i = 0; i < 5; ++i) {
                                Vec3 newVec = new Vec3(rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D);
                                newVec = newVec.multiply(0.01D, 0.01D, 0.01D);
                                Vec3 mergeVec = look.add(newVec);
                                ServerLevel sLevel = (ServerLevel) level;
                                sLevel.sendParticles(
                                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                        player.getX() + mergeVec.x,
                                        player.getY() + 1.4 + mergeVec.y,
                                        player.getZ() + mergeVec.z,
                                        1, 0, 0, 0, 0
                                );
                            }

                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }
                    }
                }
            } catch (Exception ignored) {
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
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? new HookahEntity(pos, state)
                : null;
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