package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BarrelEnvironmentHelper {

    public static int getBiomeHumidity(Level level, BlockPos pos) {

        var biome = level.getBiome(pos).value();

        float temperature = biome.getBaseTemperature();
        var precipitation = biome.getPrecipitationAt(pos);

        int humidity = 0;

        if (level.isRainingAt(pos.above())) {
            humidity += 1;
        }

        // precipitation influence
        if (precipitation == Biome.Precipitation.RAIN) {
            humidity += 2;
        }

        if (precipitation == Biome.Precipitation.SNOW) {
            humidity += 1;
        }

        // hot climates are drier
        if (temperature >= 1.5f) {
            humidity -= 2; // desert/badlands
        }
        else if (temperature >= 0.9f) {
            humidity += 1; // warm humid
        }

        return humidity;
    }

    public static int getBiomeWarmth(Level level, BlockPos pos) {

        float temp = level.getBiome(pos).value().getBaseTemperature();

        if (temp >= 1.5f) return 2;   // desert/badlands
        if (temp >= 0.9f) return 1;   // plains/jungle
        if (temp >= 0.4f) return 0;   // forest

        return -1; // cold biomes
    }

    public static int getNearbyWaterBonus(Level level, BlockPos pos) {
        int waterCount = 0;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos check = pos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(check);

                    if (state.is(Blocks.WATER)) {
                        waterCount++;
                    }
                    else if (state.is(Blocks.WATER_CAULDRON)) {
                        waterCount += 2;
                    }
                }
            }
        }

        if (waterCount >= 8) return 5;
        if (waterCount >= 4) return 4;
        if (waterCount >= 2) return 3;
        if (waterCount >= 1) return 2;
        return 0;
    }

    public static int getSunlightWarmthBonus(Level level, BlockPos pos) {
        if (level.isDay()
                && level.canSeeSky(pos.above())
                && level.getBrightness(LightLayer.SKY, pos.above()) >= 14) {
            return 2;
        }

        if (level.canSeeSky(pos.above())
                && level.getBrightness(LightLayer.SKY, pos.above()) >= 10) {
            return 1;
        }

        return 0;
    }

    private static int getNearbyFireboxHeatBonus(Level level, BlockPos pos) {
        int heat = 0;

        for (BlockPos check : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {

            BlockState state = level.getBlockState(check);

            if (state.is(ModBlocks.FLUE_FIREBOX.get())
                    && state.hasProperty(BlockStateProperties.LIT)
                    && state.getValue(BlockStateProperties.LIT)) {
                heat += 3;
            }
            else if ((state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE))
                    && state.hasProperty(BlockStateProperties.LIT)
                    && state.getValue(BlockStateProperties.LIT)) {
                heat += 1;
            }
        }

        return heat;
    }

    public static int getNearbyColdBonus(Level level, BlockPos pos) {
        int cold = 0;

        for (BlockPos check : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
            BlockState state = level.getBlockState(check);

            if (state.is(Blocks.BLUE_ICE)) {
                cold += 3;
            }
            else if (state.is(Blocks.PACKED_ICE)) {
                cold += 2;
            }
            else if (state.is(Blocks.ICE) || state.is(Blocks.SNOW_BLOCK)) {
                cold += 1;
            }
        }

        return cold;
    }

    public static boolean isCoolDarkStorage(Level level, BlockPos pos) {
        boolean noSky = !level.canSeeSky(pos.above());
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos.above());
        return noSky && blockLight <= 7;
    }

    public static int getHumidity(Level level, BlockPos pos) {
        int humidity = 0;
        humidity += getBiomeHumidity(level, pos);
        humidity += getNearbyWaterBonus(level, pos);

        if (isCoolDarkStorage(level, pos)) {
            humidity += 1;
        }

        return humidity;
    }

    public static int getWarmth(Level level, BlockPos pos) {
        int warmth = 0;
        warmth += getBiomeWarmth(level, pos);
        warmth += getSunlightWarmthBonus(level, pos);
        warmth += getNearbyFireboxHeatBonus(level, pos);
        warmth -= Math.min(2, getNearbyColdBonus(level, pos));

        if (isCoolDarkStorage(level, pos)) {
            warmth -= 1;
        }

        return warmth;
    }
}