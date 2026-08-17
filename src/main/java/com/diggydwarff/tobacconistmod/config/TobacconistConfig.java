package com.diggydwarff.tobacconistmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TobacconistConfig {

    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        Pair<Client, ModConfigSpec> clientPair =
                new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();

        Pair<Common, ModConfigSpec> commonPair =
                new ModConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<Server, ModConfigSpec> serverPair =
                new ModConfigSpec.Builder().configure(Server::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();
    }

    public static boolean isQualitySystemEnabled() {
        try {
            return SERVER.enableQualitySystem.get();
        } catch (IllegalStateException ignored) {
            // Server config may not be loaded yet during early client bootstrap.
            return true;
        }
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

    public static class Server {
        public final ModConfigSpec.BooleanValue enableQualitySystem;

        public Server(ModConfigSpec.Builder builder) {
            builder.push("gameplay");
            enableQualitySystem = builder
                    .comment("Enable the tobacco quality system: quality scores/tiers and quality-based smoking bonuses.")
                    .comment("When false, quality is hidden and ignored for gameplay, but existing quality data is preserved so it can be turned back on later.")
                    .define("enableQualitySystem", true);
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