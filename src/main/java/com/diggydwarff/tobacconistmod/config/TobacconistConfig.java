package com.diggydwarff.tobacconistmod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import com.diggydwarff.tobacconistmod.util.TobaccoAromaticHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TobacconistConfig {

    /**
     * Built-in hidden blends. Format:
     * Display Name=variety|minQuality|cure|flavor+...
     *
     * <p>The first ten use only Tobacconist/vanilla flavor profiles (five natural, five aromatic).
     * The final ten deliberately use the generic compatibility flavor profiles, so their ingredients
     * become craftable when another installed food/flavor mod supplies the matching flavoring tag.</p>
     */
    public static final List<String> DEFAULT_SECRET_BLENDS = List.of(
            // Core secrets: natural tobacco
            "The Ninth Bell=virginia|95|flue|none+burley|95|fire|none+shade|90|air|none",
            "Bosphorus No. 7=oriental|100|sun|none+dokha|95|fire|none+virginia|90|flue|none",
            "Black Cabinet=burley|100|fire|none+dokha|100|fire|none+wild|95|air|none",
            "Three Crowns=virginia|100|flue|none+oriental|100|sun|none+shade|100|air|none",
            "Last Harvest=wild|110|sun|none+virginia|105|flue|none+burley|105|air|none",

            // Core secrets: vanilla/Tobacconist aromatics
            "Golden Sultana=oriental|100|sun|honey+virginia|95|flue|none+shade|95|air|none",
            "Orchard Reserve=virginia|100|flue|apple+burley|95|air|none+shade|90|air|honey",
            "Crown & Ember=virginia|105|flue|goldenapple+burley|100|fire|none+oriental|95|sun|none",
            "Lamplighter No. 23=burley|95|fire|cocoa+virginia|95|flue|none+shade|90|air|none",
            "Far End Mixture=dokha|100|fire|chorus_fruit+oriental|95|sun|none+wild|95|air|glowberry",

            // Expanded compatibility secrets
            "Vienna Cabinet=burley|100|air|coffee+virginia|95|flue|vanilla+oriental|90|sun|none",
            "Red Parlor=virginia|100|flue|cherry+burley|95|air|none+shade|95|air|vanilla",
            "Mint Condition=dokha|100|fire|mint+oriental|95|sun|lime+virginia|90|flue|none",
            "The Confectioner=burley|100|air|caramel+virginia|95|flue|vanilla+shade|95|air|custard",
            "Black Orchard=burley|100|fire|blackberry+virginia|95|flue|none+oriental|95|sun|cinnamon",
            "Silk Road No. 12=oriental|105|sun|tea+dokha|100|fire|none+shade|95|air|hibiscus",
            "Blue Hour=shade|100|air|lavender+virginia|95|flue|none+oriental|95|sun|tea",
            "Port Royal=virginia|100|flue|coconut+burley|95|air|caramel+oriental|95|sun|none",
            "Summer Ledger=virginia|100|flue|strawberry+shade|95|air|raspberry+oriental|90|sun|none",
            "The Decadent=burley|105|fire|brownie+virginia|100|flue|custard+shade|100|air|vanilla"
    );

    /**
     * Optional visual overrides for secret blends. Format:
     * Blend Name=#RRGGBB|tintStrength|brightnessLift|saturationBoost
     *
     * <p>Use "auto" for the color to derive a tobacco hue from the stored varieties,
     * cures and flavors. A wildcard name (*) supplies the fallback for every configured or
     * user-created secret blend.</p>
     */
    public static final List<String> DEFAULT_SECRET_BLEND_VISUALS = List.of(
            // Automatic fallback for every server-added/custom secret blend.
            "*=auto|0.58|0.08|0.10",

            // Curated built-in palette. Servers can edit/remove these and fall back to auto at any time.
            "The Ninth Bell=#A87548|0.58|0.07|0.10",
            "Bosphorus No. 7=#B78348|0.59|0.07|0.11",
            "Black Cabinet=#79513D|0.62|0.06|0.08",
            "Three Crowns=#C09455|0.57|0.07|0.10",
            // Legendary natural secret: much more golden and bright.
            "Last Harvest=#F0C24A|0.95|0.14|0.28",
            "Golden Sultana=#D0A84C|0.68|0.10|0.13",
            "Orchard Reserve=#A78A51|0.58|0.08|0.10",
            // Legendary aromatic secret: hotter ember/copper treatment.
            "Crown & Ember=#E35A20|0.96|0.13|0.26",
            "Lamplighter No. 23=#8D6244|0.60|0.06|0.09",
            "Far End Mixture=#896A78|0.56|0.07|0.10",
            "Vienna Cabinet=#8C674C|0.58|0.06|0.09",
            "Red Parlor=#A46561|0.57|0.07|0.11",
            // Legendary compatibility secret: vivid cool mint-green.
            "Mint Condition=#46D971|0.94|0.12|0.26",
            "The Confectioner=#B59168|0.56|0.08|0.10",
            "Black Orchard=#78545F|0.60|0.06|0.10",
            "Silk Road No. 12=#A4784F|0.59|0.07|0.10",
            // Legendary compatibility secret: bold dusky blue-violet.
            "Blue Hour=#5E74F1|0.94|0.11|0.28",
            "Port Royal=#B38B61|0.58|0.08|0.10",
            "Summer Ledger=#AA6E68|0.57|0.08|0.12",
            "The Decadent=#765247|0.62|0.06|0.09"
    );

    /**
     * Optional built-up smoking bonuses for only the rarest secret blends. Format:
     * Blend Name=threshold|effect_id,duration,amplifier;effect_id,duration,amplifier
     */
    public static final List<String> DEFAULT_SECRET_BLEND_BONUSES = List.of(
            "Last Harvest=3|minecraft:absorption,160,0;minecraft:luck,200,0",
            "Crown & Ember=3|minecraft:fire_resistance,180,0;minecraft:strength,120,0",
            "Mint Condition=3|minecraft:speed,180,0;minecraft:jump_boost,140,0",
            "Blue Hour=3|minecraft:night_vision,220,0;minecraft:slow_falling,120,0"
    );

    /** Defaults from the short-lived custom-font secret-blend implementation. */
    private static final List<String> FONTED_DEFAULT_SECRET_BLENDS = List.of(
            "The Ninth Bell@tobacconistmod:secret_condensed=virginia|95|flue|none+burley|95|fire|none+shade|90|air|none",
            "Bosphorus No. 7@tobacconistmod:secret_garamond=oriental|100|sun|none+dokha|95|fire|none+virginia|90|flue|none",
            "Black Cabinet@tobacconistmod:secret_slab=burley|100|fire|none+dokha|100|fire|none+wild|95|air|none",
            "Three Crowns@tobacconistmod:secret_smallcaps=virginia|100|flue|none+oriental|100|sun|none+shade|100|air|none",
            "Last Harvest@tobacconistmod:secret_rustic=wild|110|sun|none+virginia|105|flue|none+burley|105|air|none",
            "Golden Sultana@tobacconistmod:secret_garamond=oriental|100|sun|honey+virginia|95|flue|none+shade|95|air|none",
            "Orchard Reserve@tobacconistmod:secret_rustic=virginia|100|flue|apple+burley|95|air|none+shade|90|air|honey",
            "Crown & Ember@tobacconistmod:secret_smallcaps=virginia|105|flue|goldenapple+burley|100|fire|none+oriental|95|sun|none",
            "Lamplighter No. 23@tobacconistmod:secret_condensed=burley|95|fire|cocoa+virginia|95|flue|none+shade|90|air|none",
            "Far End Mixture@tobacconistmod:secret_slab=dokha|100|fire|chorus_fruit+oriental|95|sun|none+wild|95|air|glowberry",
            "Vienna Cabinet@tobacconistmod:secret_garamond=burley|100|air|coffee+virginia|95|flue|vanilla+oriental|90|sun|none",
            "Red Parlor@tobacconistmod:secret_display=virginia|100|flue|cherry+burley|95|air|none+shade|95|air|vanilla",
            "Mint Condition@tobacconistmod:secret_condensed=dokha|100|fire|mint+oriental|95|sun|lime+virginia|90|flue|none",
            "The Confectioner@tobacconistmod:secret_rustic=burley|100|air|caramel+virginia|95|flue|vanilla+shade|95|air|custard",
            "Black Orchard@tobacconistmod:secret_slab=burley|100|fire|blackberry+virginia|95|flue|none+oriental|95|sun|cinnamon",
            "Silk Road No. 12@tobacconistmod:secret_smallcaps=oriental|105|sun|tea+dokha|100|fire|none+shade|95|air|hibiscus",
            "Blue Hour@tobacconistmod:secret_garamond=shade|100|air|lavender+virginia|95|flue|none+oriental|95|sun|tea",
            "Port Royal@tobacconistmod:secret_display=virginia|100|flue|coconut+burley|95|air|caramel+oriental|95|sun|none",
            "Summer Ledger@tobacconistmod:secret_condensed=virginia|100|flue|strawberry+shade|95|air|raspberry+oriental|90|sun|none",
            "The Decadent@tobacconistmod:secret_smallcaps=burley|105|fire|brownie+virginia|100|flue|custard+shade|100|air|vanilla"
    );

    /** Defaults from the first quality/cure/aromatic secret-blend implementation. */
    private static final List<String> PREVIOUS_DEFAULT_SECRET_BLENDS = List.of(
            "Notch's Blend=burley|85|air|none+shade|90|air|none+virginia|90|flue|none",
            "Ender Reserve=dokha|90|fire|none+oriental|90|sun|none+shade|90|air|chorus_fruit",
            "Redstone Reserve=burley|85|fire|none+oriental|85|sun|none+virginia|90|flue|none",
            "Dragon's Hoard=dokha|95|fire|goldenapple+oriental|90|sun|none+wild|90|fire|none"
    );

    /** Defaults from the original variety-only secret-blend implementation. */
    private static final List<String> LEGACY_DEFAULT_SECRET_BLENDS = List.of(
            "Notch's Blend=burley+shade+virginia",
            "Ender Reserve=dokha+oriental+shade",
            "Redstone Reserve=burley+oriental+virginia",
            "Dragon's Hoard=dokha+oriental+wild"
    );

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        Pair<Client, ForgeConfigSpec> clientPair =
                new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();

        Pair<Common, ForgeConfigSpec> commonPair =
                new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<Server, ForgeConfigSpec> serverPair =
                new ForgeConfigSpec.Builder().configure(Server::new);
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

    public static int getGenericAutomationQualityPenalty() {
        try {
            return SERVER.genericAutomationQualityPenalty.get();
        } catch (IllegalStateException ignored) {
            return 10;
        }
    }

    /**
     * Resolves the complete hidden-blend definition from per-component requirements.
     */
    public static SecretBlendDefinition findSecretBlend(List<TobaccoBlendComponent> components) {
        if (components == null || components.size() < 2 || components.size() > 3) return null;

        for (SecretBlendDefinition definition : getSecretBlendDefinitions()) {
            if (definition.requirements().size() != components.size()) continue;
            if (matchesSecretBlend(definition.requirements(), components)) {
                return definition;
            }
        }
        return null;
    }

    /** Backwards-compatible convenience for callers that only need the intrinsic name. */
    public static String findSecretBlendName(List<TobaccoBlendComponent> components) {
        SecretBlendDefinition definition = findSecretBlend(components);
        return definition == null ? "" : definition.name();
    }

    /** Returns all valid configured secret definitions in config order. */
    public static List<SecretBlendDefinition> getSecretBlendDefinitions() {
        List<SecretBlendDefinition> definitions = new ArrayList<>();
        for (String entry : getConfiguredSecretBlendEntries()) {
            SecretBlendDefinition definition = parseSecretBlend(entry);
            if (definition != null) definitions.add(definition);
        }
        return List.copyOf(definitions);
    }

    /** Case-insensitive lookup used by the admin spawn command. */
    public static SecretBlendDefinition findSecretBlendByName(String blendName) {
        if (blendName == null || blendName.isBlank()) return null;
        String wanted = blendName.trim();
        if (wanted.length() >= 2 && wanted.startsWith("\"") && wanted.endsWith("\"")) {
            wanted = wanted.substring(1, wanted.length() - 1).trim();
        }

        for (SecretBlendDefinition definition : getSecretBlendDefinitions()) {
            if (definition.name().equalsIgnoreCase(wanted)) return definition;
        }
        return null;
    }

    private static List<? extends String> getConfiguredSecretBlendEntries() {
        try {
            List<? extends String> configured = SERVER.secretBlends.get();

            // Existing worlds that never customized either historical default set should receive
            // the new built-ins without administrators having to delete their server config.
            if (configured.equals(LEGACY_DEFAULT_SECRET_BLENDS)
                    || configured.equals(PREVIOUS_DEFAULT_SECRET_BLENDS)
                    || configured.equals(FONTED_DEFAULT_SECRET_BLENDS)) {
                return DEFAULT_SECRET_BLENDS;
            }
            return configured;
        } catch (IllegalStateException ignored) {
            // Config can be queried while client resources/tooltips are bootstrapping.
            return DEFAULT_SECRET_BLENDS;
        }
    }

    /** Resolves visual settings for a configured or user-created secret blend. */
    public static SecretBlendVisualDefinition getSecretBlendVisual(String blendName) {
        String wanted = blendName == null ? "" : blendName.trim();
        SecretBlendVisualDefinition wildcard = null;

        for (String entry : getConfiguredSecretBlendVisualEntries()) {
            SecretBlendVisualEntry parsed = parseSecretBlendVisual(entry);
            if (parsed == null) continue;
            if (parsed.name().equals("*")) {
                wildcard = parsed.visual();
            } else if (!wanted.isEmpty() && parsed.name().equalsIgnoreCase(wanted)) {
                return parsed.visual();
            }
        }

        return wildcard != null ? wildcard : new SecretBlendVisualDefinition(-1, 0.58f, 0.08f, 0.10f);
    }

    private static List<? extends String> getConfiguredSecretBlendVisualEntries() {
        try {
            return SERVER.secretBlendVisuals.get();
        } catch (IllegalStateException ignored) {
            return DEFAULT_SECRET_BLEND_VISUALS;
        }
    }

    private static SecretBlendVisualEntry parseSecretBlendVisual(String entry) {
        if (entry == null) return null;
        int equals = entry.indexOf('=');
        if (equals <= 0 || equals >= entry.length() - 1) return null;

        String name = entry.substring(0, equals).trim();
        String[] fields = entry.substring(equals + 1).trim().split("\\|", -1);
        if (name.isEmpty() || fields.length < 1 || fields.length > 4) return null;

        int colorRgb = -1;
        String color = fields[0].trim();
        if (!color.equalsIgnoreCase("auto") && !color.isEmpty()) {
            String hex = color.startsWith("#") ? color.substring(1) : color;
            if (!hex.matches("[0-9a-fA-F]{6}")) return null;
            try {
                colorRgb = Integer.parseInt(hex, 16);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        float tint = parseVisualStrength(fields, 1, 0.58f);
        float brightness = parseVisualStrength(fields, 2, 0.08f);
        float saturation = fields.length >= 4
                ? parseVisualStrength(fields, 3, 0.10f)
                : 0.10f;
        return new SecretBlendVisualEntry(name, new SecretBlendVisualDefinition(colorRgb, tint, brightness, saturation));
    }

    private static float parseVisualStrength(String[] fields, int index, float fallback) {
        if (index >= fields.length || fields[index].isBlank()) return fallback;
        try {
            return Math.max(0.0f, Math.min(1.0f, Float.parseFloat(fields[index].trim())));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    /** Returns a built-up smoking bonus definition for a rare legendary secret blend, if any. */
    public static SecretBlendBonusDefinition getSecretBlendBonus(String blendName) {
        if (blendName == null || blendName.isBlank()) return null;
        String wanted = blendName.trim();
        for (String entry : getConfiguredSecretBlendBonusEntries()) {
            SecretBlendBonusDefinition parsed = parseSecretBlendBonus(entry, wanted);
            if (parsed != null) return parsed;
        }
        return null;
    }

    private static List<? extends String> getConfiguredSecretBlendBonusEntries() {
        try {
            return SERVER.secretBlendBonuses.get();
        } catch (IllegalStateException ignored) {
            return DEFAULT_SECRET_BLEND_BONUSES;
        }
    }

    private static SecretBlendBonusDefinition parseSecretBlendBonus(String entry, String wantedName) {
        if (entry == null) return null;
        int equals = entry.indexOf('=');
        if (equals <= 0 || equals >= entry.length() - 1) return null;

        String name = entry.substring(0, equals).trim();
        if (!name.equalsIgnoreCase(wantedName)) return null;

        String[] fields = entry.substring(equals + 1).trim().split("\\|", 2);
        if (fields.length < 2) return null;

        int threshold;
        try {
            threshold = Math.max(1, Integer.parseInt(fields[0].trim()));
        } catch (NumberFormatException ignored) {
            threshold = 3;
        }

        List<ConfiguredSmokingEffect> effects = new ArrayList<>();
        for (String token : fields[1].split(";")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split(",");
            if (parts.length < 3) continue;
            String effectId = parts[0].trim();
            try {
                int duration = Integer.parseInt(parts[1].trim());
                int amplifier = Integer.parseInt(parts[2].trim());
                effects.add(new ConfiguredSmokingEffect(effectId, duration, amplifier));
            } catch (NumberFormatException ignored) {
                // ignore malformed bonus entry parts
            }
        }

        return effects.isEmpty() ? null : new SecretBlendBonusDefinition(name, threshold, List.copyOf(effects));
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

        String header = entry.substring(0, equals).trim();
        String recipe = entry.substring(equals + 1).trim();
        if (header.isEmpty() || recipe.isEmpty()) return null;

        // Compatibility with configs generated by the short-lived per-blend-font build.
        String name = header;
        int legacyFontSeparator = header.lastIndexOf('@');
        if (legacyFontSeparator > 0) {
            name = header.substring(0, legacyFontSeparator).trim();
        }
        if (name.isEmpty()) return null;

        List<SecretBlendRequirement> requirements = new ArrayList<>();
        for (String rawComponent : recipe.split("\\+")) {
            SecretBlendRequirement requirement = parseRequirement(rawComponent);
            if (requirement == null) return null;
            requirements.add(requirement);
        }

        if (requirements.size() < 2 || requirements.size() > 3) return null;
        return new SecretBlendDefinition(name, List.copyOf(requirements));
    }

    private static SecretBlendRequirement parseRequirement(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] fields = raw.trim().split("\\|", -1);
        if (fields.length > 4) return null;

        String variety = normalizeToken(fields[0]);
        if (variety.isEmpty()) return null;

        // Variety-only entries act as wildcards for quality, cure, and flavor.
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

    public record SecretBlendDefinition(
            String name,
            List<SecretBlendRequirement> requirements
    ) {}

    public record SecretBlendVisualDefinition(
            int colorRgb,
            float tintStrength,
            float brightnessLift,
            float saturationBoost
    ) {}

    public record SecretBlendBonusDefinition(
            String name,
            int threshold,
            List<ConfiguredSmokingEffect> effects
    ) {}

    public record ConfiguredSmokingEffect(
            String effectId,
            int duration,
            int amplifier
    ) {}

    private record SecretBlendVisualEntry(
            String name,
            SecretBlendVisualDefinition visual
    ) {}

    public record SecretBlendRequirement(String variety, int minQuality, String cure, String flavor) {
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
        public final ForgeConfigSpec.IntValue particleDensity;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("curios");
            particleDensity = builder
                    .comment("Chance divisor for Curios mouth smoke. Lower = more smoke.")
                    .defineInRange("particleDensity", 6, 1, 24);
            builder.pop();
        }
    }

    public static class Server {
        public final ForgeConfigSpec.BooleanValue enableQualitySystem;
        public final ForgeConfigSpec.BooleanValue enableNicotineEffects;
        public final ForgeConfigSpec.IntValue genericAutomationQualityPenalty;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> secretBlends;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> secretBlendVisuals;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> secretBlendBonuses;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("gameplay");
            enableQualitySystem = builder
                    .comment("Enable the tobacco quality system: quality scores/tiers and quality-based smoking bonuses.")
                    .comment("When false, quality is hidden and ignored for gameplay, but existing quality data is preserved so it can be turned back on later.")
                    .define("enableQualitySystem", true);

            enableNicotineEffects = builder
                    .comment("Apply Tobacconist's nicotine status effect when tobacco is smoked.")
                    .comment("Disabling this does not disable configured additional smoking effects.")
                    .define("enableNicotineEffects", true);

            genericAutomationQualityPenalty = builder
                    .comment("Raw tobacco-quality points removed when a Cigarette or Cigar is made by generic crafting automation while Create is installed.")
                    .comment("Hand crafting and Tobacconist's dedicated Create Deployer -> Incomplete Product -> Mechanical Press route keep full quality.")
                    .comment("The quality system uses an underlying 0-120 scale; the default penalty of 10 is usually about one displayed Product Quality level.")
                    .comment("Set to 0 to disable this automation penalty.")
                    .defineInRange("genericAutomationQualityPenalty", 10, 0, 50);
            builder.pop();

            builder.push("blending");
            secretBlends = builder
                    .comment("Hidden named tobacco blends with exact per-variety requirements.")
                    .comment("Format: Display Name=variety|minQuality|cure|flavor+variety|minQuality|cure|flavor[+...]")
                    .comment("Quality is a minimum (0-120). Cure: air, fire, sun, flue, or * for any.")
                    .comment("Flavor: none, *, or a flavor id such as apple, chorus_fruit, coffee, vanilla, or mint.")
                    .comment("Order does not matter. Legacy fonted and variety-only entries remain accepted for migration.")
                    .comment("Valid varieties: wild, virginia, burley, oriental, dokha, shade.")
                    .comment("The bundled compatibility secrets use generic flavor tags; they become craftable when matching ingredients exist.")
                    .comment("Servers can rename, remove, or add entries for their own roleplay/lore.")
                    .defineListAllowEmpty(
                            List.of("secretBlends"),
                            DEFAULT_SECRET_BLENDS,
                            obj -> obj instanceof String
                    );

            secretBlendVisuals = builder
                    .comment("Visual styling for secret blends. Optional per-blend entries override the wildcard fallback.")
                    .comment("Format: Blend Name=#RRGGBB|tintStrength|brightnessLift|saturationBoost")
                    .comment("Use color 'auto' to derive the hue from the blend's stored varieties/cures/flavors.")
                    .comment("Values are 0.0-1.0. Suggested bold-but-still-readable ranges: tint 0.45-0.70, brightness 0.04-0.12, saturation 0.05-0.15.")
                    .comment("Example: Last Harvest=#B8A45A|0.65|0.08|0.12")
                    .comment("Wildcard example: *=auto|0.58|0.08|0.10")
                    .comment("Legacy 3-field entries remain accepted; their third number is treated as brightnessLift.")
                    .defineListAllowEmpty(
                            List.of("secretBlendVisuals"),
                            DEFAULT_SECRET_BLEND_VISUALS,
                            obj -> obj instanceof String
                    );

            secretBlendBonuses = builder
                    .comment("Built-up bonuses for a small set of legendary secret blends only.")
                    .comment("Format: Blend Name=threshold|effect_id,duration,amplifier;effect_id,duration,amplifier")
                    .comment("Threshold is the number of puffs/draws before the reward triggers, after which that blend's counter resets.")
                    .comment("Example: Crown & Ember=3|minecraft:fire_resistance,180,0;minecraft:strength,120,0")
                    .defineListAllowEmpty(
                            List.of("secretBlendBonuses"),
                            DEFAULT_SECRET_BLEND_BONUSES,
                            obj -> obj instanceof String
                    );
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