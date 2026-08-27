package com.diggydwarff.tobacconistmod.util;

import net.minecraft.ChatFormatting;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

        TobacconistConfig.SecretBlendDefinition secret = TobacconistConfig.findSecretBlend(componentData);
        if (secret != null) {
            tag.putString(TAG_BLEND_NAME, secret.name());
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

        // Reads the initial blend-data layout for save compatibility.
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
        TobacconistConfig.SecretBlendDefinition secret = TobacconistConfig.findSecretBlend(updated);
        if (secret != null) {
            tag.putString(TAG_BLEND_NAME, secret.name());
        }
    }

    /** Builds display lines from the stored per-component blend snapshots. */
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
        return getIntrinsicBlendName(LegacyItemTags.getTag(stack));
    }

    public static String getIntrinsicBlendName(CompoundTag tag) {
        return tag == null ? "" : tag.getString(TAG_BLEND_NAME);
    }

    public static Component getIntrinsicBlendNameComponent(ItemStack stack) {
        return getIntrinsicBlendNameComponent(LegacyItemTags.getTag(stack));
    }

    public static String getSecretBlendName(ItemStack stack) {
        if (stack.isEmpty()) return "";
        CompoundTag direct = LegacyItemTags.getTag(stack);
        if (direct != null && !getIntrinsicBlendName(direct).isEmpty()) {
            return getIntrinsicBlendName(direct);
        }

        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        return packed == null ? "" : getIntrinsicBlendName(packed);
    }

    public static boolean isLegendarySecretBlend(ItemStack stack) {
        return !getSecretBlendName(stack).isEmpty()
                && TobacconistConfig.getSecretBlendBonus(getSecretBlendName(stack)) != null;
    }

    public static void appendLegendarySecretTooltip(ItemStack stack, List<Component> tooltip) {
        String blendName = getSecretBlendName(stack);
        if (blendName.isEmpty()) return;

        TobacconistConfig.SecretBlendBonusDefinition bonus = TobacconistConfig.getSecretBlendBonus(blendName);
        if (bonus == null || bonus.effects().isEmpty()) return;

        int accent = getLegendaryTooltipAccentRgb(stack);
        tooltip.add(Component.translatable("tobacconistmod.ui.legendary_bonus_after", bonus.threshold())
                .withStyle(style -> style.withColor(accent)));
        for (TobacconistConfig.ConfiguredSmokingEffect effect : bonus.effects()) {
            tooltip.add(Component.translatable(
                    "tobacconistmod.ui.legendary_bonus_effect",
                    configuredEffectComponent(effect)
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static Component configuredEffectComponent(TobacconistConfig.ConfiguredSmokingEffect effect) {
        ResourceLocation id = ResourceLocation.tryParse(effect.effectId());
        MutableComponent base;
        if (id != null && BuiltInRegistries.MOB_EFFECT.containsKey(id)) {
            base = Component.translatable(BuiltInRegistries.MOB_EFFECT.get(id).getDescriptionId());
        } else {
            base = Component.literal(effect.effectId());
        }

        int amplifierLevel = effect.amplifier() + 1;
        if (amplifierLevel > 1) {
            base.append(" ").append(Component.translatable("enchantment.level." + amplifierLevel));
        }
        return base;
    }

    /** True for a secret blend itself or a product carrying that blend in PackedTobaccoData. */
    public static boolean hasSecretBlendStyle(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!getIntrinsicBlendName(stack).isEmpty()) return true;

        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        return packed != null && !getIntrinsicBlendName(packed).isEmpty();
    }

    /**
     * Returns a dynamic ARGB multiplier for secret-blend items. The hue is automatically derived
     * from the actual blend components unless a server config entry supplies an explicit color.
     * We intentionally avoid model/texture hacks here: the visual identity comes from a stronger
     * tint plus optional brightness and saturation boosts, so custom user blends also work cleanly.
     */
    public static int getSecretBlendTintArgb(ItemStack stack) {
        if (!hasSecretBlendStyle(stack)) return 0xFFFFFFFF;

        CompoundTag direct = LegacyItemTags.getTag(stack);
        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        CompoundTag source = direct != null && !getIntrinsicBlendName(direct).isEmpty() ? direct : packed;
        if (source == null) source = direct;

        String blendName = getIntrinsicBlendName(source);
        List<TobaccoBlendComponent> components = getComponentData(source);
        TobacconistConfig.SecretBlendVisualDefinition visual = TobacconistConfig.getSecretBlendVisual(blendName);

        int targetRgb = visual.colorRgb() >= 0
                ? visual.colorRgb()
                : deriveSecretBlendColor(components, blendName);

        float tintStrength = visual.tintStrength();
        float saturationBoost = visual.saturationBoost();
        float brightnessLift = visual.brightnessLift();
        if (isLegendarySecretBlend(stack)) {
            // Legendary blends should still read as tobacco first. Keep the body mostly natural
            // and let the dedicated overlay layer carry the rare accent color.
            targetRgb = mixRgb(targetRgb, 0x9A7146, 0.34f);
            tintStrength = Math.min(tintStrength, 0.37f);
            saturationBoost *= 0.50f;
            brightnessLift *= 0.52f;
        }

        int tinted = mixRgb(0xFFFFFF, targetRgb, tintStrength);
        int polished = adjustSaturationAndBrightness(tinted, saturationBoost, brightnessLift);
        return 0xFF000000 | polished;
    }

    public static int getLegendaryOverlayArgb(ItemStack stack) {
        if (!isLegendarySecretBlend(stack)) return 0xFFFFFFFF;
        int target = getSecretBlendTargetRgb(stack);
        // Keep the accent vivid enough to read at 16x16, but warm it back toward cured tobacco so
        // the highlighted strands still feel like tobacco rather than painted pixels.
        int warmed = mixRgb(target, 0xA87952, 0.10f);
        int vivid = adjustSaturationAndBrightness(warmed, 0.26f, 0.09f);
        return 0xFF000000 | vivid;
    }

    public static int getLegendaryTooltipAccentRgb(ItemStack stack) {
        int target = getSecretBlendTargetRgb(stack);
        int readable = adjustSaturationAndBrightness(target, -0.20f, 0.10f);
        return mixRgb(readable, 0xD8C9A0, 0.42f);
    }

    public static int getLegendaryTooltipBorderStartArgb(ItemStack stack) {
        int target = adjustSaturationAndBrightness(getSecretBlendTargetRgb(stack), 0.18f, 0.20f);
        return 0xFF000000 | target;
    }

    public static int getLegendaryTooltipBorderEndArgb(ItemStack stack) {
        int target = getSecretBlendTargetRgb(stack);
        return 0xFF000000 | mixRgb(target, 0xC89232, 0.58f);
    }

    private static int getSecretBlendTargetRgb(ItemStack stack) {
        CompoundTag direct = LegacyItemTags.getTag(stack);
        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        CompoundTag source = direct != null && !getIntrinsicBlendName(direct).isEmpty() ? direct : packed;
        if (source == null) source = direct;

        String blendName = getIntrinsicBlendName(source);
        List<TobaccoBlendComponent> components = getComponentData(source);
        TobacconistConfig.SecretBlendVisualDefinition visual = TobacconistConfig.getSecretBlendVisual(blendName);
        return visual.colorRgb() >= 0
                ? visual.colorRgb()
                : deriveSecretBlendColor(components, blendName);
    }

    private static int deriveSecretBlendColor(List<TobaccoBlendComponent> components, String blendName) {
        if (components == null || components.isEmpty()) return 0xC99E62;

        long r = 0, g = 0, b = 0;
        for (TobaccoBlendComponent component : components) {
            int color = varietyColor(component.variety());
            color = applyCureColor(color, component.cure());
            color = applyFlavorColor(color, component.flavorId());
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
        }

        int count = components.size();
        int averaged = ((int) (r / count) << 16) | ((int) (g / count) << 8) | (int) (b / count);

        // Tiny deterministic name accent means two user-defined secrets with the same recipe can still
        // have slightly different visual identities without hard-coding any particular built-in name.
        int[] accents = {0xD7B15B, 0xB86F4F, 0x9A985B, 0x9A6C75, 0xB15E50, 0x84936B};
        int hash = blendName == null ? 0 : blendName.toLowerCase(Locale.ROOT).hashCode();
        return mixRgb(averaged, accents[Math.floorMod(hash, accents.length)], 0.16f);
    }

    private static int varietyColor(String variety) {
        return switch (normalizeVisualToken(variety)) {
            case "virginia" -> 0xDCA04D;
            case "burley" -> 0x98613F;
            case "oriental" -> 0xC98242;
            case "dokha" -> 0x87543B;
            case "shade" -> 0xA47658;
            case "wild" -> 0x817044;
            default -> 0xA87349;
        };
    }

    private static int applyCureColor(int base, String cure) {
        return switch (normalizeVisualToken(cure)) {
            case TobaccoCuringHelper.CURE_FIRE -> mixRgb(base, 0x7F3F2C, 0.38f);
            case TobaccoCuringHelper.CURE_SUN -> mixRgb(base, 0xE8C55B, 0.40f);
            case TobaccoCuringHelper.CURE_FLUE -> mixRgb(base, 0xDDAA55, 0.32f);
            case TobaccoCuringHelper.CURE_AIR -> mixRgb(base, 0xBDA57F, 0.20f);
            default -> base;
        };
    }

    private static int applyFlavorColor(int base, String flavorId) {
        String flavor = TobaccoAromaticHelper.normalizeFlavorId(flavorId);
        if (flavor.isEmpty()) return base;

        int accent = switch (flavor) {
            case "apple" -> 0x9EAD55;
            case "goldenapple", "honey" -> 0xE1B84B;
            case "cocoa", "coffee", "brownie" -> 0x70452F;
            case "chorus_fruit", "lavender" -> 0x9C6E9E;
            case "glowberry", "caramel", "cinnamon" -> 0xCB7942;
            case "vanilla", "custard", "coconut" -> 0xD9CBA1;
            case "cherry", "strawberry", "raspberry", "hibiscus" -> 0xB85B57;
            case "blackberry" -> 0x775A78;
            case "mint", "lime" -> 0x79965E;
            case "tea" -> 0x9A7B4B;
            default -> unknownFlavorAccent(flavor);
        };
        return mixRgb(base, accent, 0.24f);
    }

    private static int unknownFlavorAccent(String flavor) {
        int[] accents = {0xB98B55, 0x9C7A61, 0x8D8558, 0xA66A63, 0x806C7E, 0x77836A};
        return accents[Math.floorMod(flavor.hashCode(), accents.length)];
    }

    private static String normalizeVisualToken(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static int adjustSaturationAndBrightness(int rgb, float saturationBoost, float brightnessLift) {
        float[] hsv = rgbToHsv(rgb);
        hsv[1] = clamp01(hsv[1] * (1.0f + saturationBoost));
        hsv[2] = clamp01(hsv[2] + brightnessLift);
        return hsvToRgb(hsv[0], hsv[1], hsv[2]);
    }

    private static float[] rgbToHsv(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h;
        if (delta == 0.0f) h = 0.0f;
        else if (max == r) h = ((g - b) / delta) % 6.0f;
        else if (max == g) h = ((b - r) / delta) + 2.0f;
        else h = ((r - g) / delta) + 4.0f;
        h /= 6.0f;
        if (h < 0.0f) h += 1.0f;

        float s = max == 0.0f ? 0.0f : delta / max;
        float v = max;
        return new float[]{h, s, v};
    }

    private static int hsvToRgb(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        s = clamp01(s);
        v = clamp01(v);

        float scaled = h * 6.0f;
        int sector = (int) Math.floor(scaled);
        float fraction = scaled - sector;
        float p = v * (1.0f - s);
        float q = v * (1.0f - fraction * s);
        float t = v * (1.0f - (1.0f - fraction) * s);

        float r, g, b;
        switch (sector % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }

        return (Math.round(r * 255.0f) << 16)
                | (Math.round(g * 255.0f) << 8)
                | Math.round(b * 255.0f);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static int mixRgb(int baseRgb, int accentRgb, float accentWeight) {
        accentWeight = clamp01(accentWeight);
        float baseWeight = 1.0f - accentWeight;
        int r = Math.round(((baseRgb >> 16) & 0xFF) * baseWeight + ((accentRgb >> 16) & 0xFF) * accentWeight);
        int g = Math.round(((baseRgb >> 8) & 0xFF) * baseWeight + ((accentRgb >> 8) & 0xFF) * accentWeight);
        int b = Math.round((baseRgb & 0xFF) * baseWeight + (accentRgb & 0xFF) * accentWeight);
        return (r << 16) | (g << 8) | b;
    }

    public static Component getIntrinsicBlendNameComponent(CompoundTag tag) {
        String name = getIntrinsicBlendName(tag);
        if (name.isBlank()) return Component.empty();

        // Keep Minecraft's normal font. The seal, uppercase lettering and restrained antique-gold
        // color are enough to make a discovered secret blend read as special without looking alien
        // beside vanilla item names.
        return Component.literal("✦ " + name.toUpperCase(Locale.ROOT) + " ✦")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xD4B96A)));
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
