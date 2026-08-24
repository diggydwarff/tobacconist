package com.diggydwarff.tobacconistmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

/** Two-block counterpart to the normal dyeable Hookah. */
public class DyeableDoubleHookahBlock extends DoubleHookahBlock {
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    @Override
    protected MapCodec<? extends DoubleHookahBlock> codec() {
        return simpleCodec(DyeableDoubleHookahBlock::new);
    }

    public DyeableDoubleHookahBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(COLOR, DyeColor.LIGHT_GRAY).setValue(GLOWING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR, GLOWING);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof DyeItem dye) {
            DyeColor color = dye.getDyeColor();
            if (state.getValue(COLOR) != color) {
                if (!level.isClientSide) {
                    setBoth(level, pos, state, COLOR, color);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        if (stack.is(Items.GLOW_INK_SAC) && !state.getValue(GLOWING)) {
            if (!level.isClientSide) {
                setBoth(level, pos, state, GLOWING, true);
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    private static <T extends Comparable<T>> void setBoth(Level level, BlockPos pos, BlockState state,
                                                           net.minecraft.world.level.block.state.properties.Property<T> property, T value) {
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState lowerState = level.getBlockState(lower);
        BlockState upperState = level.getBlockState(lower.above());
        if (lowerState.hasProperty(property)) level.setBlock(lower, lowerState.setValue(property, value), 3);
        if (upperState.hasProperty(property)) level.setBlock(lower.above(), upperState.setValue(property, value), 3);
    }
}
