package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.IPlantable;

public class BurleyCropBlock extends CropBlock {

    public static final int FIRST_STAGE_MAX_AGE = 3;
    public static final int SECOND_STAGE_MAX_AGE = 4;

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);

    public BurleyCropBlock(Properties properties) {
        super(properties);
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pLevel.isAreaLoaded(pPos, 1)) return;
        if (pLevel.getRawBrightness(pPos, 0) >= 9) {
            int currentAge = this.getCurrentAge(pLevel, pPos, pState);

            int nextAge = currentAge + this.getBonemealAgeIncrease(pLevel);
            int maxAge = this.getMaxAge();

            if(nextAge > maxAge) {
                nextAge = maxAge;
            }

            if(nextAge > FIRST_STAGE_MAX_AGE) {
                if(pLevel.getBlockState(pPos.below(1)).is(this)){
                    pLevel.setBlock(pPos.below(1), this.getStateForAge(FIRST_STAGE_MAX_AGE), 2);
                    pLevel.setBlock(pPos, this.getStateForAge(nextAge), 2);
                } else{
                    pLevel.setBlock(pPos.above(1), this.getStateForAge(nextAge), 2);
                    pLevel.setBlock(pPos, this.getStateForAge(FIRST_STAGE_MAX_AGE), 2);
                }
            }
            else {
                pLevel.setBlock(pPos, this.getStateForAge(nextAge), 2);
            }
        }
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        return super.mayPlaceOn(state, world, pos);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return super.canSurvive(pState, pLevel, pPos) || (pLevel.getBlockState(pPos.below(1)).is(this));
    }

    @Override
    public void growCrops(Level pLevel, BlockPos pPos, BlockState pState) {
        int currentAge = this.getCurrentAge(pLevel, pPos, pState);

        int nextAge = currentAge + this.getBonemealAgeIncrease(pLevel);
        int maxAge = this.getMaxAge();

        if(nextAge > maxAge) {
            nextAge = maxAge;
        }

        if(nextAge > FIRST_STAGE_MAX_AGE) {
            if(pLevel.getBlockState(pPos.below(1)).is(this)){
                pLevel.setBlock(pPos.below(1), this.getStateForAge(FIRST_STAGE_MAX_AGE), 2);
                pLevel.setBlock(pPos, this.getStateForAge(nextAge), 2);
            } else{
                pLevel.setBlock(pPos.above(1), this.getStateForAge(nextAge), 2);
                pLevel.setBlock(pPos, this.getStateForAge(FIRST_STAGE_MAX_AGE), 2);
            }
        }
        else {
            pLevel.setBlock(pPos, this.getStateForAge(nextAge), 2);
        }
    }

    @Override
    public int getMaxAge() {
        return FIRST_STAGE_MAX_AGE + SECOND_STAGE_MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.BURLEY_TOBACCO_SEEDS.get();
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE);
    }

    protected int getCurrentAge(Level pLevel, BlockPos pPos, BlockState pState){
        if(pLevel.getBlockState(pPos.above(1)).is(this)){
            return this.getAge(pState)+this.getAge(pLevel.getBlockState(pPos.above(1)));
        } else if(pLevel.getBlockState(pPos.below(1)).is(this)){
            return this.getAge(pState)+this.getAge(pLevel.getBlockState(pPos.below(1)));
        } else{
            return this.getAge(pState);
        }
    }
}
