package com.diggydwarff.tobacconistmod.vapor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/** A filled state of the vanilla cauldron; there is no separate item or crafting recipe. */
public final class ExtractCauldronBlock extends AbstractCauldronBlock implements EntityBlock {
    public static final MapCodec<ExtractCauldronBlock> CODEC = simpleCodec(ExtractCauldronBlock::new);

    public ExtractCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, CauldronInteraction.EMPTY);
        registerDefaultState(stateDefinition.any().setValue(LayeredCauldronBlock.LEVEL, 3));
    }
    @Override protected MapCodec<ExtractCauldronBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LayeredCauldronBlock.LEVEL);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ExtractCauldronEntity(pos, state); }
    @Override public boolean isFull(BlockState state) { return state.getValue(LayeredCauldronBlock.LEVEL) == 3; }
    @Override protected double getContentHeight(BlockState state) { return (6 + state.getValue(LayeredCauldronBlock.LEVEL) * 3) / 16.0; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return state.getValue(LayeredCauldronBlock.LEVEL); }
    @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) { return new ItemStack(Items.CAULDRON); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                       Player player, InteractionHand hand, BlockHitResult hit) {
        // Every interaction is handled by the extraction event, including empty-hand use.
        // Consume client prediction as well so a held potion is never drunk by accident.
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof ExtractCauldronEntity cauldron) cauldron.dropReturns();
        super.onRemove(state, level, pos, next, moving);
    }
}
