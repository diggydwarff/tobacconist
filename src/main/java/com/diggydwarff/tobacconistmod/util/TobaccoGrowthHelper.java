package com.diggydwarff.tobacconistmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;

public final class TobaccoGrowthHelper {

    public enum Variety {
        VIRGINIA,
        BURLEY,
        ORIENTAL,
        DOKHA,
        SHADE,
        WILD
    }

    private enum Factor {
        BIOME,
        LIGHT,
        TEMPERATURE,
        HARVEST
    }

    private TobaccoGrowthHelper() {}

    public static void applyGrowthQuality(ItemStack stack, int quality) {
        CompoundTag tag = stack.getOrCreateTag();
        int clamped = TobaccoCuringHelper.clampQuality(quality);
        tag.putInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY, clamped);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(clamped));
    }

    public static int calculateGrowthQuality(Level level, BlockPos pos, Variety variety, int effectiveAge, int maxAge) {
        int score = 50;

        score += biomeScore(level, pos, variety);
        score += lightScore(level, pos, variety);
        score += temperatureScore(level, pos, variety);
        score += harvestTimingScore(effectiveAge, maxAge);

        return TobaccoCuringHelper.clampQuality(score);
    }

    public static String getInspectionMessage(Level level, BlockPos pos, Variety variety, int effectiveAge, int maxAge) {
        int biome = biomeScore(level, pos, variety);
        int light = lightScore(level, pos, variety);
        int temp = temperatureScore(level, pos, variety);
        int harvest = harvestTimingScore(effectiveAge, maxAge);

        int total = TobaccoCuringHelper.clampQuality(50 + biome + light + temp + harvest);

        Factor bestFactor = getBestFactor(biome, light, temp, harvest);
        Factor worstFactor = getWorstFactor(biome, light, temp, harvest);

        if (total >= 85) {
            return strongPositiveMessage(level, variety, bestFactor, effectiveAge, maxAge);
        } else if (total >= 70) {
            return mildPositiveMessage(level, variety, bestFactor, effectiveAge, maxAge);
        } else if (total >= 55) {
            return neutralMessage(level, variety, bestFactor, worstFactor, effectiveAge, maxAge);
        } else if (total >= 40) {
            return mildNegativeMessage(level, variety, worstFactor, effectiveAge, maxAge);
        } else {
            return strongNegativeMessage(level, variety, worstFactor, effectiveAge, maxAge);
        }
    }

    private static Factor getBestFactor(int biome, int light, int temp, int harvest) {
        Factor best = Factor.BIOME;
        int bestValue = biome;

        if (light > bestValue) {
            best = Factor.LIGHT;
            bestValue = light;
        }
        if (temp > bestValue) {
            best = Factor.TEMPERATURE;
            bestValue = temp;
        }
        if (harvest > bestValue) {
            best = Factor.HARVEST;
        }

        return best;
    }

    private static Factor getWorstFactor(int biome, int light, int temp, int harvest) {
        Factor worst = Factor.BIOME;
        int worstValue = biome;

        if (light < worstValue) {
            worst = Factor.LIGHT;
            worstValue = light;
        }
        if (temp < worstValue) {
            worst = Factor.TEMPERATURE;
            worstValue = temp;
        }
        if (harvest < worstValue) {
            worst = Factor.HARVEST;
        }

        return worst;
    }

    private static String strongPositiveMessage(Level level, Variety variety, Factor factor, int age, int maxAge) {
        String[] pool = switch (factor) {
            case BIOME -> switch (variety) {
                case VIRGINIA -> new String[]{
                        "These plants seem perfectly at home in this open land.",
                        "The broad country seems to suit them beautifully."
                };
                case BURLEY -> new String[]{
                        "They seem deeply settled in this mild ground.",
                        "This land seems to suit them very well."
                };
                case ORIENTAL -> new String[]{
                        "They seem to relish this harsh, dry land.",
                        "These plants look right at home in this barren ground."
                };
                case DOKHA -> new String[]{
                        "The fierce land seems to suit them perfectly.",
                        "They look vigorous in this unforgiving ground."
                };
                case SHADE -> new String[]{
                        "They seem deeply comfortable in this lush place.",
                        "This humid land seems to favor them."
                };
                case WILD -> new String[]{
                        "Wild as they are, they seem to have taken well to this land.",
                        "These plants seem to have settled in naturally."
                };
            };
            case LIGHT -> switch (variety) {
                case SHADE -> new String[]{
                        "The seedlings seem to be thriving in the partial shade.",
                        "The sheltering light seems to suit them well."
                };
                case ORIENTAL, DOKHA -> new String[]{
                        "They seem to drink in the fierce sun.",
                        "The strong light seems to please them."
                };
                default -> new String[]{
                        "The light seems just right for them.",
                        "These leaves look happy in the sun."
                };
            };
            case TEMPERATURE -> switch (variety) {
                case ORIENTAL -> new String[]{
                        "The dry heat seems to please these plants.",
                        "They seem to thrive in the heat."
                };
                case DOKHA -> new String[]{
                        "The intense heat seems to suit them perfectly.",
                        "These plants seem to crave the brutal warmth."
                };
                case SHADE -> new String[]{
                        "The warm, heavy air seems kind to them.",
                        "They seem content in this humid warmth."
                };
                default -> new String[]{
                        "The air seems to suit them very well.",
                        "They seem comfortable in this weather."
                };
            };
            case HARVEST -> new String[]{
                    "The leaves seem full and nearly perfect.",
                    "The plants look ripe and in fine health."
            };
        };

        return pool[level.random.nextInt(pool.length)];
    }

    private static String mildPositiveMessage(Level level, Variety variety, Factor factor, int age, int maxAge) {
        String[] pool = switch (factor) {
            case BIOME -> new String[]{
                    "They seem comfortable in this ground.",
                    "This land seems to agree with them."
            };
            case LIGHT -> new String[]{
                    "The light seems to suit them well enough.",
                    "They seem fairly content with the light here."
            };
            case TEMPERATURE -> new String[]{
                    "The air seems agreeable to them.",
                    "They seem to like the weather well enough."
            };
            case HARVEST -> new String[]{
                    "The leaves seem to be coming along nicely.",
                    "They look healthy and nearly ready."
            };
        };

        return pool[level.random.nextInt(pool.length)];
    }

    private static String neutralMessage(Level level, Variety variety, Factor best, Factor worst, int age, int maxAge) {
        if (worst == Factor.HARVEST && age < maxAge) {
            String[] pool = {
                    "The plants seem healthy enough, but they are not ready yet.",
                    "They seem to be doing well, though the leaves still need time."
            };
            return pool[level.random.nextInt(pool.length)];
        }

        String[] pool = switch (worst) {
            case BIOME -> new String[]{
                    "They seem to be surviving, though this land may not quite suit them.",
                    "The crop looks passable, but the ground does not seem ideal."
            };
            case LIGHT -> new String[]{
                    "They seem to be getting by, though the light feels wrong for them.",
                    "The leaves look serviceable, but they do not seem pleased by the light."
            };
            case TEMPERATURE -> new String[]{
                    "They seem to be enduring the air, if not enjoying it.",
                    "The plants appear stable enough, though the weather does not quite suit them."
            };
            case HARVEST -> new String[]{
                    "The crop seems sound enough, but it still needs time.",
                    "They seem to be doing alright, though not yet ready."
            };
        };

        return pool[level.random.nextInt(pool.length)];
    }

    private static String mildNegativeMessage(Level level, Variety variety, Factor factor, int age, int maxAge) {
        String[] pool = switch (factor) {
            case BIOME -> new String[]{
                    "They seem alive enough, but this land does not suit them.",
                    "The plants seem uneasy in this ground."
            };
            case LIGHT -> new String[]{
                    "They seem to be surviving, but the light is against them.",
                    "These leaves look strained, as if wanting for better light."
            };
            case TEMPERATURE -> new String[]{
                    "The air does not seem to agree with them.",
                    "They seem to be struggling with the weather."
            };
            case HARVEST -> new String[]{
                    "The leaves are too young yet to judge well.",
                    "It is still too early for these plants to show their best."
            };
        };

        return pool[level.random.nextInt(pool.length)];
    }

    private static String strongNegativeMessage(Level level, Variety variety, Factor factor, int age, int maxAge) {
        String[] pool = switch (factor) {
            case BIOME -> new String[]{
                    "These plants do not seem meant for this place.",
                    "The land seems wholly wrong for them."
            };
            case LIGHT -> new String[]{
                    "These leaves look weak, as if starved for proper light.",
                    "The light seems badly wrong for them."
            };
            case TEMPERATURE -> new String[]{
                    "The weather seems cruel to these plants.",
                    "They look deeply unhappy in this air."
            };
            case HARVEST -> new String[]{
                    "These plants are far too young to tell much from yet.",
                    "The leaves have not come close to readiness."
            };
        };

        return pool[level.random.nextInt(pool.length)];
    }

    private static int biomeScore(Level level, BlockPos pos, Variety variety) {
        Holder<Biome> biome = level.getBiome(pos);

        return switch (variety) {
            case VIRGINIA -> {
                if (biome.is(Tags.Biomes.IS_PLAINS) || biome.is(BiomeTags.IS_SAVANNA)) yield 20;
                if (biome.is(BiomeTags.IS_FOREST)) yield 10;
                if (biome.is(Tags.Biomes.IS_DESERT) || biome.is(BiomeTags.IS_BADLANDS)) yield -15;
                yield 0;
            }
            case BURLEY -> {
                if (biome.is(Tags.Biomes.IS_PLAINS) || biome.is(BiomeTags.IS_FOREST)) yield 20;
                if (biome.is(BiomeTags.IS_JUNGLE)) yield 10;
                if (biome.is(Tags.Biomes.IS_DESERT) || biome.is(BiomeTags.IS_BADLANDS)) yield -15;
                yield 0;
            }
            case ORIENTAL -> {
                if (biome.is(Tags.Biomes.IS_DESERT) || biome.is(BiomeTags.IS_BADLANDS)) yield 20;
                if (biome.is(BiomeTags.IS_SAVANNA)) yield 10;
                if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_FOREST)) yield -15;
                yield 0;
            }
            case DOKHA -> {
                if (biome.is(Tags.Biomes.IS_DESERT)) yield 20;
                if (biome.is(BiomeTags.IS_BADLANDS)) yield 10;
                if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_FOREST)) yield -15;
                yield 0;
            }
            case SHADE -> {
                if (biome.is(BiomeTags.IS_JUNGLE)) yield 20;
                if (biome.is(BiomeTags.IS_FOREST)) yield 10;
                if (biome.is(Tags.Biomes.IS_DESERT) || biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.IS_SAVANNA)) yield -15;
                yield 0;
            }
            case WILD -> {
                if (biome.is(Tags.Biomes.IS_DESERT) || biome.is(BiomeTags.IS_BADLANDS)) yield -5;
                yield 10;
            }
        };
    }

    private static int lightScore(Level level, BlockPos pos, Variety variety) {
        int light = level.getMaxLocalRawBrightness(pos.above());

        return switch (variety) {
            case VIRGINIA -> rangeScore(light, 13, 15, 12, 15);
            case BURLEY   -> rangeScore(light, 12, 15, 11, 15);
            case ORIENTAL -> rangeScore(light, 14, 15, 13, 15);
            case DOKHA    -> rangeScore(light, 14, 15, 13, 15);
            case SHADE    -> rangeScore(light, 9, 12, 8, 13);
            case WILD     -> rangeScore(light, 11, 15, 9, 15);
        };
    }

    private static int temperatureScore(Level level, BlockPos pos, Variety variety) {
        float temp = level.getBiome(pos).value().getBaseTemperature();

        return switch (variety) {
            case VIRGINIA -> tempScore(temp, 0.8f, 1.2f, 0.65f, 1.35f);
            case BURLEY   -> tempScore(temp, 0.6f, 0.95f, 0.45f, 1.1f);
            case ORIENTAL -> tempScore(temp, 1.3f, 2.0f, 1.0f, 2.0f);
            case DOKHA    -> tempScore(temp, 1.7f, 2.0f, 1.3f, 2.0f);
            case SHADE    -> tempScore(temp, 0.9f, 1.2f, 0.75f, 1.35f);
            case WILD     -> tempScore(temp, 0.5f, 1.5f, 0.2f, 2.0f);
        };
    }

    private static int harvestTimingScore(int age, int maxAge) {
        if (age >= maxAge) return 12;
        if (age >= maxAge - 1) return -5;
        return -15;
    }

    private static int rangeScore(int value, int idealMin, int idealMax, int acceptableMin, int acceptableMax) {
        if (value >= idealMin && value <= idealMax) return 10;
        if (value >= acceptableMin && value <= acceptableMax) return 4;
        return -12;
    }

    private static int tempScore(float value, float idealMin, float idealMax, float acceptableMin, float acceptableMax) {
        if (value >= idealMin && value <= idealMax) return 10;
        if (value >= acceptableMin && value <= acceptableMax) return 4;
        return -10;
    }

    public static String getBiomeStatus(Level level, BlockPos pos, Variety variety) {
        int score = biomeScore(level, pos, variety);
        return score >= 20 ? "Ideal" : score >= 10 ? "Acceptable" : score >= 0 ? "Neutral" : "Poor";
    }

    public static String getLightStatus(Level level, BlockPos pos, Variety variety) {
        int score = lightScore(level, pos, variety);
        return score >= 10 ? "Ideal" : score >= 4 ? "Acceptable" : "Wrong";
    }

    public static String getTemperatureStatus(Level level, BlockPos pos, Variety variety) {
        int score = temperatureScore(level, pos, variety);
        return score >= 10 ? "Ideal" : score >= 4 ? "Acceptable" : "Poor";
    }

    public static String getHarvestStatus(int age, int maxAge) {
        int score = harvestTimingScore(age, maxAge);
        if (score >= 12) return "Ready";
        if (score >= -5) return "Nearly Ready";
        return "Too Early";
    }
}