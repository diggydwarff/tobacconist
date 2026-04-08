package com.diggydwarff.tobacconistmod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TobacconistConfig {

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        Pair<Client, ForgeConfigSpec> clientPair =
                new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();

        Pair<Common, ForgeConfigSpec> commonPair =
                new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();
    }

    public static class Client {
        public final ForgeConfigSpec.IntValue particleDensity;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("curios");
            particleDensity = builder
                    .comment("Chance divisor for Curios mouth smoke. Lower = more smoke.")
                    .defineInRange("particleDensity", 6, 1, 24);
            builder.pop();
        }
    }

    public static class Common {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> additionalEffects;

        public Common(ForgeConfigSpec.Builder builder) {
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