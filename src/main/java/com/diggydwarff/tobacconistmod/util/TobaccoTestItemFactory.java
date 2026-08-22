package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Admin-only factory used by /tobacconist give for quickly testing tobacco metadata combinations. */
public final class TobaccoTestItemFactory {
    private static final Pattern OPTION_PATTERN = Pattern.compile("([A-Za-z][A-Za-z0-9_]*)=(\\\"[^\\\"]*\\\"|\\S+)");

    private TobaccoTestItemFactory() {}

    public static ItemStack create(String typeValue, String optionString) {
        String type = normalize(typeValue);
        Map<String, String> options = parseOptions(optionString);

        ItemStack result = switch (type) {
            case "raw", "raw_leaf" -> makeRaw(options);
            case "leaf", "cured_leaf", "dry_leaf" -> makeDryLeaf(options);
            case "loose", "tobacco" -> makeLoose(options, defaultCutFor(type));
            case "blend", "blended" -> makeBlend(options, defaultCutFor(type));
            case "cigarette" -> makeCigarette(options);
            case "cigar" -> makeCigar(options);
            case "shisha" -> makeShisha(options);
            default -> throw new IllegalArgumentException(
                    "Unknown type '" + typeValue + "'. Use raw, leaf, loose, blend, cigarette, cigar, or shisha."
            );
        };

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Could not build that tobacco item from the supplied options.");
        }

        String label = options.getOrDefault("label", "").trim();
        if (!label.isEmpty()) {
            TobaccoLabelHelper.setProductLabel(result, label);
        }

        int requestedCount = intOption(options, "count", 1);
        int max = Math.max(1, result.getMaxStackSize());
        result.setCount(Math.max(1, Math.min(max, requestedCount)));

        int damage = intOption(options, "damage", 0);
        if (result.isDamageableItem() && damage > 0) {
            result.setDamageValue(Math.min(result.getMaxDamage() - 1, damage));
        }

        return result;
    }

    public static String helpText() {
        return "Types: raw, leaf, loose, blend, cigarette, cigar, shisha. "
                + "Options use key=value, e.g. variety=virginia quality=90 cure=flue cut=shag "
                + "flavor=berry fermented=true age=365 ruined=false count=16. "
                + "Blends may use components=virginia:95:flue:none,burley:90:air:berry,shade:88:sun:none.";
    }

    private static ItemStack makeRaw(Map<String, String> options) {
        String variety = varietyOption(options, "variety", "virginia");
        ItemStack stack = new ItemStack(rawItem(variety));
        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);
        int growthQuality = Math.max(0, Math.min(70, intOption(options, "quality", 40)));
        tag.putInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY, growthQuality);
        applyBarrelState(tag, options);
        return stack;
    }

    private static ItemStack makeDryLeaf(Map<String, String> options) {
        String variety = varietyOption(options, "variety", "virginia");
        ItemStack stack = new ItemStack(dryItem(variety));
        applyProcessedMetadata(
                stack,
                intOption(options, "quality", 75),
                cureOption(options, "cure", TobaccoCuringHelper.CURE_AIR),
                "",
                flavorOption(options, "flavor", ""),
                options
        );
        return stack;
    }

    private static ItemStack makeLoose(Map<String, String> options, String defaultCut) {
        String variety = varietyOption(options, "variety", "virginia");
        ItemStack stack = new ItemStack(looseItem(variety));
        applyProcessedMetadata(
                stack,
                intOption(options, "quality", 75),
                cureOption(options, "cure", TobaccoCuringHelper.CURE_AIR),
                cutOption(options, "cut", defaultCut),
                flavorOption(options, "flavor", ""),
                options
        );
        return stack;
    }

    private static ItemStack makeBlend(Map<String, String> options, String defaultCut) {
        String componentSpec = options.getOrDefault("components", "").trim();
        if (componentSpec.isEmpty()) {
            throw new IllegalArgumentException(
                    "Blend requires components=variety:quality:cure:flavor,... (2 or 3 unique varieties)."
            );
        }

        int defaultQuality = intOption(options, "quality", 75);
        String defaultCure = cureOption(options, "cure", TobaccoCuringHelper.CURE_AIR);
        String defaultFlavor = flavorOption(options, "flavor", "");
        String cut = cutOption(options, "cut", defaultCut);

        List<ItemStack> components = new ArrayList<>();
        for (String entry : componentSpec.split(",")) {
            if (entry.isBlank()) continue;
            String[] fields = entry.trim().split(":", -1);
            if (fields.length > 4) {
                throw new IllegalArgumentException("Invalid blend component: " + entry);
            }

            String variety = validateVariety(fields[0]);
            int quality = fields.length >= 2 && !fields[1].isBlank()
                    ? parseInt(fields[1], "component quality")
                    : defaultQuality;
            String cure = fields.length >= 3 && !fields[2].isBlank()
                    ? validateCure(fields[2])
                    : defaultCure;
            String flavor = fields.length >= 4 && !fields[3].isBlank()
                    ? normalizeFlavor(fields[3])
                    : defaultFlavor;

            ItemStack component = new ItemStack(looseItem(variety));
            applyProcessedMetadata(component, quality, cure, cut, flavor, options);
            components.add(component);
        }

        if (components.size() < 2 || components.size() > 3) {
            throw new IllegalArgumentException("Blend requires exactly 2 or 3 components.");
        }

        ItemStack result = TobaccoBlendHelper.blend(components);
        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    "Blend components are incompatible. Varieties must be unique and cut/fermented/ruined state must match."
            );
        }
        return result;
    }

    private static ItemStack makeFiller(Map<String, String> options, String defaultCut) {
        if (options.containsKey("components")) {
            return makeBlend(options, defaultCut);
        }
        return makeLoose(options, defaultCut);
    }

    private static ItemStack makeCigarette(Map<String, String> options) {
        ItemStack filler = makeFiller(options, TobaccoCuringHelper.CUT_SHAG);
        ItemStack result = TobaccoProductCraftingHelper.makeCigarette(filler);
        if (result.isEmpty()) throw new IllegalArgumentException("Could not create cigarette filler.");
        return result;
    }

    private static ItemStack makeCigar(Map<String, String> options) {
        ItemStack filler = makeFiller(options, TobaccoCuringHelper.CUT_FLAKE);

        String wrapperVariety = varietyOption(options, "wrapper", options.getOrDefault("variety", "virginia"));
        ItemStack wrapper = new ItemStack(dryItem(wrapperVariety));
        CompoundTag wrapperTag = LegacyItemTags.getOrCreateTag(wrapper);
        int wrapperQuality = TobaccoCuringHelper.clampQuality(intOption(
                options,
                "wrapperquality",
                intOption(options, "quality", 75)
        ));
        wrapperTag.putInt(TobaccoCuringHelper.TAG_QUALITY, wrapperQuality);
        wrapperTag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(wrapperQuality));
        wrapperTag.putString(
                TobaccoCuringHelper.TAG_CURE_TYPE,
                cureOption(options, "wrappercure", cureOption(options, "cure", TobaccoCuringHelper.CURE_AIR))
        );

        if (boolOption(options, "wrapperfermented", boolOption(options, "fermented", false))) {
            wrapperTag.putBoolean(TobaccoBarrelBlockEntity.TAG_FERMENTED, true);
        }
        int wrapperAge = intOption(options, "wrapperage", intOption(options, "age", 0));
        if (wrapperAge > 0) wrapperTag.putInt(TobaccoBarrelBlockEntity.TAG_AGED_DAYS, wrapperAge);
        if (boolOption(options, "wrapperruined", false)) {
            wrapperTag.putBoolean(TobaccoBarrelBlockEntity.TAG_RUINED, true);
        }

        ItemStack result = TobaccoProductCraftingHelper.makeCigar(filler, wrapper);
        if (result.isEmpty()) throw new IllegalArgumentException("Could not create cigar filler/wrapper.");
        return result;
    }

    private static ItemStack makeShisha(Map<String, String> options) {
        Map<String, String> fillerOptions = new HashMap<>(options);
        // flavor=/flavors= describe the wet Shisha treatment, not aromatic metadata on the filler.
        // Rough remains the convenient default, but cut= now lets the test command exercise any loose cut.
        fillerOptions.remove("flavor");
        fillerOptions.remove("flavors");
        ItemStack filler = makeFiller(fillerOptions, TobaccoCuringHelper.CUT_ROUGH);

        String flavorList = options.getOrDefault("flavors", options.getOrDefault("flavor", "berry"));
        List<String> shishaFlavors = new ArrayList<>();
        for (String id : flavorList.split(",")) {
            if (id.isBlank()) continue;
            BottledMolassesFlavors flavor = findMolassesFlavor(id);
            if (flavor == null) {
                throw new IllegalArgumentException("Unknown molasses flavor: " + id);
            }
            shishaFlavors.add(flavor.getShishaFlavorTag());
        }
        if (shishaFlavors.isEmpty() || shishaFlavors.size() > 3) {
            throw new IllegalArgumentException("Shisha requires 1 to 3 flavors.");
        }

        return TobaccoProcessingHelper.createShisha(filler, shishaFlavors);
    }

    private static void applyProcessedMetadata(
            ItemStack stack,
            int qualityValue,
            String cure,
            String cut,
            String flavor,
            Map<String, String> options
    ) {
        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);
        int quality = TobaccoCuringHelper.clampQuality(qualityValue);
        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, quality);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(quality));
        tag.putString(TobaccoCuringHelper.TAG_CURE_TYPE, cure);

        if (!cut.isEmpty()) tag.putString(TobaccoCuringHelper.TAG_CUT_TYPE, cut);
        else tag.remove(TobaccoCuringHelper.TAG_CUT_TYPE);

        if (!flavor.isEmpty()) {
            tag.putString(TobaccoAromaticHelper.TAG_FLAVOR_ID, flavor);
            tag.putString(TobaccoAromaticHelper.TAG_FLAVOR_NAME, TobaccoAromaticHelper.formatFlavorId(flavor));
        } else {
            tag.remove(TobaccoAromaticHelper.TAG_FLAVOR_ID);
            tag.remove(TobaccoAromaticHelper.TAG_FLAVOR_NAME);
        }

        applyBarrelState(tag, options);
    }

    private static void applyBarrelState(CompoundTag tag, Map<String, String> options) {
        if (boolOption(options, "fermented", false)) {
            tag.putBoolean(TobaccoBarrelBlockEntity.TAG_FERMENTED, true);
        } else {
            tag.remove(TobaccoBarrelBlockEntity.TAG_FERMENTED);
        }

        int age = Math.max(0, intOption(options, "age", 0));
        if (age > 0) tag.putInt(TobaccoBarrelBlockEntity.TAG_AGED_DAYS, age);
        else tag.remove(TobaccoBarrelBlockEntity.TAG_AGED_DAYS);

        if (boolOption(options, "ruined", false)) {
            tag.putBoolean(TobaccoBarrelBlockEntity.TAG_RUINED, true);
        } else {
            tag.remove(TobaccoBarrelBlockEntity.TAG_RUINED);
        }
    }

    private static Map<String, String> parseOptions(String optionString) {
        Map<String, String> options = new HashMap<>();
        if (optionString == null || optionString.isBlank()) return options;

        Matcher matcher = OPTION_PATTERN.matcher(optionString);
        int consumedTo = 0;
        while (matcher.find()) {
            if (!optionString.substring(consumedTo, matcher.start()).isBlank()) {
                throw new IllegalArgumentException("Invalid option syntax near: "
                        + optionString.substring(consumedTo, matcher.start()).trim());
            }
            String key = matcher.group(1).toLowerCase(Locale.ROOT);
            String value = matcher.group(2);
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            options.put(key, value);
            consumedTo = matcher.end();
        }

        if (!optionString.substring(consumedTo).isBlank()) {
            throw new IllegalArgumentException("Invalid option syntax near: "
                    + optionString.substring(consumedTo).trim());
        }
        return options;
    }

    private static String varietyOption(Map<String, String> options, String key, String fallback) {
        return validateVariety(options.getOrDefault(key.toLowerCase(Locale.ROOT), fallback));
    }

    private static String validateVariety(String value) {
        String variety = normalize(value);
        return switch (variety) {
            case "wild", "virginia", "burley", "oriental", "dokha", "shade" -> variety;
            default -> throw new IllegalArgumentException("Unknown tobacco variety: " + value);
        };
    }

    private static String cureOption(Map<String, String> options, String key, String fallback) {
        return validateCure(options.getOrDefault(key.toLowerCase(Locale.ROOT), fallback));
    }

    private static String validateCure(String value) {
        String cure = normalize(value).replace("_cured", "").replace("cured", "");
        return switch (cure) {
            case "air", "fire", "sun", "flue", "mixed" -> cure;
            default -> throw new IllegalArgumentException("Unknown cure: " + value);
        };
    }

    private static String cutOption(Map<String, String> options, String key, String fallback) {
        String cut = normalize(options.getOrDefault(key.toLowerCase(Locale.ROOT), fallback)).replace("_cut", "");
        return switch (cut) {
            case "rough", "ribbon", "shag", "flake" -> cut;
            default -> throw new IllegalArgumentException("Unknown cut: " + cut);
        };
    }

    private static String flavorOption(Map<String, String> options, String key, String fallback) {
        return normalizeFlavor(options.getOrDefault(key.toLowerCase(Locale.ROOT), fallback));
    }

    private static String normalizeFlavor(String value) {
        return TobaccoAromaticHelper.normalizeFlavorId(value);
    }

    private static int intOption(Map<String, String> options, String key, int fallback) {
        String value = options.get(key.toLowerCase(Locale.ROOT));
        return value == null || value.isBlank() ? fallback : parseInt(value, key);
    }

    private static int parseInt(String value, String name) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + name + ": " + value);
        }
    }

    private static boolean boolOption(Map<String, String> options, String key, boolean fallback) {
        String value = options.get(key.toLowerCase(Locale.ROOT));
        if (value == null || value.isBlank()) return fallback;
        return switch (normalize(value)) {
            case "true", "yes", "1", "on" -> true;
            case "false", "no", "0", "off" -> false;
            default -> throw new IllegalArgumentException("Invalid boolean for " + key + ": " + value);
        };
    }

    private static BottledMolassesFlavors findMolassesFlavor(String id) {
        String wanted = TobaccoAromaticHelper.normalizeFlavorId(id);
        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (TobaccoAromaticHelper.getFlavorId(flavor).equals(wanted)) return flavor;
        }
        return null;
    }

    private static String defaultCutFor(String ignored) {
        return TobaccoCuringHelper.CUT_RIBBON;
    }

    private static Item rawItem(String variety) {
        return switch (variety) {
            case "wild" -> ModItems.WILD_TOBACCO_LEAF.get();
            case "burley" -> ModItems.BURLEY_TOBACCO_LEAF.get();
            case "oriental" -> ModItems.ORIENTAL_TOBACCO_LEAF.get();
            case "dokha" -> ModItems.DOKHA_TOBACCO_LEAF.get();
            case "shade" -> ModItems.SHADE_TOBACCO_LEAF.get();
            default -> ModItems.VIRGINIA_TOBACCO_LEAF.get();
        };
    }

    private static Item dryItem(String variety) {
        return switch (variety) {
            case "wild" -> ModItems.WILD_TOBACCO_LEAF_DRY.get();
            case "burley" -> ModItems.BURLEY_TOBACCO_LEAF_DRY.get();
            case "oriental" -> ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get();
            case "dokha" -> ModItems.DOKHA_TOBACCO_LEAF_DRY.get();
            case "shade" -> ModItems.SHADE_TOBACCO_LEAF_DRY.get();
            default -> ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get();
        };
    }

    private static Item looseItem(String variety) {
        return switch (variety) {
            case "wild" -> ModItems.TOBACCO_LOOSE_WILD.get();
            case "burley" -> ModItems.TOBACCO_LOOSE_BURLEY.get();
            case "oriental" -> ModItems.TOBACCO_LOOSE_ORIENTAL.get();
            case "dokha" -> ModItems.TOBACCO_LOOSE_DOKHA.get();
            case "shade" -> ModItems.TOBACCO_LOOSE_SHADE.get();
            default -> ModItems.TOBACCO_LOOSE_VIRGINIA.get();
        };
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
