package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.entity.IndustrialDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Factory rack that deliberately exposes leaf handling only through automation. */
public class IndustrialDryingRackBlock extends TobaccoDryingRackBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = TobaccoDryingRackBlock.HALF;

    // Each half owns a normal one-block selection volume. The full visual model is rendered by
    // the lower half, while the upper half exists as an invisible interaction/capability proxy.
    private static final VoxelShape HALF_OUTLINE_SHAPE = Shapes.block();

    private static final VoxelShape LOWER_COLLISION_SHAPE = Shapes.or(
            box(0.0, 0.0, 0.0, 3.08, 2.08, 3.08),
            box(12.92, 0.0, 0.0, 16.0, 2.08, 3.08),
            box(0.0, 0.0, 12.92, 3.08, 2.08, 16.0),
            box(12.92, 0.0, 12.92, 16.0, 2.08, 16.0),
            box(0.5, 2.0, 0.5, 2.5, 16.0, 2.5),
            box(13.5, 2.0, 0.5, 15.5, 16.0, 2.5),
            box(0.5, 2.0, 13.5, 2.5, 16.0, 15.5),
            box(13.5, 2.0, 13.5, 15.5, 16.0, 15.5),
            box(2.42, 15.0, 0.5, 13.5, 16.0, 2.5),
            box(2.42, 15.0, 13.5, 13.5, 16.0, 15.5),
            box(0.5, 15.0, 2.42, 2.5, 16.0, 13.58),
            box(13.5, 15.0, 2.42, 15.5, 16.0, 13.58)
    );

    private static final VoxelShape UPPER_COLLISION_SHAPE = Shapes.or(
            box(0.5, 0.0, 0.5, 2.5, 14.0, 2.5),
            box(13.5, 0.0, 0.5, 15.5, 14.0, 2.5),
            box(0.5, 0.0, 13.5, 2.5, 14.0, 15.5),
            box(13.5, 0.0, 13.5, 15.5, 14.0, 15.5),
            box(0.0, 13.92, 0.0, 16.0, 16.0, 3.0),
            box(0.0, 13.92, 13.0, 16.0, 16.0, 16.0),
            box(0.0, 14.0, 2.92, 3.0, 16.0, 13.08),
            box(13.0, 14.0, 2.92, 16.0, 16.0, 13.08),
            box(4.0, 15.0, 2.92, 6.0, 16.0, 13.08),
            box(10.0, 15.0, 2.92, 12.0, 16.0, 13.08)
    );

    public IndustrialDryingRackBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return HALF_OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_COLLISION_SHAPE : UPPER_COLLISION_SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(IndustrialDryingRackBlock::new);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        showAutomationStatus(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        showAutomationStatus(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void showAutomationStatus(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        BlockState state = level.getBlockState(pos);
        BlockPos entityPos = state.is(this) && state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockEntity blockEntity = level.getBlockEntity(entityPos);
        if (blockEntity instanceof IndustrialDryingRackBlockEntity rack && player.isShiftKeyDown()) {
            player.displayClientMessage(Component.translatable(
                    "tobacconistmod.message.rack.inspect",
                    rack.getLeafCount(), rack.getMaxLeaves(), rack.getRackStatusComponent()), true);
            return;
        }
        player.displayClientMessage(Component.translatable(
                "tobacconistmod.message.industrial_rack.automation_only"), true);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialDryingRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(HALF) == DoubleBlockHalf.UPPER) return null;
        return createTickerHelper(type, ModBlockEntities.INDUSTRIAL_DRYING_RACK.get(),
                (tickLevel, tickPos, tickState, rack) -> {
                    // Self-heal racks placed before the block became a true two-block structure.
                    if (tickLevel.isEmptyBlock(tickPos.above())) {
                        tickLevel.setBlock(tickPos.above(), tickState.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
                    }
                    TobaccoDryingRackBlockEntity.serverTick(tickLevel, tickPos, tickState, rack);
                });
    }

}
