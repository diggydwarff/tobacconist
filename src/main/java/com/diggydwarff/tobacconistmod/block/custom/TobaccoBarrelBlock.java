package com.diggydwarff.tobacconistmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TobaccoBarrelBlock extends BaseEntityBlock {

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TobaccoBarrelBlock::new);
    }

    public TobaccoBarrelBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {

        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TobaccoBarrelBlockEntity barrel) {
                ItemStack stored = barrel.getStoredTobaccoCopy();
                if (!stored.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stored);
                }
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(
                type,
                ModBlockEntities.TOBACCO_BARREL.get(),
                level.isClientSide ? TobaccoBarrelBlockEntity::clientTick : TobaccoBarrelBlockEntity::serverTick
        );
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
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TobaccoBarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        // Shift-right-click = info only
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                Component[] lines = barrel.getStatusMessage();
                player.displayClientMessage(lines[0], false);
                player.displayClientMessage(lines[1], false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Empty hand = remove stack
        if (held.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack extracted = barrel.removeStoredTobacco();
                if (!extracted.isEmpty()) {
                    if (!player.addItem(extracted)) {
                        player.drop(extracted, false);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            int inserted = barrel.tryInsertTobacco(held);

            if (inserted > 0) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(inserted);
                }
            } else {
                player.displayClientMessage(Component.literal("Cannot insert that tobacco batch."), true);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TobaccoBarrelBlockEntity(pos, state);
    }
}