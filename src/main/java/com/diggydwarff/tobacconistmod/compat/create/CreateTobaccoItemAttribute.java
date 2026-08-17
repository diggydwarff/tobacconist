package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
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
import java.util.List;

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
        String text = switch (category) {
            case "growth_quality_score" -> "Growth Quality Score: " + value;
            case "growth_quality_tier" -> "Growth Quality: " + value;
            case "growth_quality_threshold" -> "Growth Quality: " + value;
            case "quality_score" -> "Quality Score: " + value;
            case "quality_tier" -> "Quality: " + value;
            case "quality_threshold" -> "Quality: " + value;
            case "variety" -> "Variety: " + value;
            case "cure" -> "Cure: " + value;
            case "cut" -> "Cut: " + value;
            case "state" -> value;
            default -> value;
        };
        return Component.literal(inverted ? "Not " + text : text);
    }

    @Override
    public String getTranslationKey() {
        // format() is deliberately overridden so Create does not need translations in its own
        // namespace for Tobacconist-provided attribute values.
        return "tobacconist_metadata";
    }

    static List<ItemAttribute> collect(ItemStack stack) {
        if (stack.isEmpty() || (!TobaccoCuringHelper.isRawTobaccoLeaf(stack)
                && !TobaccoCuringHelper.isDryTobaccoLeaf(stack)
                && !TobaccoCuringHelper.isLooseTobacco(stack))) {
            return List.of();
        }

        ArrayList<ItemAttribute> attributes = new ArrayList<>();

        String variety = getVariety(stack);
        if (!variety.isEmpty()) {
            attributes.add(new CreateTobaccoItemAttribute("variety", variety));
        }

        int quality = TobaccoCuringHelper.getQuality(stack);
        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack)) {
            attributes.add(new CreateTobaccoItemAttribute(
                    "growth_quality_score", Integer.toString(quality)));
            attributes.add(new CreateTobaccoItemAttribute(
                    "growth_quality_tier", TobaccoCuringHelper.getRawLeafTier(quality)));
            if (quality >= 31) {
                attributes.add(new CreateTobaccoItemAttribute(
                        "growth_quality_threshold", "Good or Better"));
            }
            if (quality >= 46) {
                attributes.add(new CreateTobaccoItemAttribute(
                        "growth_quality_threshold", "Excellent or Better"));
            }
            if (quality >= 60) {
                attributes.add(new CreateTobaccoItemAttribute(
                        "growth_quality_threshold", "Perfect or Better"));
            }
        } else {
            attributes.add(new CreateTobaccoItemAttribute("quality_score", Integer.toString(quality)));
            attributes.add(new CreateTobaccoItemAttribute(
                    "quality_tier", TobaccoCuringHelper.getQualityTier(quality)));
            if (quality >= 61) {
                attributes.add(new CreateTobaccoItemAttribute("quality_threshold", "Good or Better"));
            }
            if (quality >= 81) {
                attributes.add(new CreateTobaccoItemAttribute("quality_threshold", "Excellent or Better"));
            }
            if (quality >= 90) {
                attributes.add(new CreateTobaccoItemAttribute("quality_threshold", "Perfect or Better"));
            }
        }

        if (TobaccoCuringHelper.isProcessedTobacco(stack)) {
            String cure = TobaccoCuringHelper.getCureType(stack);
            if (!cure.isEmpty()) {
                attributes.add(new CreateTobaccoItemAttribute(
                        "cure", TobaccoCuringHelper.getCureDisplayName(cure)));
            }
        }

        if (TobaccoCuringHelper.isLooseTobacco(stack)) {
            String cut = TobaccoCuringHelper.getCutType(stack);
            if (!cut.isEmpty()) {
                attributes.add(new CreateTobaccoItemAttribute(
                        "cut", TobaccoCuringHelper.getCutDisplayName(cut)));
            }
        }

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag != null) {
            if (tag.getBoolean("Fermented")) {
                attributes.add(new CreateTobaccoItemAttribute("state", "Fermented"));
            }
            if (tag.getInt("AgedDays") > 0) {
                attributes.add(new CreateTobaccoItemAttribute("state", "Aged"));
            }
            if (tag.getBoolean("Ruined")) {
                attributes.add(new CreateTobaccoItemAttribute("state", "Ruined"));
            }
        }

        return List.copyOf(attributes);
    }

    private static String getVariety(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !"tobacconistmod".equals(id.getNamespace())) {
            return "";
        }

        String path = id.getPath();
        String variety;
        if (path.startsWith("tobacco_leaf_")) {
            variety = path.substring("tobacco_leaf_".length());
            if (variety.endsWith("_dry")) {
                variety = variety.substring(0, variety.length() - "_dry".length());
            }
        } else if (path.startsWith("tobacco_loose_")) {
            variety = path.substring("tobacco_loose_".length());
        } else {
            return "";
        }

        if (variety.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(variety.charAt(0)) + variety.substring(1);
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
