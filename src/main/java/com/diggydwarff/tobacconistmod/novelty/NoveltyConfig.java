package com.diggydwarff.tobacconistmod.novelty;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NoveltyConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue RELIC_LOOT;
    public static final ModConfigSpec.DoubleValue RARE_CHANCE, MYTHIC_CHANCE;
    public static final ModConfigSpec.BooleanValue SOUNDS;
    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("noveltyPipes");
        RELIC_LOOT = builder.comment("Add loot-only relic pipes to their designated structure chests.").define("enableRelicLoot", true);
        RARE_CHANCE = builder.comment("Chance per eligible dungeon, mineshaft or desert chest for its rare pipe.").defineInRange("rarePipeChance", .04, 0, 1);
        MYTHIC_CHANCE = builder.comment("Chance per eligible Nether, End or Ancient City chest for its mythic pipe.").defineInRange("mythicPipeChance", .015, 0, 1);
        SOUNDS = builder.comment("Play quiet novelty-pipe flourish sounds. Normal tobacco sounds are unchanged.").define("enableNoveltySounds", true);
        builder.pop(); SPEC = builder.build();
    }
    private NoveltyConfig() {}
}
