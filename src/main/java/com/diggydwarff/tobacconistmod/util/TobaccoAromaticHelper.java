package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Shared metadata and conversion rules for single-flavor aromatic tobacco. */
public final class TobaccoAromaticHelper {
    public static final String TAG_FLAVOR_ID = "AromaticFlavor";
    public static final String TAG_FLAVOR_NAME = "AromaticFlavorName";
    public static final String FLAVOR_MIXED = "mixed";

    public enum AromaticStrength {
        NONE,
        HINT,
        FULL
    }

    /** Human-facing aromatic strength for a loose blend or finished smoking product. */
    public record AromaticProfile(AromaticStrength strength, String flavorName) {
        public boolean isAromatic() {
            return strength != AromaticStrength.NONE && flavorName != null && !flavorName.isBlank();
        }

        public String tooltipLine() {
            if (!isAromatic()) return "";
            return strength == AromaticStrength.HINT
                    ? "Hint of " + flavorName
                    : "Flavor: " + flavorName;
        }
    }

    private TobaccoAromaticHelper() {}

    /** Any unused loose cut may be lightly cased once with a concentrated Flavoring Essence. */
    public static boolean canAromatize(ItemStack stack) {
        return TobaccoCuringHelper.isLooseTobacco(stack)
                && !isAromatic(stack)
                && !TobaccoBarrelBlockEntity.isRuined(stack);
    }

    public static ItemStack aromatize(ItemStack stack, BottledMolassesFlavors flavor) {
        if (flavor == null || !canAromatize(stack)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = stack.copy();
        result.setCount(1);

        String flavorId = getFlavorId(flavor);
        String flavorName = getFlavorDisplayName(flavor);
        CompoundTag tag = LegacyItemTags.getOrCreateTag(result);
        tag.putString(TAG_FLAVOR_ID, flavorId);
        tag.putString(TAG_FLAVOR_NAME, flavorName);

        // Flavoring a finished blend cases every component equally. Updating the component
        // snapshots also lets a configured hidden blend become discoverable after casing.
        TobaccoBlendHelper.applyFlavorToStoredComponents(result, flavorId, flavorName);
        return result;
    }

    public static boolean isAromatic(ItemStack stack) {
        return !getFlavorId(stack).isEmpty();
    }

    public static String getFlavorId(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        return getFlavorId(tag);
    }

    public static String getFlavorName(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        return getFlavorName(tag);
    }

    public static String getPackedFlavorId(ItemStack product) {
        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(product);
        return getFlavorId(packed);
    }

    public static String getPackedFlavorName(ItemStack product) {
        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(product);
        return getFlavorName(packed);
    }

    public static boolean isPackedAromatic(ItemStack product) {
        return getProductAromaticProfile(product).isAromatic();
    }

    /**
     * Returns the aromatic presentation for loose tobacco. Blends retain per-component flavor
     * snapshots, allowing a minority aromatic component to remain visible as a "Hint of" flavor
     * instead of collapsing to a generic Mixed Aromatic label.
     */
    public static AromaticProfile getAromaticProfile(ItemStack stack) {
        return getAromaticProfile(LegacyItemTags.getTag(stack));
    }

    /**
     * Finished smoking products normally carry their complete filler snapshot in
     * PackedTobaccoData. Use that snapshot first, then fall back to direct custom data for
     * older/test stacks.
     */
    public static AromaticProfile getProductAromaticProfile(ItemStack product) {
        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(product);
        AromaticProfile packedProfile = getAromaticProfile(packed);
        if (packedProfile.isAromatic()) return packedProfile;
        return getAromaticProfile(LegacyItemTags.getTag(product));
    }

    public static String getProductFlavorName(ItemStack product) {
        AromaticProfile profile = getProductAromaticProfile(product);
        return profile.isAromatic() ? profile.flavorName() : "";
    }

    public static boolean isProductAromatic(ItemStack product) {
        return getProductAromaticProfile(product).isAromatic();
    }

    public static AromaticProfile getAromaticProfile(CompoundTag tag) {
        if (tag == null) return new AromaticProfile(AromaticStrength.NONE, "");

        List<TobaccoBlendComponent> components = TobaccoBlendHelper.getComponentData(tag);
        if (!components.isEmpty()) {
            int flavored = 0;
            Set<String> flavorNames = new LinkedHashSet<>();

            for (TobaccoBlendComponent component : components) {
                String flavorId = normalizeFlavorId(component.flavorId());
                if (flavorId.isEmpty()) continue;
                flavored++;
                flavorNames.add(formatFlavorId(flavorId));
            }

            if (flavored == 0) {
                return new AromaticProfile(AromaticStrength.NONE, "");
            }

            String display = String.join(" + ", flavorNames);
            // Majority-aromatic blends read as fully flavored. A 1/3 or 1/2 aromatic blend is
            // intentionally presented as a subtle hint instead.
            AromaticStrength strength = flavored * 2 > components.size()
                    ? AromaticStrength.FULL
                    : AromaticStrength.HINT;
            return new AromaticProfile(strength, display);
        }

        String flavor = getFlavorName(tag);
        if (flavor.isEmpty()) {
            return new AromaticProfile(AromaticStrength.NONE, "");
        }
        return new AromaticProfile(AromaticStrength.FULL, flavor);
    }

    public static String getFlavorId(CompoundTag tag) {
        if (tag == null) return "";
        return normalizeFlavorId(tag.getString(TAG_FLAVOR_ID));
    }

    public static String getFlavorName(CompoundTag tag) {
        if (tag == null) return "";
        String explicit = tag.getString(TAG_FLAVOR_NAME);
        if (!explicit.isBlank()) return explicit;
        return formatFlavorId(getFlavorId(tag));
    }

    public static String getFlavorId(BottledMolassesFlavors flavor) {
        if (flavor == null) return "";
        String id = flavor.getFluidName().toLowerCase(Locale.ROOT);
        if (id.startsWith("molasses_")) {
            id = id.substring("molasses_".length());
        }
        return normalizeFlavorId(id);
    }

    public static String getFlavorDisplayName(BottledMolassesFlavors flavor) {
        if (flavor == null) return "";
        String display = flavor.getFluidDisplayName();
        if (display.endsWith(" Molasses")) {
            display = display.substring(0, display.length() - " Molasses".length());
        }
        if (display.equals("Molasses") || display.isBlank()) {
            return "Molasses";
        }
        return display;
    }

    public static String normalizeFlavorId(String value) {
        if (value == null) return "";
        String id = value.trim().toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');
        if (id.equals("none") || id.equals("unflavored")) return "";
        if (id.startsWith("molasses_")) id = id.substring("molasses_".length());
        return id;
    }

    public static String formatFlavorId(String id) {
        String normalized = normalizeFlavorId(id);
        if (normalized.isEmpty()) return "";
        if (FLAVOR_MIXED.equals(normalized)) return "Mixed Aromatic";
        if (normalized.equals("plain")) return "Molasses";

        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (getFlavorId(flavor).equals(normalized)) {
                return getFlavorDisplayName(flavor);
            }
        }

        StringBuilder out = new StringBuilder();
        for (String part : normalized.split("_")) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }
}
