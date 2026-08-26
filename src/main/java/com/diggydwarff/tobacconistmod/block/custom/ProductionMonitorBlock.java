package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Directional throughput counter. The HORIZONTAL_FACING value and visible front face point at
 * the adjacent transport block being monitored; the opposite face remains outward toward the operator.
 */
public class ProductionMonitorBlock extends BaseEntityBlock {
    public static final MapCodec<ProductionMonitorBlock> CODEC = simpleCodec(ProductionMonitorBlock::new);

    public ProductionMonitorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Stand behind the monitor and look at the transport you want to observe.
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND && isExternalFilterSlotHit(state, pos, hit) && !isCreateWrench(stack)) {
            setExternalFilter(level, pos, stack);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        InteractionResult result = openMenu(level, pos, player);
        return result.consumesAction()
                ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (isExternalFilterSlotHit(state, pos, hit)) {
            setExternalFilter(level, pos, ItemStack.EMPTY);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return openMenu(level, pos, player);
    }

    /**
     * Create-style external ghost-filter target on the operator-facing side of the monitor.
     * The configured stack is never consumed; even Create Filter/Attribute Filter items remain
     * configuration ghosts, matching the Production Monitor's GUI semantics.
     */
    private void setExternalFilter(Level level, BlockPos pos, ItemStack stack) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ProductionMonitorBlockEntity monitor
                && monitor.setExternalFilter(stack)) {
            level.playSound(null, pos,
                    stack.isEmpty() ? SoundEvents.ITEM_FRAME_REMOVE_ITEM : SoundEvents.ITEM_FRAME_ADD_ITEM,
                    SoundSource.BLOCKS, 0.25F, 0.9F);
        }
    }

    public static boolean isExternalFilterSlotHit(BlockState state, BlockPos pos, BlockHitResult hit) {
        Direction monitoredDirection = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction operatorFace = monitoredDirection.getOpposite();
        if (hit.getDirection() != operatorFace) return false;

        Vec3 local = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        double horizontal = operatorFace.getAxis() == Direction.Axis.X ? local.z : local.x;
        return horizontal >= 0.25D && horizontal <= 0.75D
                && local.y >= 0.25D && local.y <= 0.75D;
    }

    private boolean isCreateWrench(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return "create".equals(key.getNamespace()) && "wrench".equals(key.getPath());
    }

    private InteractionResult openMenu(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ProductionMonitorBlockEntity monitor) {
            serverPlayer.openMenu(monitor, buf -> {
                // Bootstrap the client menu with authoritative state immediately. Data slots continue
                // synchronizing live count/rate changes after the screen has opened.
                buf.writeBlockPos(pos);
                buf.writeLong(monitor.getCount());
                buf.writeVarInt(monitor.getTarget());
                buf.writeByte(monitor.getCountMode().ordinal());
                buf.writeByte(monitor.getAtTargetMode().ordinal());
                buf.writeByte(monitor.getOutputMode().ordinal());
                buf.writeBoolean(monitor.isExternalResetEnabled());
                buf.writeVarInt((int) Math.min(Integer.MAX_VALUE, Math.round(monitor.getRollingRate() * 100.0D)));
                buf.writeBoolean(monitor.isTargetValid());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, monitor.getFilter());
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof ProductionMonitorBlockEntity monitor
                ? monitor.getRedstoneSignal()
                : 0;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ProductionMonitorBlockEntity monitor
                ? monitor.getComparatorSignal()
                : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProductionMonitorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.PRODUCTION_MONITOR.get(),
                ProductionMonitorBlockEntity::serverTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }
}
