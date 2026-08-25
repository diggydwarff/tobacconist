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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Factory rack that deliberately exposes leaf handling only through automation. */
public class IndustrialDryingRackBlock extends TobaccoDryingRackBlock {
    // The cleaned supplied rack model sits a little under two blocks tall; the selection shape
    // follows the visible steel frame and excludes the removed decorative top connector.
    private static final VoxelShape OUTLINE_SHAPE = box(0.685714D, 0.0D, 0.228571D, 16.457143D, 29.330286D, 16.457143D);

    // Structural frame collision only: the center stays open for players, smoke and visible airflow.
    private static final VoxelShape COLLISION_SHAPE = Shapes.or(
            box(0.685714, 0.0, 0.685714, 3.501714, 1.901714, 3.501714),
            box(13.714286, 0.0, 0.685714, 16.457143, 1.901714, 3.501714),
            box(0.685714, 0.0, 12.498286, 3.501714, 1.901714, 15.314286),
            box(13.714286, 0.0, 12.498286, 16.457143, 1.901714, 15.314286),
            box(3.428571, 0.0, 1.142857, 12.571429, 1.371429, 2.971429),
            box(3.428571, 0.0, 13.028571, 12.571429, 1.371429, 14.857143),
            box(1.142857, 0.0, 3.428571, 2.971429, 1.371429, 12.571429),
            box(14.171429, 0.0, 3.428571, 16.0, 1.371429, 12.571429),
            box(1.142857, 1.828571, 1.142857, 2.971429, 27.428571, 2.971429),
            box(14.171429, 1.828571, 1.142857, 16.0, 27.428571, 2.971429),
            box(1.142857, 1.828571, 13.028571, 2.971429, 27.428571, 14.857143),
            box(14.171429, 1.828571, 13.028571, 16.0, 27.428571, 14.857143),
            box(2.898286, 13.714286, 1.142857, 13.028571, 15.085714, 2.971429),
            box(2.898286, 13.714286, 13.028571, 13.028571, 15.085714, 14.857143),
            box(1.142857, 13.714286, 2.898286, 2.971429, 15.085714, 13.101714),
            box(14.171429, 13.714286, 2.898286, 16.0, 15.085714, 13.101714),
            box(0.685714, 27.355429, 0.685714, 15.314286, 29.257143, 3.428571),
            box(0.685714, 27.355429, 12.571429, 15.314286, 29.257143, 15.314286),
            box(0.685714, 27.428571, 3.355429, 3.428571, 29.257143, 12.644571),
            box(13.714286, 27.428571, 3.355429, 16.457143, 29.257143, 12.644571),
            box(4.342857, 28.342857, 3.355429, 6.171429, 29.330286, 12.644571),
            box(10.971429, 28.342857, 3.355429, 12.8, 29.257143, 12.644571),
            box(13.942857, 13.641143, 7.085714, 15.314286, 15.158857, 8.914286),
            box(14.628571, 12.8, 6.171429, 16.073143, 16.0, 9.828571),
            box(15.771429, 13.714286, 7.314286, 16.457143, 15.085714, 8.685714)
    );

    public IndustrialDryingRackBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
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
        BlockEntity blockEntity = level.getBlockEntity(pos);
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
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.INDUSTRIAL_DRYING_RACK.get(),
                (tickLevel, tickPos, tickState, rack) ->
                        TobaccoDryingRackBlockEntity.serverTick(tickLevel, tickPos, tickState, rack));
    }
}
