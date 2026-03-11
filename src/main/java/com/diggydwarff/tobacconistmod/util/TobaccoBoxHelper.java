package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
                || item == ModItems.TOBACCO_LOOSE_WILD.get()
                || item == ModItems.TOBACCO_LOOSE_VIRGINIA.get()
                || item == ModItems.TOBACCO_LOOSE_BURLEY.get()
                || item == ModItems.TOBACCO_LOOSE_ORIENTAL.get()
                || item == ModItems.TOBACCO_LOOSE_DOKHA.get()
                || item == ModItems.TOBACCO_LOOSE_SHADE.get();
    }

    public static int getCapacity(ItemStack content) {
        if (content.is(ModItems.CIGAR.get())) return 8;
        if (content.is(ModItems.CIGARETTE.get())) return 12;
        if (content.is(ModItems.SHISHA_TOBACCO.get())) return 16;
        return 16; // all loose tobacco variants
    }

    public static boolean hasStoredItem(ItemStack box) {
        CompoundTag tag = box.getTag();
        return tag != null && tag.contains(TAG_STORED);
    }

    public static ItemStack getStoredItem(ItemStack box) {
        CompoundTag tag = box.getTag();
        if (tag == null || !tag.contains(TAG_STORED)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound(TAG_STORED));
    }

    public static int getStoredCount(ItemStack box) {
        CompoundTag tag = box.getTag();
        return tag == null ? 0 : tag.getInt(TAG_COUNT);
    }

    public static void setStored(ItemStack box, ItemStack content, int count) {
        CompoundTag tag = box.getOrCreateTag();
        ItemStack copy = content.copy();
        copy.setCount(1);
        clearCustomProductName(copy);
        tag.put(TAG_STORED, copy.save(new CompoundTag()));
        tag.putInt(TAG_COUNT, count);
    }

    public static void clearStored(ItemStack box) {
        CompoundTag tag = box.getTag();
        if (tag == null) return;
        tag.remove(TAG_STORED);
        tag.remove(TAG_COUNT);
        if (tag.isEmpty()) {
            box.setTag(null);
        }
    }

    public static String getLabel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(TAG_LABEL);
    }

    public static void setLabel(ItemStack stack, String label) {
        if (label == null || label.isBlank()) return;
        stack.getOrCreateTag().putString(TAG_LABEL, label.trim());
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

        return ItemStack.isSameItemSameTags(aCopy, bCopy);
    }

    public static void clearCustomProductName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        tag.remove(TAG_PRODUCT_LABEL);

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
            stack.setTag(null);
        }
    }

    public static ItemStack createExtractedStack(ItemStack box) {
        ItemStack stored = getStoredItem(box);
        if (stored.isEmpty()) return ItemStack.EMPTY;

        ItemStack out = stored.copy();
        out.setCount(1);

        String label = getLabel(box);
        if (!label.isEmpty()) {
            out.getOrCreateTag().putString(TAG_PRODUCT_LABEL, label);
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
        CompoundTag tag = stack.getTag();
        if (tag == null) return "";

        StringBuilder out = new StringBuilder();

        // Label first, if present
        String label = tag.getString("ProductLabel");
        if (!label.isEmpty()) {
            out.append(label);
        }

        // These keys are examples. Replace with your real stored keys/helper calls.
        String tobaccoType = tag.getString("TobaccoType");
        String cutType = tag.getString("CutType");
        String cureType = tag.getString("CureType");
        int quality = tag.contains("Quality") ? tag.getInt("Quality") : -1;

        String qualityWord = getQualityWord(quality);
        String tobaccoWord = formatTobaccoType(tobaccoType);
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
        if (quality < 0) return "";
        if (quality >= 9) return "Premium";
        if (quality >= 7) return "Fine";
        if (quality >= 5) return "Standard";
        if (quality >= 3) return "Harsh";
        return "Low-Grade";
    }

    public static String getBlendLine(ItemStack stored) {

        if (stored.isEmpty()) return "";

        CompoundTag tag = stored.getTag();
        if (tag == null) return "";

        int quality = tag.contains("Quality") ? tag.getInt("Quality") : -1;
        int quality10 = Math.round(quality / 10f);

        String cut = tag.getString("CutType");

        // Pull the actual tobacco stack stored inside the cigarette
        ItemStack tobaccoStack = ItemStack.EMPTY;

        if (tag.contains("TobaccoStack")) {
            tobaccoStack = ItemStack.of(tag.getCompound("TobaccoStack"));
        }

        String cure = "";
        String leafType = "";

        if (!tobaccoStack.isEmpty()) {
            cure = TobaccoCuringHelper.getCureDisplayName(
                    TobaccoCuringHelper.getCureType(tobaccoStack)
            );

            leafType = tobaccoStack.getHoverName().getString()
                    .replace(" Loose Tobacco", "")
                    .replace(" Tobacco", "");
        }

        String cutDisplay = TobaccoCuringHelper.getCutDisplayName(cut);

        StringBuilder out = new StringBuilder();

        if (quality >= 0) {
            out.append(quality10).append("/10 ");
        }

        if (!cure.isEmpty()) {
            out.append(cure).append(" ");
        }

        if (!cutDisplay.isEmpty()) {
            out.append(cutDisplay).append(" ");
        }

        if (!leafType.isEmpty()) {
            out.append(leafType);
        }

        return out.toString().trim();
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
        return "Loose Tobacco";
    }

    public static String getContentSingularName(ItemStack content) {
        if (content.is(ModItems.CIGAR.get())) return "Cigar";
        if (content.is(ModItems.CIGARETTE.get())) return "Cigarette";
        if (content.is(ModItems.SHISHA_TOBACCO.get())) return "Shisha Tobacco";
        return "Loose Tobacco";
    }

    public static Component getBoxDisplayName(ItemStack box) {
        ItemStack stored = getStoredItem(box);
        if (stored.isEmpty()) {
            return Component.translatable("item.tobacconistmod.tobacco_box");
        }

        String label = getLabel(box);
        String plural = getContentPluralName(stored);

        if (!label.isEmpty()) {
            return Component.literal("Box of " + label + " " + plural);
        }

        return Component.literal("Box of " + plural);
    }
}