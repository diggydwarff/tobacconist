package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Shared crafting/Create logic for two- and three-variety loose tobacco blends. */
public final class TobaccoBlendHelper {
    public static final String TAG_BLEND_COMPONENTS = "BlendComponents";
    public static final String TAG_BLEND_COMPONENT_DATA = "BlendComponentData";
    public static final String TAG_BLEND_NAME = "BlendName";

    private static final String COMPONENT_VARIETY = "Variety";
    private static final String COMPONENT_QUALITY = "Quality";
    private static final String COMPONENT_CURE = "Cure";
    private static final String COMPONENT_FLAVOR = "Flavor";

    private TobaccoBlendHelper() {}

    public static boolean isBlendableBaseTobacco(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == ModItems.TOBACCO_LOOSE_WILD.get()
                || item == ModItems.TOBACCO_LOOSE_VIRGINIA.get()
                || item == ModItems.TOBACCO_LOOSE_BURLEY.get()
                || item == ModItems.TOBACCO_LOOSE_ORIENTAL.get()
                || item == ModItems.TOBACCO_LOOSE_DOKHA.get()
                || item == ModItems.TOBACCO_LOOSE_SHADE.get();
    }

    public static boolean canBlend(List<ItemStack> inputs) {
        if (inputs == null || inputs.size() < 2 || inputs.size() > 3) return false;

        Set<String> componentIdentities = new HashSet<>();
        String cut = null;
        Boolean fermented = null;
        Boolean ruined = null;

        for (ItemStack stack : inputs) {
            if (!isBlendableBaseTobacco(stack)) return false;

            String variety = getVarietyId(stack);
            if (variety.isEmpty()) return false;

            String stackCut = TobaccoCuringHelper.getCutType(stack);
            String stackCure = TobaccoCuringHelper.getCureType(stack);
            String stackFlavor = TobaccoAromaticHelper.normalizeFlavorId(
                    TobaccoAromaticHelper.getFlavorId(stack)
            );

            // Quality-only differences of otherwise identical tobacco belong in the
            // homogenizer, not the blender. The same variety is still a valid blend
            // component when its cure or aromatic casing is genuinely different.
            String componentIdentity = variety + "|" + stackCure + "|" + stackFlavor;
            if (!componentIdentities.add(componentIdentity)) return false;

            boolean stackFermented = TobaccoBarrelBlockEntity.isFermented(stack);
            boolean stackRuined = TobaccoBarrelBlockEntity.isRuined(stack);

            if (cut == null) {
                cut = stackCut;
                fermented = stackFermented;
                ruined = stackRuined;
            } else if (!cut.equals(stackCut)
                    || fermented != stackFermented
                    || ruined != stackRuined) {
                return false;
            }
        }

        return true;
    }

    public static ItemStack blend(List<ItemStack> inputs) {
        if (!canBlend(inputs)) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(ModItems.BLENDED_TOBACCO.get(), inputs.size());
        CompoundTag firstTag = LegacyItemTags.getTag(inputs.getFirst());
        CompoundTag tag = firstTag == null ? new CompoundTag() : firstTag.copy();

        tag.remove(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
        tag.remove(TobaccoLabelHelper.TAG_PRODUCT_LABEL);
        tag.remove("PackedTobaccoData");
        tag.remove(TAG_BLEND_COMPONENTS);
        tag.remove(TAG_BLEND_COMPONENT_DATA);
        tag.remove(TAG_BLEND_NAME);
        tag.remove(TobaccoAromaticHelper.TAG_FLAVOR_ID);
        tag.remove(TobaccoAromaticHelper.TAG_FLAVOR_NAME);

        int qualityTotal = 0;
        int ageTotal = 0;
        List<String> varieties = new ArrayList<>();
        List<TobaccoBlendComponent> componentData = new ArrayList<>();

        String commonCure = null;
        boolean mixedCure = false;
        Map<String, String> aromaticFlavors = new LinkedHashMap<>();

        for (ItemStack input : inputs) {
            int quality = TobaccoCuringHelper.getQuality(input);
            String variety = getVarietyId(input);
            String cure = TobaccoCuringHelper.getCureType(input);
            String flavor = TobaccoAromaticHelper.getFlavorId(input);
            String flavorName = TobaccoAromaticHelper.getFlavorName(input);

            qualityTotal += quality;
            ageTotal += TobaccoBarrelBlockEntity.getAgedDays(input);
            varieties.add(variety);
            componentData.add(new TobaccoBlendComponent(variety, quality, cure, flavor));

            if (commonCure == null) commonCure = cure;
            else if (!commonCure.equals(cure)) mixedCure = true;

            if (!flavor.isEmpty()) {
                aromaticFlavors.putIfAbsent(flavor, flavorName);
            }
        }

        int quality = qualityTotal / inputs.size();
        int agedDays = ageTotal / inputs.size();

        tag.putString("TobaccoType", "blend");
        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, quality);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(quality));
        tag.putString(
                TobaccoCuringHelper.TAG_CURE_TYPE,
                mixedCure ? TobaccoCuringHelper.CURE_MIXED : commonCure
        );
        tag.putString(TobaccoCuringHelper.TAG_CUT_TYPE, TobaccoCuringHelper.getCutType(inputs.getFirst()));

        if (agedDays > 0) tag.putInt("AgedDays", agedDays);
        else tag.remove("AgedDays");

        if (!aromaticFlavors.isEmpty()) {
            if (aromaticFlavors.size() == 1) {
                Map.Entry<String, String> only = aromaticFlavors.entrySet().iterator().next();
                tag.putString(TobaccoAromaticHelper.TAG_FLAVOR_ID, only.getKey());
                tag.putString(TobaccoAromaticHelper.TAG_FLAVOR_NAME, only.getValue());
            } else {
                tag.putString(TobaccoAromaticHelper.TAG_FLAVOR_ID, TobaccoAromaticHelper.FLAVOR_MIXED);
                tag.putString(TobaccoAromaticHelper.TAG_FLAVOR_NAME, "Mixed Aromatic");
            }
        }

        writeComponents(tag, varieties, componentData);

        String secretName = TobacconistConfig.findSecretBlendName(componentData);
        if (!secretName.isEmpty()) {
            tag.putString(TAG_BLEND_NAME, secretName);
        }

        LegacyItemTags.setTag(result, tag);
        return result;
    }

    private static void writeComponents(
            CompoundTag tag,
            List<String> varieties,
            List<TobaccoBlendComponent> componentData
    ) {
        ListTag components = new ListTag();
        varieties.stream().sorted().forEach(v -> components.add(StringTag.valueOf(v)));
        tag.put(TAG_BLEND_COMPONENTS, components);

        ListTag detailed = new ListTag();
        componentData.stream()
                .sorted((a, b) -> a.variety().compareTo(b.variety()))
                .forEach(component -> {
                    CompoundTag entry = new CompoundTag();
                    entry.putString(COMPONENT_VARIETY, component.variety());
                    entry.putInt(COMPONENT_QUALITY, component.quality());
                    entry.putString(COMPONENT_CURE, component.cure());
                    if (!component.flavorId().isEmpty()) {
                        entry.putString(COMPONENT_FLAVOR, component.flavorId());
                    }
                    detailed.add(entry);
                });
        tag.put(TAG_BLEND_COMPONENT_DATA, detailed);
    }

    public static List<String> getComponents(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null || !tag.contains(TAG_BLEND_COMPONENTS)) return List.of();

        ListTag list = tag.getList(TAG_BLEND_COMPONENTS, 8);
        List<String> out = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            String value = list.getString(i);
            if (!value.isBlank()) out.add(value);
        }
        return out;
    }

    public static List<TobaccoBlendComponent> getComponentData(ItemStack stack) {
        return getComponentData(LegacyItemTags.getTag(stack));
    }

    public static List<TobaccoBlendComponent> getComponentData(CompoundTag tag) {
        if (tag == null) return List.of();

        if (tag.contains(TAG_BLEND_COMPONENT_DATA)) {
            ListTag list = tag.getList(TAG_BLEND_COMPONENT_DATA, 10);
            List<TobaccoBlendComponent> out = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                String variety = entry.getString(COMPONENT_VARIETY);
                if (variety.isBlank()) continue;
                out.add(new TobaccoBlendComponent(
                        variety,
                        entry.getInt(COMPONENT_QUALITY),
                        entry.getString(COMPONENT_CURE),
                        TobaccoAromaticHelper.normalizeFlavorId(entry.getString(COMPONENT_FLAVOR))
                ));
            }
            return out;
        }

        // Migration fallback for blends created by the first blending implementation.
        if (!tag.contains(TAG_BLEND_COMPONENTS)) return List.of();
        ListTag legacyList = tag.getList(TAG_BLEND_COMPONENTS, 8);
        List<String> legacy = new ArrayList<>(legacyList.size());
        for (int i = 0; i < legacyList.size(); i++) {
            String variety = legacyList.getString(i);
            if (!variety.isBlank()) legacy.add(variety);
        }
        if (legacy.isEmpty()) return List.of();

        int quality = tag.contains(TobaccoCuringHelper.TAG_QUALITY)
                ? TobaccoCuringHelper.clampQuality(tag.getInt(TobaccoCuringHelper.TAG_QUALITY))
                : 75;
        String cure = tag.getString(TobaccoCuringHelper.TAG_CURE_TYPE);
        if (cure.isBlank()) cure = TobaccoCuringHelper.CURE_AIR;
        String flavor = TobaccoAromaticHelper.getFlavorId(tag);
        String finalCure = cure;
        return legacy.stream()
                .map(variety -> new TobaccoBlendComponent(variety, quality, finalCure, flavor))
                .toList();
    }

    /** Cases every stored component in an existing blend and rechecks hidden-blend rules. */
    public static void applyFlavorToStoredComponents(ItemStack stack, String flavorId, String flavorName) {
        if (stack.isEmpty() || stack.getItem() != ModItems.BLENDED_TOBACCO.get()) return;

        List<TobaccoBlendComponent> existing = getComponentData(stack);
        if (existing.isEmpty()) return;

        List<TobaccoBlendComponent> updated = existing.stream()
                .map(c -> new TobaccoBlendComponent(c.variety(), c.quality(), c.cure(), flavorId))
                .toList();

        CompoundTag tag = LegacyItemTags.getOrCreateTag(stack);
        writeComponents(tag, updated.stream().map(TobaccoBlendComponent::variety).toList(), updated);
        tag.putString(TobaccoAromaticHelper.TAG_FLAVOR_ID, flavorId);
        tag.putString(TobaccoAromaticHelper.TAG_FLAVOR_NAME, flavorName);

        tag.remove(TAG_BLEND_NAME);
        String secretName = TobacconistConfig.findSecretBlendName(updated);
        if (!secretName.isEmpty()) tag.putString(TAG_BLEND_NAME, secretName);
    }

    /**
     * Human-readable, per-component description of a blend. This is intentionally derived
     * from the stored component snapshots so cigarettes/cigars do not collapse a blend
     * into only its averaged quality/cure/flavor metadata.
     */
    public static List<String> getDetailedComponentLines(CompoundTag tag) {
        List<TobaccoBlendComponent> components = getComponentData(tag);
        if (components.isEmpty()) return List.of();

        List<String> lines = new ArrayList<>(components.size());
        for (TobaccoBlendComponent component : components) {
            StringBuilder line = new StringBuilder(formatVariety(component.variety()));

            if (TobacconistConfig.isQualitySystemEnabled()) {
                line.append(" Q").append(component.quality());
            }

            if (!component.cure().isBlank()) {
                line.append(" • ").append(TobaccoCuringHelper.getCureDisplayName(component.cure()));
            }

            String flavor = TobaccoAromaticHelper.formatFlavorId(component.flavorId());
            line.append(" • ").append(flavor.isEmpty() ? "Plain" : flavor);
            lines.add(line.toString());
        }
        return lines;
    }

    public static String getIntrinsicBlendName(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        return tag == null ? "" : tag.getString(TAG_BLEND_NAME);
    }

    public static String getVarietyId(ItemStack stack) {
        if (stack.is(ModItems.TOBACCO_LOOSE_WILD.get())) return "wild";
        if (stack.is(ModItems.TOBACCO_LOOSE_VIRGINIA.get())) return "virginia";
        if (stack.is(ModItems.TOBACCO_LOOSE_BURLEY.get())) return "burley";
        if (stack.is(ModItems.TOBACCO_LOOSE_ORIENTAL.get())) return "oriental";
        if (stack.is(ModItems.TOBACCO_LOOSE_DOKHA.get())) return "dokha";
        if (stack.is(ModItems.TOBACCO_LOOSE_SHADE.get())) return "shade";
        return "";
    }

    public static String formatVariety(String id) {
        if (id == null || id.isBlank()) return "Unknown";
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }
}
