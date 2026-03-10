package com.diggydwarff.tobacconistmod.block;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.IPlantable;

public abstract class AbstractTallTobaccoCropBlock extends CropBlock {

    public static final int FIRST_STAGE_MAX_AGE = 3;
    public static final int SECOND_STAGE_MAX_AGE = 4;

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
    public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.create("half", DoubleBlockHalf.class);

    protected AbstractTallTobaccoCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    protected abstract TobaccoGrowthHelper.Variety getVariety();

    protected abstract Item getLeafItem();

    @Override
    protected abstract ItemLike getBaseSeedId();

    protected int getLeafDropCount(Level level) {
        return 2 + level.random.nextInt(3); // 2-4
    }

    protected int getSeedDropCount(Level level) {
        return 1 + level.random.nextInt(2); // 1-2
    }

    protected String getDisplayName() {
        String raw = getVariety().name().toLowerCase();
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1) + " Tobacco";
    }

    protected String formatTierName(String tier) {
        return switch (tier) {
            case "poor" -> "Poor";
            case "common" -> "Common";
            case "good" -> "Good";
            case "excellent" -> "Excellent";
            case "perfect" -> "Perfect";
            default -> "Unknown";
        };
    }

    protected BlockState lower(int age) {
        return getStateForAge(age).setValue(HALF, DoubleBlockHalf.LOWER);
    }

    protected BlockState upper(int age) {
        return getStateForAge(age).setValue(HALF, DoubleBlockHalf.UPPER);
    }

    protected int getEffectiveAge(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return getAge(state);
        }

        BlockState above = level.getBlockState(pos.above());
        if (above.is(this) && above.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return getAge(above); // 4..7
        }

        return getAge(state); // 0..3
    }

    protected ItemStack makeLeafStackWithQuality(Level level, BlockPos basePos, int count) {
        BlockState baseState = level.getBlockState(basePos);

        int effectiveAge = getEffectiveAge(level, basePos, baseState);
        int quality = TobaccoGrowthHelper.calculateGrowthQuality(
                level,
                basePos,
                getVariety(),
                effectiveAge,
                getMaxAge()
        );

        ItemStack stack = new ItemStack(getLeafItem(), count);
        TobaccoGrowthHelper.applyGrowthQuality(stack, quality);
        return stack;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;
        if (level.getRawBrightness(pos, 0) < 9) return;

        int currentAge = getEffectiveAge(level, pos, state);
        float speed = getGrowthSpeed(this, level, pos);

        if (random.nextInt((int) (25.0F / speed) + 1) != 0) return;

        int nextAge = Math.min(getMaxAge(), currentAge + 1);

        if (nextAge > FIRST_STAGE_MAX_AGE) {
            BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            BlockPos upperPos = lowerPos.above();

            level.setBlock(lowerPos, lower(FIRST_STAGE_MAX_AGE), 2);
            level.setBlock(upperPos, upper(nextAge), 2);
            return;
        }

        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return;

        level.setBlock(pos, lower(nextAge), 2);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        return super.mayPlaceOn(state, world, pos);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }

        return super.canSurvive(state, level, pos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide) {
            super.playerWillDestroy(level, pos, state, player);
            return;
        }

        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.UPPER) {
            if (!player.isCreative()) {
                BlockPos basePos = pos.below();
                int leaves = getLeafDropCount(level);
                int seeds = getSeedDropCount(level);

                popResource(level, pos, makeLeafStackWithQuality(level, basePos, leaves));
                popResource(level, pos, new ItemStack(getBaseSeedId(), seeds));
            }

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }

        BlockPos upperPos = pos.above();
        BlockState upperState = level.getBlockState(upperPos);

        if (upperState.is(this) && upperState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            if (!player.isCreative()) {
                int leaves = getLeafDropCount(level);
                int seeds = getSeedDropCount(level);

                popResource(level, upperPos, makeLeafStackWithQuality(level, pos, leaves));
                popResource(level, upperPos, new ItemStack(getBaseSeedId(), seeds));
            }

            level.setBlock(upperPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return false;
        return getEffectiveAge(level, pos, state) < getMaxAge();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return false;
        return getEffectiveAge(level, pos, state) < getMaxAge();
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
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
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return;

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
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return super.use(state, level, pos, player, hand, hit);
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos inspectPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState inspectState = level.getBlockState(inspectPos);

        int effectiveAge = getEffectiveAge(level, inspectPos, inspectState);

        String message = TobaccoGrowthHelper.getInspectionMessage(
                level,
                inspectPos,
                getVariety(),
                effectiveAge,
                getMaxAge()
        );

        player.displayClientMessage(Component.literal(message), true);
        return InteractionResult.CONSUME;
    }
}