package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoAromaticHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendComponent;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoTooltipHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** One serializable Tobacconist metadata attribute shown by Create's Attribute Filter. */
public record CreateTobaccoItemAttribute(String category, String value) implements ItemAttribute {
    public static final MapCodec<CreateTobaccoItemAttribute> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("category").forGetter(CreateTobaccoItemAttribute::category),
            Codec.STRING.fieldOf("value").forGetter(CreateTobaccoItemAttribute::value)
    ).apply(instance, CreateTobaccoItemAttribute::new));

    public static final StreamCodec<ByteBuf, CreateTobaccoItemAttribute> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, CreateTobaccoItemAttribute::category,
                    ByteBufCodecs.STRING_UTF8, CreateTobaccoItemAttribute::value,
                    CreateTobaccoItemAttribute::new
            );

    @Override
    public boolean appliesTo(ItemStack stack, Level world) {
        return collect(stack).contains(this);
    }

    @Override
    public ItemAttributeType getType() {
        return CreateItemAttributeCompat.TOBACCO_METADATA.get();
    }

    @Override
    public MutableComponent format(boolean inverted) {
        Component formattedValue = switch (category) {
            case "product", "box_contents" -> TobaccoText.product(value);
            case "growth_quality_tier", "quality_tier" -> TobaccoText.qualityTierFromDisplay(value);
            case "growth_quality_band", "quality_band", "age_days", "age_threshold", "box_fill" -> Component.literal(value);
            case "growth_quality_threshold", "quality_threshold" -> TobaccoText.qualityThreshold(value);
            case "variety", "contains_variety", "wrapper_variety" -> TobaccoText.variety(value);
            case "cure" -> TobaccoText.cure(value);
            case "cut" -> TobaccoText.cut(value);
            case "flavor" -> TobaccoText.flavor(value);
            case "state" -> TobaccoText.state(value);
            default -> Component.literal(value);
        };

        MutableComponent text = switch (category) {
            case "product" -> Component.translatable("tobacconistmod.create.attribute.product", formattedValue);
            case "growth_quality_score" -> Component.translatable("tobacconistmod.create.attribute.growth_quality_score", formattedValue);
            case "growth_quality_tier", "growth_quality_threshold" -> Component.translatable("tobacconistmod.create.attribute.growth_quality", formattedValue);
            case "growth_quality_band" -> Component.translatable("tobacconistmod.create.attribute.growth_quality_band", formattedValue);
            case "quality_score" -> Component.translatable("tobacconistmod.create.attribute.quality_score", formattedValue);
            case "quality_tier", "quality_threshold" -> Component.translatable("tobacconistmod.create.attribute.quality", formattedValue);
            case "quality_band" -> Component.translatable("tobacconistmod.create.attribute.quality_band", formattedValue);
            case "age_days" -> Component.translatable("tobacconistmod.create.attribute.age_days", formattedValue);
            case "age_threshold" -> Component.translatable("tobacconistmod.create.attribute.aged_at_least", formattedValue);
            case "variety" -> Component.translatable("tobacconistmod.create.attribute.variety", formattedValue);
            case "contains_variety" -> Component.translatable("tobacconistmod.create.attribute.contains_variety", formattedValue);
            case "cure" -> Component.translatable("tobacconistmod.create.attribute.cure", formattedValue);
            case "cut" -> Component.translatable("tobacconistmod.create.attribute.cut", formattedValue);
            case "flavor" -> Component.translatable("tobacconistmod.create.attribute.flavor", formattedValue);
            case "blend_name" -> Component.translatable("tobacconistmod.create.attribute.blend", formattedValue);
            case "wrapper_variety" -> Component.translatable("tobacconistmod.create.attribute.wrapper_variety", formattedValue);
            case "box_contents" -> Component.translatable("tobacconistmod.create.attribute.box_contents", formattedValue);
            case "box_fill" -> Component.translatable("tobacconistmod.create.attribute.box_fill", formattedValue);
            case "box_label" -> Component.translatable("tobacconistmod.create.attribute.box_label", formattedValue);
            case "state" -> formattedValue.copy();
            default -> formattedValue.copy();
        };
        return inverted
                ? Component.translatable("tobacconistmod.create.attribute.not", text)
                : text;
    }

    @Override
    public String getTranslationKey() {
        // Format Tobacconist attributes locally instead of relying on Create translations.
        return "tobacconist_metadata";
    }

    static List<ItemAttribute> collect(ItemStack stack) {
        if (stack.isEmpty() || !isSupported(stack)) {
            return List.of();
        }

        ArrayList<ItemAttribute> attributes = new ArrayList<>();

        if (stack.is(ModItems.TOBACCO_BOX.get())) {
            add(attributes, "product", "Tobacco Box");

            ItemStack stored = TobaccoBoxHelper.getStoredItem(stack);
            int storedCount = TobaccoBoxHelper.getStoredCount(stack);
            if (stored.isEmpty() || storedCount <= 0) {
                add(attributes, "box_fill", "Empty");
            } else {
                int capacity = TobaccoBoxHelper.getCapacity(stored);
                add(attributes, "box_fill", storedCount >= capacity ? "Full" : "Partially Filled");
            }
            add(attributes, "box_contents", getBoxContents(stored));

            String label = TobaccoBoxHelper.getLabel(stack);
            if (!label.isBlank()) {
                add(attributes, "box_label", label);
            }

            // Expose stored tobacco metadata for Attribute Filter sorting while retaining box-only attributes.
            if (!stored.isEmpty()) {
                collectTobaccoMetadata(stored, attributes, false);
            }
            return List.copyOf(attributes);
        }

        collectTobaccoMetadata(stack, attributes, true);
        return List.copyOf(attributes);
    }

    private static boolean isSupported(ItemStack stack) {
        return TobaccoCuringHelper.isRawTobaccoLeaf(stack)
                || TobaccoCuringHelper.isDryTobaccoLeaf(stack)
                || TobaccoCuringHelper.isLooseTobacco(stack)
                || stack.is(ModItems.CIGARETTE.get())
                || stack.is(ModItems.CIGAR.get())
                || stack.is(ModItems.SHISHA_TOBACCO.get())
                || stack.is(ModItems.TOBACCO_BOX.get());
    }

    private static void collectTobaccoMetadata(ItemStack stack, List<ItemAttribute> attributes, boolean includeProduct) {
        boolean raw = TobaccoCuringHelper.isRawTobaccoLeaf(stack);
        boolean processed = TobaccoCuringHelper.isDryTobaccoLeaf(stack) || TobaccoCuringHelper.isLooseTobacco(stack);
        boolean loose = TobaccoCuringHelper.isLooseTobacco(stack);
        boolean cigarette = stack.is(ModItems.CIGARETTE.get());
        boolean cigar = stack.is(ModItems.CIGAR.get());
        boolean shisha = stack.is(ModItems.SHISHA_TOBACCO.get());
        boolean finishedProduct = cigarette || cigar || shisha;

        if (includeProduct) {
            if (loose) add(attributes, "product", "Loose Tobacco");
            else if (cigarette) add(attributes, "product", "Cigarette");
            else if (cigar) add(attributes, "product", "Cigar");
            else if (shisha) add(attributes, "product", "Shisha");
        }

        CompoundTag directTag = LegacyItemTags.getTag(stack);
        CompoundTag tobaccoTag = finishedProduct ? TobaccoTooltipHelper.getPackedTobaccoData(stack) : directTag;
        if (tobaccoTag == null) tobaccoTag = directTag;

        List<TobaccoBlendComponent> components = tobaccoTag == null
                ? List.of()
                : TobaccoBlendHelper.getComponentData(tobaccoTag);
        boolean blended = !components.isEmpty() || stack.is(ModItems.BLENDED_TOBACCO.get());

        if (blended) {
            add(attributes, "state", "Blended");
            Set<String> varieties = new LinkedHashSet<>();
            for (TobaccoBlendComponent component : components) {
                String variety = TobaccoBlendHelper.formatVariety(component.variety());
                if (!variety.isBlank()) varieties.add(variety);
            }
            for (String variety : varieties) {
                add(attributes, "contains_variety", variety);
            }

            String blendName = tobaccoTag == null ? "" : tobaccoTag.getString(TobaccoBlendHelper.TAG_BLEND_NAME);
            if (!blendName.isBlank()) {
                add(attributes, "blend_name", blendName);
            }
        } else {
            String variety = getVariety(stack, directTag, tobaccoTag);
            if (!variety.isBlank()) {
                add(attributes, "variety", variety);
            }
        }

        if (raw) {
            int quality = TobaccoCuringHelper.getQuality(stack);
            add(attributes, "growth_quality_score", Integer.toString(quality));
            add(attributes, "growth_quality_band", qualityBand(quality, 70));
            add(attributes, "growth_quality_tier", TobaccoCuringHelper.getRawLeafTier(quality));
            if (quality >= 31) add(attributes, "growth_quality_threshold", "Good or Better");
            if (quality >= 46) add(attributes, "growth_quality_threshold", "Excellent or Better");
            if (quality >= 60) add(attributes, "growth_quality_threshold", "Perfect or Better");
        } else if (finishedProduct) {
            int productQuality = TobaccoProductQualityHelper.getStoredProductQuality(stack);
            if (productQuality >= 0) {
                addProductQuality(attributes, productQuality);
            } else if (tobaccoTag != null) {
                addTobaccoQuality(attributes, getQuality(tobaccoTag));
            }
        } else if (processed) {
            addTobaccoQuality(attributes, TobaccoCuringHelper.getQuality(stack));
        }

        if (processed) {
            String cure = TobaccoCuringHelper.getCureType(stack);
            if (!cure.isBlank()) add(attributes, "cure", TobaccoCuringHelper.getCureDisplayName(cure));
        } else if (finishedProduct) {
            String cure = getProductProcessType(directTag, tobaccoTag,
                    TobaccoProductQualityHelper.TAG_INPUT_CURE_TYPE, TobaccoCuringHelper.TAG_CURE_TYPE);
            if (!cure.isBlank()) add(attributes, "cure", TobaccoCuringHelper.getCureDisplayName(cure));
        }

        if (loose) {
            String cut = TobaccoCuringHelper.getCutType(stack);
            if (!cut.isBlank()) add(attributes, "cut", TobaccoCuringHelper.getCutDisplayName(cut));
        } else if (finishedProduct) {
            String cut = getProductProcessType(directTag, tobaccoTag,
                    TobaccoProductQualityHelper.TAG_INPUT_CUT_TYPE, TobaccoCuringHelper.TAG_CUT_TYPE);
            if (!cut.isBlank()) add(attributes, "cut", TobaccoCuringHelper.getCutDisplayName(cut));
        }

        addFlavorAttributes(stack, directTag, tobaccoTag, components, shisha, attributes);

        if (cigar && directTag != null) {
            String wrapper = findKnownVariety(directTag.getString("wrapper"));
            if (!wrapper.isBlank()) {
                add(attributes, "wrapper_variety", wrapper);
            }
        }

        addProcessStates(directTag, attributes);
        if (tobaccoTag != directTag) {
            addProcessStates(tobaccoTag, attributes);
        }
    }

    private static void addTobaccoQuality(List<ItemAttribute> attributes, int quality) {
        add(attributes, "quality_score", Integer.toString(quality));
        add(attributes, "quality_band", qualityBand(quality, 120));
        add(attributes, "quality_tier", TobaccoCuringHelper.getQualityTier(quality));
        if (quality >= 61) add(attributes, "quality_threshold", "Good or Better");
        if (quality >= 81) add(attributes, "quality_threshold", "Excellent or Better");
        if (quality >= 90) add(attributes, "quality_threshold", "Perfect or Better");
    }

    private static String qualityBand(int quality, int maximum) {
        int clamped = Math.max(0, Math.min(maximum, quality));
        if (clamped == 0) return "0";
        int lower = ((clamped - 1) / 5) * 5 + 1;
        int upper = Math.min(maximum, lower + 4);
        return lower == upper ? Integer.toString(lower) : lower + "–" + upper;
    }

    private static void addProductQuality(List<ItemAttribute> attributes, int quality10) {
        add(attributes, "quality_score", quality10 + "/10");
        String tier = switch (quality10) {
            case 0, 1, 2, 3 -> "Poor";
            case 4, 5, 6 -> "Common";
            case 7, 8 -> "Good";
            case 9 -> "Excellent";
            default -> "Perfect";
        };
        add(attributes, "quality_tier", tier);
        if (quality10 >= 7) add(attributes, "quality_threshold", "Good or Better");
        if (quality10 >= 9) add(attributes, "quality_threshold", "Excellent or Better");
        if (quality10 >= 10) add(attributes, "quality_threshold", "Perfect or Better");
    }

    private static int getQuality(CompoundTag tag) {
        if (tag == null) return 75;
        if (tag.contains(TobaccoCuringHelper.TAG_QUALITY)) {
            return TobaccoCuringHelper.clampQuality(tag.getInt(TobaccoCuringHelper.TAG_QUALITY));
        }
        if (tag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
            return TobaccoCuringHelper.clampQuality(tag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY));
        }
        return 75;
    }

    private static String getProductProcessType(CompoundTag directTag, CompoundTag tobaccoTag,
                                                String productKey, String tobaccoKey) {
        if (directTag != null) {
            String direct = directTag.getString(productKey);
            if (!direct.isBlank()) return direct;
            direct = directTag.getString(tobaccoKey);
            if (!direct.isBlank()) return direct;
        }
        return tobaccoTag == null ? "" : tobaccoTag.getString(tobaccoKey);
    }

    private static void addFlavorAttributes(ItemStack stack, CompoundTag directTag, CompoundTag tobaccoTag,
                                            List<TobaccoBlendComponent> components, boolean shisha,
                                            List<ItemAttribute> attributes) {
        LinkedHashSet<String> flavors = new LinkedHashSet<>();

        for (TobaccoBlendComponent component : components) {
            String flavor = TobaccoAromaticHelper.formatFlavorId(component.flavorId());
            if (!flavor.isBlank()) flavors.add(flavor);
        }

        if (components.isEmpty()) {
            String flavor = finishedFlavorName(stack, directTag, tobaccoTag);
            if (!flavor.isBlank()) flavors.add(flavor);
        }

        if (shisha && directTag != null) {
            for (String key : List.of("flavor1", "flavor2", "flavor3")) {
                String flavor = normalizeShishaFlavor(directTag.getString(key));
                if (!flavor.isBlank()) flavors.add(flavor);
            }
        }

        if (!flavors.isEmpty()) {
            add(attributes, "state", "Aromatic");
            for (String flavor : flavors) {
                add(attributes, "flavor", flavor);
            }
        }
    }

    private static String finishedFlavorName(ItemStack stack, CompoundTag directTag, CompoundTag tobaccoTag) {
        if (TobaccoCuringHelper.isLooseTobacco(stack)) {
            return TobaccoAromaticHelper.getFlavorName(stack);
        }

        String flavor = TobaccoAromaticHelper.getFlavorName(tobaccoTag);
        if (!flavor.isBlank()) return flavor;
        return TobaccoAromaticHelper.getFlavorName(directTag);
    }

    /** Accepts current Shisha flavor IDs and the legacy "Bottle of Molasses (...)" metadata form. */
    private static String normalizeShishaFlavor(String value) {
        if (value == null || value.isBlank()) return "";
        String trimmed = value.trim();
        int open = trimmed.indexOf('(');
        int close = trimmed.indexOf(')', open + 1);
        if (trimmed.contains("Molasses") && open >= 0 && close > open) {
            trimmed = trimmed.substring(open + 1, close).trim();
            if (trimmed.endsWith(" Flavored")) {
                trimmed = trimmed.substring(0, trimmed.length() - " Flavored".length());
            }
        }
        return trimmed;
    }

    private static void addProcessStates(CompoundTag tag, List<ItemAttribute> attributes) {
        if (tag == null) return;
        if (tag.getBoolean("Fermented")) add(attributes, "state", "Fermented");

        int agedDays = Math.max(0, tag.getInt("AgedDays"));
        if (agedDays > 0) {
            add(attributes, "state", "Aged");
            add(attributes, "age_days", Integer.toString(agedDays));
            if (agedDays >= 7) add(attributes, "age_threshold", "7");
            if (agedDays >= 30) add(attributes, "age_threshold", "30");
            if (agedDays >= 90) add(attributes, "age_threshold", "90");
            if (agedDays >= 365) add(attributes, "age_threshold", "365");
        }

        if (tag.getBoolean("Ruined")) add(attributes, "state", "Ruined");
    }

    private static String getVariety(ItemStack stack, CompoundTag directTag, CompoundTag tobaccoTag) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null && "tobacconistmod".equals(id.getNamespace())) {
            String path = id.getPath();
            String variety = "";
            if (path.startsWith("tobacco_leaf_")) {
                variety = path.substring("tobacco_leaf_".length());
                if (variety.endsWith("_dry")) {
                    variety = variety.substring(0, variety.length() - "_dry".length());
                }
            } else if (path.startsWith("tobacco_loose_") && !path.equals("tobacco_loose_blend")) {
                variety = path.substring("tobacco_loose_".length());
            }
            String formatted = formatKnownVarietyId(variety);
            if (!formatted.isBlank()) return formatted;
        }

        for (CompoundTag tag : List.of(directTag == null ? new CompoundTag() : directTag,
                tobaccoTag == null ? new CompoundTag() : tobaccoTag)) {
            String fromType = findKnownVariety(tag.getString("TobaccoType"));
            if (!fromType.isBlank()) return fromType;
            String fromLabel = findKnownVariety(tag.getString("tobacco"));
            if (!fromLabel.isBlank()) return fromLabel;
        }
        return "";
    }

    private static String findKnownVariety(String text) {
        if (text == null || text.isBlank()) return "";
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("virginia")) return "Virginia";
        if (lower.contains("burley")) return "Burley";
        if (lower.contains("oriental")) return "Oriental";
        if (lower.contains("dokha")) return "Dokha";
        if (lower.contains("shade")) return "Shade";
        if (lower.contains("wild")) return "Wild";
        return "";
    }

    private static String formatKnownVarietyId(String variety) {
        return switch (variety) {
            case "wild" -> "Wild";
            case "virginia" -> "Virginia";
            case "burley" -> "Burley";
            case "oriental" -> "Oriental";
            case "dokha" -> "Dokha";
            case "shade" -> "Shade";
            default -> "";
        };
    }

    private static String getBoxContents(ItemStack stored) {
        if (stored.isEmpty()) return "Empty";
        if (stored.is(ModItems.CIGARETTE.get())) return "Cigarettes";
        if (stored.is(ModItems.CIGAR.get())) return "Cigars";
        if (stored.is(ModItems.SHISHA_TOBACCO.get())) return "Shisha";
        if (stored.is(ModItems.BLENDED_TOBACCO.get())) return "Blend";
        if (TobaccoCuringHelper.isLooseTobacco(stored)) return "Loose Tobacco";
        return "Other";
    }

    private static void add(List<ItemAttribute> attributes, String category, String value) {
        if (value == null || value.isBlank()) return;
        CreateTobaccoItemAttribute attribute = new CreateTobaccoItemAttribute(category, value);
        if (!attributes.contains(attribute)) attributes.add(attribute);
    }

    /** Registry type that enumerates every Tobacconist attribute applicable to the examined stack. */
    public static final class Type implements ItemAttributeType {
        @Override
        public ItemAttribute createAttribute() {
            return new CreateTobaccoItemAttribute("state", "Tobacco");
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
            return collect(stack);
        }

        @Override
        public MapCodec<? extends ItemAttribute> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
