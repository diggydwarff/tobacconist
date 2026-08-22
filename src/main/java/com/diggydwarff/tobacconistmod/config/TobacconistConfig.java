package com.diggydwarff.tobacconistmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import com.diggydwarff.tobacconistmod.util.TobaccoAromaticHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TobacconistConfig {

    public static final List<String> DEFAULT_SECRET_BLENDS = List.of(
            "Notch's Blend=burley|85|air|none+shade|90|air|none+virginia|90|flue|none",
            "Ender Reserve=dokha|90|fire|none+oriental|90|sun|none+shade|90|air|chorus_fruit",
            "Redstone Reserve=burley|85|fire|none+oriental|85|sun|none+virginia|90|flue|none",
            "Dragon's Hoard=dokha|95|fire|goldenapple+oriental|90|sun|none+wild|90|fire|none"
    );

    private static final List<String> LEGACY_DEFAULT_SECRET_BLENDS = List.of(
            "Notch's Blend=burley+shade+virginia",
            "Ender Reserve=dokha+oriental+shade",
            "Redstone Reserve=burley+oriental+virginia",
            "Dragon's Hoard=dokha+oriental+wild"
    );

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

    public static boolean areNicotineEffectsEnabled() {
        try {
            return SERVER.enableNicotineEffects.get();
        } catch (IllegalStateException ignored) {
            return true;
        }
    }

    /**
     * Resolves a hidden blend from per-component requirements.
     *
     * <p>Preferred component format: variety|minQuality|cure|flavor. Components are joined with
     * '+'. Cure or flavor may be '*' to accept anything. Flavor 'none' requires unflavored
     * tobacco. Legacy variety-only entries are still accepted as wildcards for migration.</p>
     */
    public static String findSecretBlendName(List<TobaccoBlendComponent> components) {
        if (components == null || components.size() < 2 || components.size() > 3) return "";

        List<? extends String> configured;
        try {
            configured = SERVER.secretBlends.get();
            // Worlds that generated the first R20 defaults before this richer format existed
            // transparently use the stricter defaults unless the server owner customized them.
            if (configured.equals(LEGACY_DEFAULT_SECRET_BLENDS)) {
                configured = DEFAULT_SECRET_BLENDS;
            }
        } catch (IllegalStateException ignored) {
            configured = DEFAULT_SECRET_BLENDS;
        }

        for (String entry : configured) {
            SecretBlendDefinition definition = parseSecretBlend(entry);
            if (definition == null || definition.requirements().size() != components.size()) continue;
            if (matchesSecretBlend(definition.requirements(), components)) {
                return definition.name();
            }
        }
        return "";
    }

    private static boolean matchesSecretBlend(
            List<SecretBlendRequirement> requirements,
            List<TobaccoBlendComponent> components
    ) {
        List<TobaccoBlendComponent> remaining = new ArrayList<>(components);

        for (SecretBlendRequirement requirement : requirements) {
            int matchedIndex = -1;
            for (int i = 0; i < remaining.size(); i++) {
                if (requirement.matches(remaining.get(i))) {
                    matchedIndex = i;
                    break;
                }
            }
            if (matchedIndex < 0) return false;
            remaining.remove(matchedIndex);
        }
        return remaining.isEmpty();
    }

    private static SecretBlendDefinition parseSecretBlend(String entry) {
        if (entry == null) return null;
        int equals = entry.indexOf('=');
        if (equals <= 0 || equals >= entry.length() - 1) return null;

        String name = entry.substring(0, equals).trim();
        String recipe = entry.substring(equals + 1).trim();
        if (name.isEmpty() || recipe.isEmpty()) return null;

        List<SecretBlendRequirement> requirements = new ArrayList<>();
        for (String rawComponent : recipe.split("\\+")) {
            SecretBlendRequirement requirement = parseRequirement(rawComponent);
            if (requirement == null) return null;
            requirements.add(requirement);
        }

        if (requirements.size() < 2 || requirements.size() > 3) return null;
        return new SecretBlendDefinition(name, requirements);
    }

    private static SecretBlendRequirement parseRequirement(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] fields = raw.trim().split("\\|", -1);

        String variety = normalizeToken(fields[0]);
        if (variety.isEmpty()) return null;

        // Legacy variety-only entries remain valid and simply ignore quality/cure/flavor.
        if (fields.length == 1) {
            return new SecretBlendRequirement(variety, 0, "*", "*");
        }

        int minQuality;
        try {
            minQuality = fields.length > 1 && !fields[1].isBlank()
                    ? Math.max(0, Math.min(120, Integer.parseInt(fields[1].trim())))
                    : 0;
        } catch (NumberFormatException ignored) {
            return null;
        }

        String cure = fields.length > 2 && !fields[2].isBlank() ? normalizeToken(fields[2]) : "*";
        String flavor = fields.length > 3 && !fields[3].isBlank()
                ? normalizeFlavorRequirement(fields[3])
                : "*";

        return new SecretBlendRequirement(variety, minQuality, cure, flavor);
    }

    private static String normalizeToken(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private static String normalizeFlavorRequirement(String value) {
        String normalized = normalizeToken(value);
        if (normalized.equals("*") || normalized.equals("any")) return "*";
        if (normalized.equals("none") || normalized.equals("unflavored")) return "none";
        return TobaccoAromaticHelper.normalizeFlavorId(normalized);
    }

    private record SecretBlendDefinition(String name, List<SecretBlendRequirement> requirements) {}

    private record SecretBlendRequirement(String variety, int minQuality, String cure, String flavor) {
        private boolean matches(TobaccoBlendComponent component) {
            if (!variety.equals(normalizeToken(component.variety()))) return false;
            if (component.quality() < minQuality) return false;

            String actualCure = normalizeToken(component.cure());
            if (!cure.equals("*") && !cure.equals("any") && !cure.equals(actualCure)) return false;

            String actualFlavor = TobaccoAromaticHelper.normalizeFlavorId(component.flavorId());
            if (flavor.equals("*")) return true;
            if (flavor.equals("none")) return actualFlavor.isEmpty();
            return flavor.equals(actualFlavor);
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
        public final ModConfigSpec.BooleanValue enableNicotineEffects;
        public final ModConfigSpec.ConfigValue<List<? extends String>> secretBlends;

        public Server(ModConfigSpec.Builder builder) {
            builder.push("gameplay");
            enableQualitySystem = builder
                    .comment("Enable the tobacco quality system: quality scores/tiers and quality-based smoking bonuses.")
                    .comment("When false, quality is hidden and ignored for gameplay, but existing quality data is preserved so it can be turned back on later.")
                    .define("enableQualitySystem", true);

            enableNicotineEffects = builder
                    .comment("Apply Tobacconist's nicotine status effect when tobacco is smoked.")
                    .comment("Disabling this does not disable configured additional smoking effects.")
                    .define("enableNicotineEffects", true);
            builder.pop();

            builder.push("blending");
            secretBlends = builder
                    .comment("Hidden named tobacco blends with per-variety requirements.")
                    .comment("Format: Display Name=variety|minQuality|cure|flavor+variety|minQuality|cure|flavor[+...]")
                    .comment("Quality is a minimum (0-120). Cure: air, fire, sun, flue, or * for any.")
                    .comment("Flavor: none, *, or a molasses flavor id such as apple, chorus_fruit, goldenapple.")
                    .comment("Order does not matter. Legacy variety-only entries are still accepted as unrestricted matches.")
                    .comment("Valid varieties: wild, virginia, burley, oriental, dokha, shade.")
                    .comment("Servers can rename, remove, or add entries for their own roleplay/lore.")
                    .defineListAllowEmpty(
                            List.of("secretBlends"),
                            DEFAULT_SECRET_BLENDS,
                            obj -> obj instanceof String
                    );
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