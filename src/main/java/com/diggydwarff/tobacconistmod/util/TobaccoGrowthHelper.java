package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);
        int clamped = Math.max(0, Math.min(70, quality));

        tag.putInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY, clamped);

        // Raw leaf should not carry final cured product fields.
        tag.remove(TobaccoCuringHelper.TAG_QUALITY);
        tag.remove(TobaccoCuringHelper.TAG_QUALITY_TIER);
        tag.remove(TobaccoCuringHelper.TAG_CURE_TYPE);
    }

    public static int calculateGrowthQuality(Level level, BlockPos pos, Variety variety, int age, int maxAge) {
        int base = calculateGrowthPotential(level, pos, variety, age, maxAge);
        int rng = level.getRandom().nextInt(11); // 0–10
        int quality = base + rng;
        return Math.max(0, Math.min(70, quality));
    }

    /** Scores biome, light, and temperature without including crop maturity. */
    public static int calculateEnvironmentConditionScore(Level level, BlockPos pos, Variety variety) {
        int biome = biomeScore(level, pos, variety);
        int light = lightScore(level, pos, variety);
        int temp = temperatureScore(level, pos, variety);

        return (biome / 2) + ((light * 3) / 5) + ((temp * 3) / 5);
    }

    /** Returns deterministic growth potential before the 0-10 harvest roll. */
    public static int calculateGrowthPotential(Level level, BlockPos pos, Variety variety, int age, int maxAge) {
        int biome = biomeScore(level, pos, variety);
        int light = lightScore(level, pos, variety);
        int temp = temperatureScore(level, pos, variety);
        int harvest = harvestTimingScore(age, maxAge);

        int base =
                28
                        + (biome / 2)
                        + ((light * 3) / 5)
                        + ((temp * 3) / 5)
                        + ((harvest * 3) / 4);

        return Math.max(0, Math.min(70, base));
    }

    public static Component getInspectionMessage(Level level, BlockPos pos, Variety variety, int effectiveAge, int maxAge) {
        int biome = biomeScore(level, pos, variety);
        int light = lightScore(level, pos, variety);
        int temp = temperatureScore(level, pos, variety);
        int harvest = harvestTimingScore(effectiveAge, maxAge);

        int total =
                28
                        + (biome / 2)
                        + ((light * 3) / 5)
                        + ((temp * 3) / 5)
                        + ((harvest * 3) / 4);

        total = Math.max(0, Math.min(70, total));

        Factor bestFactor = getBestFactor(biome, light, temp, harvest);
        Factor worstFactor = getWorstFactor(biome, light, temp, harvest);

        if (total >= 58) {
            return strongPositiveMessage(level, variety, bestFactor, effectiveAge, maxAge);
        } else if (total >= 46) {
            return mildPositiveMessage(level, variety, bestFactor, effectiveAge, maxAge);
        } else if (total >= 32) {
            return neutralMessage(level, variety, bestFactor, worstFactor, effectiveAge, maxAge);
        } else if (total >= 18) {
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

    private static Component strongPositiveMessage(Level level, Variety variety, Factor factor, int age, int maxAge) {
        String[] pool = switch (factor) {
            case BIOME -> switch (variety) {
                case VIRGINIA -> new String[]{
                        "tobacconistmod.growth.inspect.001",
                        "tobacconistmod.growth.inspect.002"
                };
                case BURLEY -> new String[]{
                        "tobacconistmod.growth.inspect.003",
                        "tobacconistmod.growth.inspect.004"
                };
                case ORIENTAL -> new String[]{
                        "tobacconistmod.growth.inspect.005",
                        "tobacconistmod.growth.inspect.006"
                };
                case DOKHA -> new String[]{
                        "tobacconistmod.growth.inspect.007",
                        "tobacconistmod.growth.inspect.008"
                };
                case SHADE -> new String[]{
                        "tobacconistmod.growth.inspect.009",
                        "tobacconistmod.growth.inspect.010"
                };
                case WILD -> new String[]{
                        "tobacconistmod.growth.inspect.011",
                        "tobacconistmod.growth.inspect.012"
                };
            };
            case LIGHT -> switch (variety) {
                case SHADE -> new String[]{
                        "tobacconistmod.growth.inspect.013",
                        "tobacconistmod.growth.inspect.014"
                };
                case ORIENTAL, DOKHA -> new String[]{
                        "tobacconistmod.growth.inspect.015",
                        "tobacconistmod.growth.inspect.016"
                };
                default -> new String[]{
                        "tobacconistmod.growth.inspect.017",
                        "tobacconistmod.growth.inspect.018"
                };
            };
            case TEMPERATURE -> switch (variety) {
                case ORIENTAL -> new String[]{
                        "tobacconistmod.growth.inspect.019",
                        "tobacconistmod.growth.inspect.020"
                };
                case DOKHA -> new String[]{
                        "tobacconistmod.growth.inspect.021",
                        "tobacconistmod.growth.inspect.022"
                };
                case SHADE -> new String[]{
                        "tobacconistmod.growth.inspect.023",
                        "tobacconistmod.growth.inspect.024"
                };
                default -> new String[]{
                        "tobacconistmod.growth.inspect.025",
                        "tobacconistmod.growth.inspect.026"
                };
            };
            case HARVEST -> new String[]{
                    "tobacconistmod.growth.inspect.027",
                    "tobacconistmod.growth.inspect.028"
            };
        };

        return Component.translatable(pool[level.random.nextInt(pool.length)]);
    }

    private static Component mildPositiveMessage(Level level, Variety variety, Factor factor, int age, int maxAge) {
        String[] pool = switch (factor) {
            case BIOME -> new String[]{
                    "tobacconistmod.growth.inspect.029",
                    "tobacconistmod.growth.inspect.030"
            };
            case LIGHT -> new String[]{
                    "tobacconistmod.growth.inspect.031",
                    "tobacconistmod.growth.inspect.032"
            };
            case TEMPERATURE -> new String[]{
                    "tobacconistmod.growth.inspect.033",
                    "tobacconistmod.growth.inspect.034"
            };
            case HARVEST -> new String[]{
                    "tobacconistmod.growth.inspect.035",
                    "tobacconistmod.growth.inspect.036"
            };
        };

        return Component.translatable(pool[level.random.nextInt(pool.length)]);
    }

    private static Component neutralMessage(Level level, Variety variety, Factor best, Factor worst, int age, int maxAge) {
        if (worst == Factor.HARVEST && age < maxAge) {
            String[] pool = {
                    "tobacconistmod.growth.inspect.037",
                    "tobacconistmod.growth.inspect.038"
            };
            return Component.translatable(pool[level.random.nextInt(pool.length)]);
        }

        String[] pool = switch (worst) {
            case BIOME -> new String[]{
                    "tobacconistmod.growth.inspect.039",
                    "tobacconistmod.growth.inspect.040"
            };
            case LIGHT -> new String[]{
                    "tobacconistmod.growth.inspect.041",
                    "tobacconistmod.growth.inspect.042"
            };
            case TEMPERATURE -> new String[]{
                    "tobacconistmod.growth.inspect.043",
                    "tobacconistmod.growth.inspect.044"
            };
            case HARVEST -> new String[]{
                    "tobacconistmod.growth.inspect.045",
                    "tobacconistmod.growth.inspect.046"
            };
        };

        return Component.translatable(pool[level.random.nextInt(pool.length)]);
    }

    private static Component mildNegativeMessage(Level level, Variety variety, Factor factor, int age, int maxAge) {
        String[] pool = switch (factor) {
            case BIOME -> new String[]{
                    "tobacconistmod.growth.inspect.047",
                    "tobacconistmod.growth.inspect.048"
            };
            case LIGHT -> new String[]{
                    "tobacconistmod.growth.inspect.049",
                    "tobacconistmod.growth.inspect.050"
            };
            case TEMPERATURE -> new String[]{
                    "tobacconistmod.growth.inspect.051",
                    "tobacconistmod.growth.inspect.052"
            };
            case HARVEST -> new String[]{
                    "tobacconistmod.growth.inspect.053",
                    "tobacconistmod.growth.inspect.054"
            };
        };

        return Component.translatable(pool[level.random.nextInt(pool.length)]);
    }

    private static Component strongNegativeMessage(Level level, Variety variety, Factor factor, int age, int maxAge) {
        String[] pool = switch (factor) {
            case BIOME -> new String[]{
                    "tobacconistmod.growth.inspect.055",
                    "tobacconistmod.growth.inspect.056"
            };
            case LIGHT -> new String[]{
                    "tobacconistmod.growth.inspect.057",
                    "tobacconistmod.growth.inspect.058"
            };
            case TEMPERATURE -> new String[]{
                    "tobacconistmod.growth.inspect.059",
                    "tobacconistmod.growth.inspect.060"
            };
            case HARVEST -> new String[]{
                    "tobacconistmod.growth.inspect.061",
                    "tobacconistmod.growth.inspect.062"
            };
        };

        return Component.translatable(pool[level.random.nextInt(pool.length)]);
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
        if (age >= maxAge) return 8;
        if (age >= maxAge - 1) return -3;
        return -12;
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
        if (score >= 8) return "Ready";
        if (score >= -3) return "Nearly Ready";
        return "Too Early";
    }
}