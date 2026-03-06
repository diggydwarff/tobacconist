package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.IPlantable;

public class OrientalCropBlock extends CropBlock {

    public static final int FIRST_STAGE_MAX_AGE = 3;
    public static final int SECOND_STAGE_MAX_AGE = 4;

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);

    public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.create("half", DoubleBlockHalf.class);

    public OrientalCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pLevel.isAreaLoaded(pPos, 1)) return;
        if (pLevel.getRawBrightness(pPos, 0) >= 9) {
            int currentAge = getEffectiveAge(pLevel, pPos, pState);

            float speed = getGrowthSpeed(this, pLevel, pPos);
            if (pRandom.nextInt((int)(25.0F / speed) + 1) != 0) return;

            int nextAge = currentAge + 1;
            int maxAge = this.getMaxAge();

            if(nextAge > maxAge) {
                nextAge = maxAge;
            }

            if (nextAge > FIRST_STAGE_MAX_AGE) {
                BlockPos lowerPos = (pState.getValue(HALF) == DoubleBlockHalf.UPPER) ? pPos.below() : pPos;
                BlockPos upperPos = lowerPos.above();

                // keep lower at 0..3 only
                pLevel.setBlock(lowerPos, lower(FIRST_STAGE_MAX_AGE), 2);

                // upper carries 4..7
                pLevel.setBlock(upperPos, upper(nextAge), 2);
                return;
            }
            else {
                // stage 1: only the LOWER block exists/updates
                if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) return;
                pLevel.setBlock(pPos, lower(nextAge), 2);
            }
        }
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        return super.mayPlaceOn(state, world, pos);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Only farmland (typical crop behavior)
        return state.is(Blocks.FARMLAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }

        // LOWER: normal crop rules (uses your mayPlaceOn -> FARMLAND)
        return super.canSurvive(state, level, pos);
    }

    private int getEffectiveAge(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return getAge(state);
        }
        BlockState above = level.getBlockState(pos.above());
        if (above.is(this) && above.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return getAge(above); // 4..7
        }
        return getAge(state); // 0..3
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide) {
            super.playerWillDestroy(level, pos, state, player);
            return;
        }

        DoubleBlockHalf half = state.getValue(HALF);

        // ---- BREAKING UPPER ----
        if (half == DoubleBlockHalf.UPPER) {
            if (!player.isCreative()) {
                int leaves = 2 + level.random.nextInt(3); // 2-4
                int seeds  = 1 + level.random.nextInt(2); // 1-2

                popResource(level, pos, new ItemStack(ModItems.ORIENTAL_TOBACCO_LEAF.get(), leaves));
                popResource(level, pos, new ItemStack(ModItems.ORIENTAL_TOBACCO_SEEDS.get(), seeds));
            }

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }

        // ---- BREAKING LOWER ----
        BlockPos upperPos = pos.above();
        BlockState upperState = level.getBlockState(upperPos);

        if (upperState.is(this) && upperState.getValue(HALF) == DoubleBlockHalf.UPPER) {

            // If mature (upper exists), drop top harvest loot too
            if (!player.isCreative()) {
                int leaves = 2 + level.random.nextInt(3); // 2-4
                int seeds  = 1 + level.random.nextInt(2); // 1-2

                popResource(level, upperPos, new ItemStack(ModItems.ORIENTAL_TOBACCO_LEAF.get(), leaves));
                popResource(level, upperPos, new ItemStack(ModItems.ORIENTAL_TOBACCO_SEEDS.get(), seeds));
            }

            level.setBlock(upperPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        // only allow bonemeal on LOWER
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return false;

        // stop bonemeal when fully mature (upper at 7, or lower at 3 if no upper)
        return getEffectiveAge(level, pos, state) < getMaxAge();
    }


    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return false;
        return getEffectiveAge(level, pos, state) < getMaxAge();
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // if someone somehow bonemeals upper (mods/commands), redirect to lower
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos lowerPos = pos.below();
            BlockState lowerState = level.getBlockState(lowerPos);
            if (lowerState.is(this) && lowerState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                super.performBonemeal(level, random, lowerPos, lowerState);
            }
            return;
        }

        if (getEffectiveAge(level, pos, state) >= getMaxAge()) return;

        super.performBonemeal(level, random, pos, state);
    }


    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return; // ignore upper

        int currentAge = getEffectiveAge(level, pos, state);
        if (currentAge >= getMaxAge()) return;

        int nextAge = Math.min(getMaxAge(), currentAge + getBonemealAgeIncrease(level));

        if (nextAge > FIRST_STAGE_MAX_AGE) {
            BlockPos lowerPos = pos;
            BlockPos upperPos = pos.above();
            level.setBlock(lowerPos, lower(FIRST_STAGE_MAX_AGE), 2);
            level.setBlock(upperPos, upper(nextAge), 2);
        } else {
            level.setBlock(pos, lower(nextAge), 2);
        }
    }

    @Override
    public int getMaxAge() {
        return FIRST_STAGE_MAX_AGE + SECOND_STAGE_MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.ORIENTAL_TOBACCO_SEEDS.get();
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    protected int getCurrentAge(Level level, BlockPos pos, BlockState state) {
        return getAge(state);
    }

    private BlockState lower(int age) { return getStateForAge(age).setValue(HALF, DoubleBlockHalf.LOWER); }
    private BlockState upper(int age) { return getStateForAge(age).setValue(HALF, DoubleBlockHalf.UPPER); }

}