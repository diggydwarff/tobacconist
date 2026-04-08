package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TobaccoCropDebugHelper {

    public record CropDebugInfo(
            int age,
            int totalScore,
            String status,
            int biomeScore,
            int tempScore,
            int lightScore,
            int rainScore,
            boolean canSeeSky,
            int skyLight,
            boolean raining
    ) {}

    public static CropDebugInfo getDebugInfo(Level level, BlockPos pos, BlockState state) {
        int age = state.getValue(AbstractTallTobaccoCropBlock.AGE);

        int skyLight = level.getBrightness(LightLayer.SKY, pos.above());
        boolean canSeeSky = level.canSeeSky(pos.above());
        boolean raining = level.isRainingAt(pos.above());

        int lightScore;
        if (skyLight >= 14) {
            lightScore = 3;
        } else if (skyLight >= 10) {
            lightScore = 1;
        } else {
            lightScore = -2;
        }

        int rainScore;
        if (raining) {
            rainScore = -2;
        } else {
            rainScore = 1;
        }

        float temp = level.getBiome(pos).value().getBaseTemperature();

        int tempScore;
        if (temp >= 0.8f && temp <= 1.2f) {
            tempScore = 3; // ideal warm
        } else if (temp >= 0.6f && temp <= 1.4f) {
            tempScore = 1;
        } else {
            tempScore = -2;
        }

        int biomeScore = 1;

        int total = biomeScore + tempScore + lightScore + rainScore;

        String status;
        if (total >= 6) {
            status = "Ideal";
        } else if (total >= 2) {
            status = "OK";
        } else {
            status = "Poor";
        }

        return new CropDebugInfo(
                age,
                total,
                status,
                biomeScore,
                tempScore,
                lightScore,
                rainScore,
                canSeeSky,
                skyLight,
                raining
        );
    }

    public static List<Component> getFullDebugLines(Level level, BlockPos pos, BlockState state) {
        CropDebugInfo info = getDebugInfo(level, pos, state);

        return List.of(
                Component.literal("=== Tobacco Crop Debug ===").withStyle(ChatFormatting.GOLD),

                Component.literal("Stage: " + info.age()),
                Component.literal("Score: " + info.totalScore() + " (" + info.status() + ")"),

                Component.literal("Biome: " + info.biomeScore() + " "
                        + explain(info.biomeScore(), "bad biome", "ok biome", "good biome")),

                Component.literal("Temperature: " + info.tempScore() + " "
                        + explain(info.tempScore(), "too cold/hot", "acceptable", "good")),

                Component.literal("Light: " + info.lightScore() + " "
                        + explain(info.lightScore(), "too dark", "ok light", "strong light")),

                Component.literal("Rain: " + info.rainScore() + " "
                        + explain(info.rainScore(), "too wet", "ok", "dry/good")),

                Component.literal("Can See Sky: " + info.canSeeSky()),
                Component.literal("Sky Light: " + info.skyLight()),
                Component.literal("Raining: " + info.raining())
        );
    }

    public static String getActionBarLine(Level level, BlockPos pos, BlockState state) {
        CropDebugInfo info = getDebugInfo(level, pos, state);

        return "[Grow] "
                + info.status()
                + " | S:" + info.totalScore()
                + " | L:" + info.lightScore()
                + " T:" + info.tempScore()
                + " R:" + info.rainScore();
    }

    private static String explain(int score, String bad, String ok, String good) {
        if (score >= 3) return "(" + good + ")";
        if (score >= 0) return "(" + ok + ")";
        return "(" + bad + ")";
    }
}