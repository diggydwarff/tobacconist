package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.ModTags;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TobaccoBoxHelper {

    public static final String TAG_STORED = "StoredItem";
    public static final String TAG_COUNT = "StoredCount";
    public static final String TAG_LABEL = "BoxLabel";
    public static final String TAG_PRODUCT_LABEL = "ProductLabel";

    public static boolean isSupportedContent(ItemStack stack) {
        Item item = stack.getItem();
        return item == ModItems.CIGAR.get()
                || item == ModItems.CIGARETTE.get()
                || item == ModItems.SHISHA_TOBACCO.get()
                || stack.is(ModTags.Items.LOOSE_TOBACCO);
    }

    public static int getCapacity(ItemStack content) {
        if (content.is(ModItems.CIGAR.get())) return 8;
        if (content.is(ModItems.CIGARETTE.get())) return 12;
        if (content.is(ModItems.SHISHA_TOBACCO.get())) return 16;
        return 16; // all loose tobacco variants
    }

    public static boolean hasStoredItem(ItemStack box) {
        CompoundTag tag = LegacyItemTags.getTag(box);
        return tag != null && tag.contains(TAG_STORED);
    }

    public static ItemStack getStoredItem(ItemStack box) {
        CompoundTag tag = LegacyItemTags.getTag(box);
        if (tag == null || !tag.contains(TAG_STORED)) {
            return ItemStack.EMPTY;
        }
        return readStoredStack(tag.getCompound(TAG_STORED));
    }

    public static int getStoredCount(ItemStack box) {
        CompoundTag tag = LegacyItemTags.getTag(box);
        return tag == null ? 0 : tag.getInt(TAG_COUNT);
    }

    public static void setStored(ItemStack box, ItemStack content, int count) {
        CompoundTag tag = LegacyItemTags.getOrCreateTag(box);
        ItemStack copy = content.copy();
        copy.setCount(1);
        clearCustomProductName(copy);
        tag.put(TAG_STORED, writeStoredStack(copy));
        tag.putInt(TAG_COUNT, count);
    }

    /**
     * The 1.21 ItemStack NBT codec requires registry access. Tobacco boxes are item-only
     * helpers with no Level/RegistryAccess parameter, so persist the small subset this mod
     * actually needs: registry id, damage and custom tobacco data. The reader also accepts
     * the 1.20.1 ItemStack compound layout for world migration.
     */
    private static CompoundTag writeStoredStack(ItemStack stack) {
        CompoundTag stored = new CompoundTag();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        stored.putString("id", id.toString());
        if (stack.getDamageValue() > 0) {
            stored.putInt("Damage", stack.getDamageValue());
        }
        CompoundTag custom = LegacyItemTags.getTag(stack);
        if (custom != null && !custom.isEmpty()) {
            stored.put("CustomData", custom.copy());
        }
        return stored;
    }

    private static ItemStack readStoredStack(CompoundTag stored) {
        if (!stored.contains("id")) return ItemStack.EMPTY;

        ResourceLocation id = ResourceLocation.tryParse(stored.getString("id"));
        if (id == null) return ItemStack.EMPTY;

        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == net.minecraft.world.item.Items.AIR) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(item);

        // New 1.21 helper layout.
        if (stored.contains("CustomData")) {
            LegacyItemTags.setTag(result, stored.getCompound("CustomData").copy());
        }

        // Old 1.20.1 ItemStack layout: {id, Count, tag:{...}}.
        if (stored.contains("tag")) {
            CompoundTag legacy = stored.getCompound("tag").copy();
            if (legacy.contains("Damage")) {
                result.setDamageValue(legacy.getInt("Damage"));
                legacy.remove("Damage");
            }
            if (!legacy.isEmpty()) {
                LegacyItemTags.setTag(result, legacy);
            }
        }

        if (stored.contains("Damage")) {
            result.setDamageValue(stored.getInt("Damage"));
        }
        return result;
    }

    public static void clearStored(ItemStack box) {
        CompoundTag tag = LegacyItemTags.getTag(box);
        if (tag == null) return;

        tag.remove(TAG_STORED);
        tag.remove(TAG_COUNT);
        tag.remove(TAG_LABEL);

        if (tag.isEmpty()) {
            LegacyItemTags.setTag(box, null);
        } else {
            LegacyItemTags.setTag(box, tag);
        }
    }

    public static String getLabel(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        return tag == null ? "" : tag.getString(TAG_LABEL);
    }

    public static void setLabel(ItemStack stack, String label) {
        if (label == null || label.isBlank()) return;
        LegacyItemTags.getOrCreateTag(stack).putString(TAG_LABEL, label.trim());
    }

    public static boolean canAccept(ItemStack box, ItemStack incoming) {
        if (!isSupportedContent(incoming)) return false;

        if (!hasStoredItem(box)) return true;

        ItemStack stored = getStoredItem(box);
        return sameContent(stored, incoming);
    }

    public static boolean sameContent(ItemStack a, ItemStack b) {
        if (a.getItem() != b.getItem()) return false;

        ItemStack aCopy = a.copy();
        ItemStack bCopy = b.copy();
        clearCustomProductName(aCopy);
        clearCustomProductName(bCopy);

        return ItemStack.isSameItemSameComponents(aCopy, bCopy);
    }

    /**
     * Box compatibility is based on the tobacco/product itself, not branding. Strip both the
     * Tobacconist ProductLabel and vanilla 1.21 custom names before comparing stacks. Also
     * remove legacy ProductLabel data so named aromatic or blended products remain compatible
     * with their stored batch.
     */
    public static void clearCustomProductName(ItemStack stack) {
        stack.resetHoverName();

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null) return;

        tag.remove(TAG_PRODUCT_LABEL);

        if (tag.contains("PackedTobaccoData")) {
            CompoundTag packed = tag.getCompound("PackedTobaccoData");
            packed.remove(TAG_PRODUCT_LABEL);
            if (packed.contains("display")) {
                CompoundTag packedDisplay = packed.getCompound("display");
                packedDisplay.remove("Name");
                if (packedDisplay.isEmpty()) packed.remove("display");
                else packed.put("display", packedDisplay);
            }
            tag.put("PackedTobaccoData", packed);
        }

        if (tag.contains("display")) {
            CompoundTag display = tag.getCompound("display");
            display.remove("Name");
            if (display.isEmpty()) {
                tag.remove("display");
            } else {
                tag.put("display", display);
            }
        }

        if (tag.isEmpty()) {
            LegacyItemTags.setTag(stack, null);
        } else {
            LegacyItemTags.setTag(stack, tag);
        }
    }

    public static ItemStack createExtractedStack(ItemStack box) {
        ItemStack stored = getStoredItem(box);
        if (stored.isEmpty()) return ItemStack.EMPTY;

        ItemStack out = stored.copy();
        out.setCount(1);

        String label = getLabel(box);
        if (!label.isEmpty()) {
            LegacyItemTags.getOrCreateTag(out).putString(TAG_PRODUCT_LABEL, label);
        }

        return out;
    }

    public static String getDetailedContentName(ItemStack stored) {
        if (stored.isEmpty()) return "Empty";

        // If the item already has a specific non-generic name, use it.
        String hoverName = stored.getHoverName().getString();

        if (!hoverName.equals("Cigarette") && !hoverName.equals("Cigar")) {
            return pluralizeContentName(hoverName, stored);
        }

        // Cigarettes / cigars are generic by base item name, so build detail from NBT.
        String detail = getProductDescriptor(stored);

        if (stored.is(ModItems.CIGARETTE.get())) {
            return detail.isEmpty() ? "Cigarettes" : detail + " Cigarettes";
        }

        if (stored.is(ModItems.CIGAR.get())) {
            return detail.isEmpty() ? "Cigars" : detail + " Cigars";
        }

        if (stored.is(ModItems.SHISHA_TOBACCO.get())) {
            return detail.isEmpty() ? "Shisha Tobacco" : detail + " Shisha Tobacco";
        }

        return pluralizeContentName(hoverName, stored);
    }

    private static String pluralizeContentName(String name, ItemStack stored) {
        if (stored.is(ModItems.CIGARETTE.get()) && name.endsWith("Cigarette")) {
            return name.substring(0, name.length() - "Cigarette".length()) + "Cigarettes";
        }

        if (stored.is(ModItems.CIGAR.get()) && name.endsWith("Cigar")) {
            return name.substring(0, name.length() - "Cigar".length()) + "Cigars";
        }

        return name;
    }

    private static String getProductDescriptor(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null) return "";

        StringBuilder out = new StringBuilder();

        // Product labels are displayed before processing descriptors.
        String label = tag.getString("ProductLabel");
        if (!label.isEmpty()) {
            out.append(label);
        }


        String cutType = tag.getString("CutType");
        String cureType = tag.getString("CureType");
        int quality = getDisplayQuality100(stack);

        String qualityWord = getQualityWord(quality);
        String tobaccoWord = resolveTobaccoTypeDisplay(stack);
        String cutWord = formatCutType(cutType);
        String cureWord = formatCureType(cureType);

        if (!qualityWord.isEmpty()) {
            appendWord(out, qualityWord);
        }

        if (!cutWord.isEmpty()) {
            appendWord(out, cutWord);
        }

        if (!cureWord.isEmpty()) {
            appendWord(out, cureWord);
        }

        if (!tobaccoWord.isEmpty()) {
            appendWord(out, tobaccoWord);
        }

        return out.toString().trim();
    }

    private static void appendWord(StringBuilder out, String word) {
        if (word == null || word.isBlank()) return;
        if (!out.isEmpty()) out.append(" ");
        out.append(word.trim());
    }

    private static String getQualityWord(int quality) {
        if (!TobacconistConfig.isQualitySystemEnabled() || quality < 0) return "";
        return TobaccoTooltipHelper.getQualityWord(quality);
    }

    public static String getBlendLine(ItemStack stored) {
        if (stored.isEmpty()) return "";

        int quality10 = getDisplayQuality10(stored);

        String cut = TobaccoCuringHelper.getCutDisplayName(TobaccoCuringHelper.getCutType(stored));
        String cure = TobaccoCuringHelper.getCureDisplayName(TobaccoCuringHelper.getCureType(stored));
        String tobaccoType = resolveTobaccoTypeDisplay(stored);

        StringBuilder out = new StringBuilder();

        if (TobacconistConfig.isQualitySystemEnabled() && quality10 >= 0) {
            out.append(quality10).append("/10");
        }

        if (!cut.isEmpty() && !cut.equals("Uncut")) {
            if (!out.isEmpty()) out.append(" ");
            out.append(cut);
        }

        if (!cure.isEmpty()) {
            if (!out.isEmpty()) out.append(" ");
            out.append(cure);
        }

        if (!tobaccoType.isEmpty()) {
            if (!out.isEmpty()) out.append(" ");
            out.append(tobaccoType);
        }

        return out.toString();
    }

    private static String resolveTobaccoTypeDisplay(ItemStack stack) {
        if (stack.is(ModItems.BLENDED_TOBACCO.get())) {
            String intrinsicName = TobaccoBlendHelper.getIntrinsicBlendName(stack);
            if (!intrinsicName.isEmpty()) return intrinsicName;

            java.util.List<String> components = TobaccoBlendHelper.getComponents(stack);
            if (!components.isEmpty()) {
                return components.stream()
                        .map(TobaccoBlendHelper::formatVariety)
                        .reduce((a, b) -> a + "/" + b)
                        .orElse("") + " Blend";
            }
            return "Blend";
        }

        String raw = resolveTobaccoTypeId(stack);
        return formatTobaccoType(raw);
    }

    private static String resolveTobaccoTypeId(ItemStack stack) {
        if (stack.isEmpty()) return "";

        CompoundTag tag = LegacyItemTags.getTag(stack);

        if (tag != null) {
            String direct = normalizeTobaccoType(tag.getString("TobaccoType"));
            if (!direct.isEmpty()) return direct;

            if (tag.contains("PackedTobaccoData")) {
                CompoundTag packed = tag.getCompound("PackedTobaccoData");
                String packedType = normalizeTobaccoType(packed.getString("TobaccoType"));
                if (!packedType.isEmpty()) return packedType;
            }

            if (tag.contains("TobaccoStack")) {
                CompoundTag nestedStack = tag.getCompound("TobaccoStack");
                CompoundTag nestedTag = nestedStack.contains("tag") ? nestedStack.getCompound("tag") : nestedStack;
                String nestedType = normalizeTobaccoType(nestedTag.getString("TobaccoType"));
                if (!nestedType.isEmpty()) return nestedType;
                if (nestedTag.contains("PackedTobaccoData")) {
                    String packedType = normalizeTobaccoType(nestedTag.getCompound("PackedTobaccoData").getString("TobaccoType"));
                    if (!packedType.isEmpty()) return packedType;
                }
            }

            String tobaccoLabel = normalizeTobaccoType(tag.getString("tobacco"));
            if (!tobaccoLabel.isEmpty()) return tobaccoLabel;
        }

        if (stack.is(ModItems.TOBACCO_LOOSE_WILD.get()) || stack.is(ModItems.WILD_TOBACCO_LEAF.get()) || stack.is(ModItems.WILD_TOBACCO_LEAF_DRY.get())) {
            return "wild";
        }
        if (stack.is(ModItems.TOBACCO_LOOSE_VIRGINIA.get()) || stack.is(ModItems.VIRGINIA_TOBACCO_LEAF.get()) || stack.is(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get())) {
            return "virginia";
        }
        if (stack.is(ModItems.TOBACCO_LOOSE_BURLEY.get()) || stack.is(ModItems.BURLEY_TOBACCO_LEAF.get()) || stack.is(ModItems.BURLEY_TOBACCO_LEAF_DRY.get())) {
            return "burley";
        }
        if (stack.is(ModItems.TOBACCO_LOOSE_ORIENTAL.get()) || stack.is(ModItems.ORIENTAL_TOBACCO_LEAF.get()) || stack.is(ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get())) {
            return "oriental";
        }
        if (stack.is(ModItems.TOBACCO_LOOSE_DOKHA.get()) || stack.is(ModItems.DOKHA_TOBACCO_LEAF.get()) || stack.is(ModItems.DOKHA_TOBACCO_LEAF_DRY.get())) {
            return "dokha";
        }
        if (stack.is(ModItems.TOBACCO_LOOSE_SHADE.get()) || stack.is(ModItems.SHADE_TOBACCO_LEAF.get()) || stack.is(ModItems.SHADE_TOBACCO_LEAF_DRY.get())) {
            return "shade";
        }

        return "";
    }

    private static String normalizeTobaccoType(String value) {
        if (value == null || value.isBlank()) return "";

        String s = value.toLowerCase();

        if (s.contains("virginia")) return "virginia";
        if (s.contains("burley")) return "burley";
        if (s.contains("oriental")) return "oriental";
        if (s.contains("dokha")) return "dokha";
        if (s.contains("shade")) return "shade";
        if (s.contains("wild")) return "wild";

        return "";
    }

    public static String toSuperscriptNumber(int num) {
        StringBuilder out = new StringBuilder();
        for (char c : String.valueOf(num).toCharArray()) {
            out.append(switch (c) {
                case '0' -> "⁰";
                case '1' -> "¹";
                case '2' -> "²";
                case '3' -> "³";
                case '4' -> "⁴";
                case '5' -> "⁵";
                case '6' -> "⁶";
                case '7' -> "⁷";
                case '8' -> "⁸";
                case '9' -> "⁹";
                default -> "";
            });
        }
        return out.toString();
    }


    /** Product displays use the same final 1-10 score shown on the item itself. */
    private static int getDisplayQuality10(ItemStack stack) {
        int productQuality = TobaccoProductQualityHelper.getStoredProductQuality(stack);
        if (productQuality >= 0) return productQuality;
        int quality100 = TobaccoCuringHelper.getQuality(stack);
        return quality100 >= 0 ? Math.max(1, Math.round(quality100 / 10.0f)) : -1;
    }

    private static int getDisplayQuality100(ItemStack stack) {
        int productQuality = TobaccoProductQualityHelper.getStoredProductQuality(stack);
        if (productQuality >= 0) return productQuality * 10;

        CompoundTag tag = LegacyItemTags.getTag(stack);
        CompoundTag packed = TobaccoTooltipHelper.getPackedTobaccoData(stack);
        if (packed != null && packed.contains(TobaccoCuringHelper.TAG_QUALITY)) {
            return TobaccoCuringHelper.clampQuality(packed.getInt(TobaccoCuringHelper.TAG_QUALITY));
        }
        if (tag != null && tag.contains(TobaccoCuringHelper.TAG_QUALITY)) {
            return TobaccoCuringHelper.clampQuality(tag.getInt(TobaccoCuringHelper.TAG_QUALITY));
        }
        return TobaccoCuringHelper.getQuality(stack);
    }

    public static Component getBoxContentsComponent(ItemStack stored) {
        if (stored.isEmpty()) return Component.translatable("tobacconistmod.ui.empty");

        int quality100 = getDisplayQuality100(stored);

        MutableComponent out = Component.empty();
        if (TobacconistConfig.isQualitySystemEnabled()) {
            out.append(TobaccoText.qualityDescriptor(quality100)).append(" ");
        }
        return out.append(getContentPluralComponent(stored));
    }

    public static Component getBlendComponent(ItemStack stored) {
        if (stored.isEmpty()) return Component.empty();

        int quality10 = getDisplayQuality10(stored);
        String cut = TobaccoCuringHelper.getCutType(stored);
        String cure = TobaccoCuringHelper.getCureType(stored);
        MutableComponent out = Component.empty();

        if (TobacconistConfig.isQualitySystemEnabled() && quality10 >= 0) {
            out.append(Component.literal(quality10 + "/10"));
        }
        if (!cut.isEmpty()) {
            if (!out.getString().isEmpty()) out.append(" ");
            out.append(TobaccoText.cut(cut));
        }
        if (!cure.isEmpty()) {
            if (!out.getString().isEmpty()) out.append(" ");
            out.append(TobaccoText.cure(cure));
        }

        String typeId = resolveTobaccoTypeId(stored);
        if (!typeId.isEmpty()) {
            if (!out.getString().isEmpty()) out.append(" ");
            out.append(TobaccoText.variety(typeId));
        } else if (stored.is(ModItems.BLENDED_TOBACCO.get())) {
            String intrinsicName = TobaccoBlendHelper.getIntrinsicBlendName(stored);
            if (!out.getString().isEmpty()) out.append(" ");
            if (!intrinsicName.isEmpty()) out.append(Component.literal(intrinsicName));
            else out.append(Component.translatable("tobacconistmod.content.blend"));
        }

        return out;
    }

    public static Component getContentPluralComponent(ItemStack content) {
        if (content.is(ModItems.CIGAR.get())) return Component.translatable("tobacconistmod.content.cigars");
        if (content.is(ModItems.CIGARETTE.get())) return Component.translatable("tobacconistmod.content.cigarettes");
        if (content.is(ModItems.SHISHA_TOBACCO.get())) return Component.translatable("tobacconistmod.content.shisha_tobacco");
        if (content.is(ModItems.BLENDED_TOBACCO.get())) return Component.translatable("tobacconistmod.content.blended_tobacco");
        return Component.translatable("tobacconistmod.content.loose_tobacco");
    }

    public static String getBoxContentsLine(ItemStack stored) {
        if (stored.isEmpty()) return "Empty";

        int quality100 = getDisplayQuality100(stored);

        String qualityWord = TobaccoTooltipHelper.getQualityWord(quality100);
        String typeName = getContentPluralName(stored);

        return (qualityWord + " " + typeName).trim();
    }

    public static String getProcessSuffix(boolean fermented, int monthsAged) {
        StringBuilder out = new StringBuilder();

        if (fermented) {
            out.append(" ✿");
        }

        String age = getSuperscriptAge(monthsAged);
        if (!age.isEmpty()) {
            out.append(" ").append(age);
        }

        return out.toString();
    }

    public static String getSuperscriptAge(int monthsAged) {
        if (monthsAged <= 0) return "";

        if (monthsAged < 12) {
            return toSuperscriptNumber(monthsAged) + "ᵐ";
        }

        if (monthsAged % 12 == 0) {
            return toSuperscriptNumber(monthsAged / 12) + "ʸ";
        }

        int years = monthsAged / 12;
        int months = monthsAged % 12;
        return toSuperscriptNumber(years) + "ʸ" + toSuperscriptNumber(months) + "ᵐ";
    }

    private static String formatTobaccoType(String type) {
        return switch (type) {
            case "wild" -> "Wild";
            case "virginia" -> "Virginia";
            case "burley" -> "Burley";
            case "oriental" -> "Oriental";
            case "dokha" -> "Dokha";
            case "shade" -> "Shade";
            default -> "";
        };
    }

    private static String formatCutType(String type) {
        return switch (type) {
            case "ribbon" -> "Ribbon Cut";
            case "shag" -> "Shag Cut";
            case "fine" -> "Fine Cut";
            case "flake" -> "Flake Cut";
            case "plug" -> "Plug Cut";
            default -> "";
        };
    }

    private static String formatCureType(String type) {
        return switch (type) {
            case "air_cured" -> "Air-Cured";
            case "fire_cured" -> "Fire-Cured";
            case "flue_cured" -> "Flue-Cured";
            case "sun_cured" -> "Sun-Cured";
            default -> "";
        };
    }

    public static String getContentPluralName(ItemStack content) {
        if (content.is(ModItems.CIGAR.get())) return "Cigars";
        if (content.is(ModItems.CIGARETTE.get())) return "Cigarettes";
        if (content.is(ModItems.SHISHA_TOBACCO.get())) return "Shisha Tobacco";
        if (content.is(ModItems.BLENDED_TOBACCO.get())) return "Blended Tobacco";
        return "Loose Tobacco";
    }

    public static String getContentSingularName(ItemStack content) {
        if (content.is(ModItems.CIGAR.get())) return "Cigar";
        if (content.is(ModItems.CIGARETTE.get())) return "Cigarette";
        if (content.is(ModItems.SHISHA_TOBACCO.get())) return "Shisha Tobacco";
        if (content.is(ModItems.BLENDED_TOBACCO.get())) return "Blended Tobacco";
        return "Loose Tobacco";
    }

    public static Component getBoxDisplayName(ItemStack box) {
        ItemStack stored = getStoredItem(box);
        if (stored.isEmpty()) {
            return Component.translatable("item.tobacconistmod.tobacco_box");
        }

        String label = getLabel(box);
        Component plural = getContentPluralComponent(stored);

        if (!label.isEmpty()) {
            return Component.translatable("tobacconistmod.box.named", label, plural);
        }

        return Component.translatable("tobacconistmod.box.of", plural);
    }
}