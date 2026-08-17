package com.diggydwarff.tobacconistmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TobacconistConfig {

    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    static {
        Pair<Client, ModConfigSpec> clientPair =
                new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();

        Pair<Common, ModConfigSpec> commonPair =
                new ModConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();
    }

    public static class Client {
        public final ModConfigSpec.IntValue particleDensity;

        public Client(ModConfigSpec.Builder builder) {
            builder.push("curios");
            particleDensity = builder
                    .comment("Chance divisor for Curios mouth smoke. Lower = more smoke.")
                    .defineInRange("particleDensity", 6, 1, 24);
            builder.pop();
        }
    }

    public static class Common {
        public final ModConfigSpec.ConfigValue<List<? extends String>> additionalEffects;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("tobacco_effects");

            additionalEffects = builder
                    .comment("Additional effects applied when smoking. Format: effect_id,duration,amplifier")
                    .comment("additionalEffects = [\"minecraft:luck,200,0\", \"minecraft:speed,100,0\"]")
                    .defineListAllowEmpty(
                            List.of("additionalEffects"),
                            List.of(),
                            obj -> obj instanceof String
                    );

            builder.pop();
        }
    }
}