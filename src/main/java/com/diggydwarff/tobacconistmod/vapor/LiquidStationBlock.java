package com.diggydwarff.tobacconistmod.vapor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

public final class LiquidStationBlock extends BaseEntityBlock {
    private final boolean blending;
    public LiquidStationBlock(Properties properties, boolean blending) {
        super(properties); this.blending = blending;
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }
    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                                                          BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return blending ? VaporBenchShape.forFacing(state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                : super.getShape(state, level, pos, context);
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(BlockStateProperties.HORIZONTAL_FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite()); }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING))); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LiquidStationEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, VaporContent.STATION.get(), LiquidStationEntity::tick);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        open(level, pos, player); return InteractionResult.sidedSuccess(level.isClientSide);
    }
    private void open(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof LiquidStationEntity station) {
            net.minecraftforge.network.NetworkHooks.openScreen(server, station, buf -> { buf.writeBlockPos(pos); buf.writeBoolean(blending); });
        }
    }
    @Override public java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootContext.Builder builder) {
        var drops = super.getDrops(state, builder);
        var entity = builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (entity instanceof LiquidStationEntity station && station.isBlending()) {
            for (ItemStack drop : drops) if (drop.is(state.getBlock().asItem())) {
                net.minecraft.world.item.BlockItem.setBlockEntityData(drop, VaporContent.STATION.get(), station.portableTanks());
            }
        }
        return drops;
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof LiquidStationEntity station) {
            station.dropContents();
        }
        super.onRemove(state, level, pos, next, moving);
    }
}
