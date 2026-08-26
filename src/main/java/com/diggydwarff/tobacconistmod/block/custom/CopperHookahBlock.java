package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

/** Vanilla-style oxidation/waxing wrapper around the existing two-block Hookah behavior. */
public class CopperHookahBlock extends DoubleHookahBlock {
    private final int age;
    private final boolean waxed;
    public CopperHookahBlock(Properties properties, int age, boolean waxed) {
        super(properties);
        this.age = age;
        this.waxed = waxed;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !waxed && age < 3 && state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.05F) transition(level, pos, state, blockFor(age + 1, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.HONEYCOMB) && !waxed) {
            if (!level.isClientSide) {
                transition(level, pos, state, blockFor(age, true));
                if (!player.getAbilities().instabuild) stack.shrink(1);
                level.levelEvent(player, 3003, lowerPos(pos, state), 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.getItem() instanceof AxeItem && (waxed || age > 0)) {
            if (!level.isClientSide) {
                transition(level, pos, state, blockFor(waxed ? age : age - 1, false));
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                level.levelEvent(player, waxed ? 3004 : 3005, lowerPos(pos, state), 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    private static BlockPos lowerPos(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }

    private void transition(Level level, BlockPos pos, BlockState clickedState, Block target) {
        BlockPos lower = lowerPos(pos, clickedState);
        BlockState oldLower = level.getBlockState(lower);
        if (!(oldLower.getBlock() instanceof CopperHookahBlock)) return;
        HookahEntity oldEntity = level.getBlockEntity(lower) instanceof HookahEntity h ? h : null;
        var saved = oldEntity == null ? null : oldEntity.saveTransferData();
        BlockState replacement = target.defaultBlockState()
                .setValue(FACING, oldLower.getValue(FACING))
                .setValue(LIT, oldLower.getValue(LIT))
                .setValue(HALF, DoubleBlockHalf.LOWER);
        // Replace both halves without intermediate shape validation or item drops.
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
        BlockPos upper = lower.above();
        level.setBlock(upper, replacement.setValue(HALF, DoubleBlockHalf.UPPER), flags);
        level.setBlock(lower, replacement, flags);

        // Run neighbor updates after both halves have been replaced.
        level.updateNeighborsAt(lower, target);
        level.updateNeighborsAt(upper, target);
        BlockEntity be = level.getBlockEntity(lower);
        if (saved != null && be instanceof HookahEntity hookah) hookah.loadTransferData(saved);
    }

    private static Block blockFor(int age, boolean waxed) {
        return switch (age) {
            case 0 -> waxed ? ModBlocks.WAXED_COPPER_HOOKAH.get() : ModBlocks.ORNATE_COPPER_HOOKAH.get();
            case 1 -> waxed ? ModBlocks.WAXED_EXPOSED_COPPER_HOOKAH.get() : ModBlocks.EXPOSED_COPPER_HOOKAH.get();
            case 2 -> waxed ? ModBlocks.WAXED_WEATHERED_COPPER_HOOKAH.get() : ModBlocks.WEATHERED_COPPER_HOOKAH.get();
            default -> waxed ? ModBlocks.WAXED_OXIDIZED_COPPER_HOOKAH.get() : ModBlocks.OXIDIZED_COPPER_HOOKAH.get();
        };
    }
}
