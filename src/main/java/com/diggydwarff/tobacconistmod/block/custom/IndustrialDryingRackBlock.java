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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Factory rack that deliberately exposes leaf handling only through automation. */
public class IndustrialDryingRackBlock extends TobaccoDryingRackBlock {
    public IndustrialDryingRackBlock(Properties properties) {
        super(properties);
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
