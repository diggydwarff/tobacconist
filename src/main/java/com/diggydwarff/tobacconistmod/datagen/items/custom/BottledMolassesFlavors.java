package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum BottledMolassesFlavors {

    // PLAIN MOLASSES
    BOTTLED_MOLASSES_PLAIN("[Bottle of Molasses]"),

    // APPLE FLAVORS
    BOTTLED_MOLASSES_APPLE_FLAVOR("[Bottle of Molasses (Apple Flavored)]"),
    BOTTLED_MOLASSES_TWO_APPLES_FLAVOR("[Bottle of Molasses (Double Apple Flavored)]"),
    BOTTLED_MOLASSES_GOLDENAPPLE_FLAVOR("[Bottle of Molasses (Golden Apple Flavored)]"),
    BOTTLED_MOLASSES_TWO_GOLDENAPPLES_FLAVOR("[Bottle of Molasses (Double Golden Apple Flavored)]"),
    BOTTLED_MOLASSES_ROYALAPPLE_FLAVOR("[Bottle of Molasses (Royal Golden Apple Flavored)]"),
    BOTTLED_MOLASSES_TWO_ROYALAPPLES_FLAVOR("[Bottle of Molasses (Double Royal Apple Flavored)]"),

    // FRUIT FLAVORS
    BOTTLED_MOLASSES_MELON_FLAVOR("[Bottle of Molasses (Melon Flavored)]"),
    BOTTLED_MOLASSES_SWEETBERRY_FLAVOR("[Bottle of Molasses (Sweet Berry Flavored)]"),
    BOTTLED_MOLASSES_GLOWBERRY_FLAVOR("[Bottle of Molasses (Glow Berry Flavored)]"),
    BOTTLED_MOLASSES_BERRY_FLAVOR("[Bottle of Molasses (Berry Flavored)]"),
    BOTTLED_MOLASSES_CHORUS_FRUIT_FLAVOR("[Bottle of Molasses (Chorus Fruit Flavored)]"),

    // OTHER VANILLA FLAVORS
    BOTTLED_MOLASSES_HONEY_FLAVOR("[Bottle of Molasses (Honey Flavored)]"),
    BOTTLED_MOLASSES_CAKE_FLAVOR("[Bottle of Molasses (Cake Flavored)]"),
    BOTTLED_MOLASSES_COOKIES_FLAVOR("[Bottle of Molasses (Cookies Flavored)]"),
    BOTTLED_MOLASSES_CREAM_FLAVOR("[Bottle of Molasses (Cream Flavored)]"),
    BOTTLED_MOLASSES_COCOA_FLAVOR("[Bottle of Molasses (Cocoa Flavored)]"),
    BOTTLED_MOLASSES_PUMPKIN_FLAVOR("[Bottle of Molasses (Pumpkin Flavored)]"),
    BOTTLED_MOLASSES_SUGAR_FLAVOR("[Bottle of Molasses (Sugar Flavored)]"),
    BOTTLED_MOLASSES_CARROT_FLAVOR("[Bottle of Molasses (Carrot Flavored)]"),

    // FARMERS DELIGHT FLAVORS
    BOTTLED_MOLASSES_APPLEPIE_FLAVOR("[Bottle of Molasses (Apple Pie Flavored)]"),
    BOTTLED_MOLASSES_SWEETBERRY_CHEESECAKE_FLAVOR("[Bottle of Molasses (Sweet Berry Cheesecake Flavored)]"),
    BOTTLED_MOLASSES_CHOCOLATEPIE_FLAVOR("[Bottle of Molasses (Chocolate Pie Flavored)]"),
    BOTTLED_MOLASSES_SWEETBERRY_COOKIE_FLAVOR("[Bottle of Molasses (Sweet Berry Cookie Flavored)]"),
    BOTTLED_MOLASSES_HONEYCOOKIE_FLAVOR("[Bottle of Molasses (Honey Cookie Flavored)]"),
    BOTTLED_MOLASSES_MELONPOPSICLE_FLAVOR("[Bottle of Molasses (Melon Popsicle Flavored)]"),
    BOTTLED_MOLASSES_GLOWBERRY_CUSTARD_FLAVOR("[Bottle of Molasses (Glow Berry Custard Flavored)]"),

    // FRUITS DELIGHT FLAVORS
    BOTTLED_MOLASSES_PEACH_FLAVOR("[Bottle of Molasses (Peach Flavored)]"),
    BOTTLED_MOLASSES_PEAR_FLAVOR("[Bottle of Molasses (Pear Flavored)]"),
    BOTTLED_MOLASSES_MANGO_FLAVOR("[Bottle of Molasses (Mango Flavored)]"),
    BOTTLED_MOLASSES_LYCHEE_FLAVOR("[Bottle of Molasses (Lychee Flavored)]"),
    BOTTLED_MOLASSES_HAWBERRY_FLAVOR("[Bottle of Molasses (Hawberry Flavored)]"),
    BOTTLED_MOLASSES_ORANGE_FLAVOR("[Bottle of Molasses (Orange Flavored)]"),
    BOTTLED_MOLASSES_PERSIMMON_FLAVOR("[Bottle of Molasses (Persimmon Flavored)]"),
    BOTTLED_MOLASSES_BLUEBERRY_FLAVOR("[Bottle of Molasses (Blueberry Flavored)]"),
    BOTTLED_MOLASSES_LEMON_FLAVOR("[Bottle of Molasses (Lemon Flavored)]"),
    BOTTLED_MOLASSES_HAMIMELON_FLAVOR("[Bottle of Molasses (Hamimelon Flavored)]"),
    BOTTLED_MOLASSES_PINEAPPLE_FLAVOR("[Bottle of Molasses (Pineapple Flavored)]"),
    BOTTLED_MOLASSES_MANGOSTEEN_FLAVOR("[Bottle of Molasses (Mangosteen Flavored)]"),
    BOTTLED_MOLASSES_CRANBERRY_FLAVOR("[Bottle of Molasses (Cranberry Flavored)]"),
    BOTTLED_MOLASSES_BAYBERRY_FLAVOR("[Bottle of Molasses (Bayberry Flavored)]"),
    BOTTLED_MOLASSES_FIG_FLAVOR("[Bottle of Molasses (Fig Flavored)]"),
    BOTTLED_MOLASSES_KIWI_FLAVOR("[Bottle of Molasses (Kiwi Flavored)]"),
    BOTTLED_MOLASSES_DURIAN_FLAVOR("[Bottle of Molasses (Durian Flavored)]"),

    // CROSS-MOD / GENERIC FLAVOR COMPATIBILITY
    BOTTLED_MOLASSES_COFFEE_FLAVOR("[Bottle of Molasses (Coffee Flavored)]"),
    BOTTLED_MOLASSES_VANILLA_FLAVOR("[Bottle of Molasses (Vanilla Flavored)]"),
    BOTTLED_MOLASSES_STRAWBERRY_FLAVOR("[Bottle of Molasses (Strawberry Flavored)]"),
    BOTTLED_MOLASSES_BANANA_FLAVOR("[Bottle of Molasses (Banana Flavored)]"),
    BOTTLED_MOLASSES_MINT_FLAVOR("[Bottle of Molasses (Mint Flavored)]"),
    BOTTLED_MOLASSES_LIME_FLAVOR("[Bottle of Molasses (Lime Flavored)]"),
    BOTTLED_MOLASSES_GRAPEFRUIT_FLAVOR("[Bottle of Molasses (Grapefruit Flavored)]"),
    BOTTLED_MOLASSES_CHERRY_FLAVOR("[Bottle of Molasses (Cherry Flavored)]"),
    BOTTLED_MOLASSES_GRAPE_FLAVOR("[Bottle of Molasses (Grape Flavored)]"),
    BOTTLED_MOLASSES_COCONUT_FLAVOR("[Bottle of Molasses (Coconut Flavored)]"),
    BOTTLED_MOLASSES_BLACKBERRY_FLAVOR("[Bottle of Molasses (Blackberry Flavored)]"),
    BOTTLED_MOLASSES_RASPBERRY_FLAVOR("[Bottle of Molasses (Raspberry Flavored)]"),
    BOTTLED_MOLASSES_CINNAMON_FLAVOR("[Bottle of Molasses (Cinnamon Flavored)]"),
    BOTTLED_MOLASSES_CARAMEL_FLAVOR("[Bottle of Molasses (Caramel Flavored)]"),
    BOTTLED_MOLASSES_APRICOT_FLAVOR("[Bottle of Molasses (Apricot Flavored)]"),
    BOTTLED_MOLASSES_PLUM_FLAVOR("[Bottle of Molasses (Plum Flavored)]"),
    BOTTLED_MOLASSES_DRAGONFRUIT_FLAVOR("[Bottle of Molasses (Dragonfruit Flavored)]"),
    BOTTLED_MOLASSES_MARSHMALLOW_FLAVOR("[Bottle of Molasses (Marshmallow Flavored)]"),
    BOTTLED_MOLASSES_TEA_FLAVOR("[Bottle of Molasses (Tea Flavored)]"),
    BOTTLED_MOLASSES_HIBISCUS_FLAVOR("[Bottle of Molasses (Hibiscus Flavored)]"),
    BOTTLED_MOLASSES_LAVENDER_FLAVOR("[Bottle of Molasses (Lavender Flavored)]"),
    BOTTLED_MOLASSES_PEANUT_FLAVOR("[Bottle of Molasses (Peanut Flavored)]"),
    BOTTLED_MOLASSES_BROWNIE_FLAVOR("[Bottle of Molasses (Brownie Flavored)]"),
    BOTTLED_MOLASSES_CUSTARD_FLAVOR("[Bottle of Molasses (Custard Flavored)]");

    /**
     * Lazily created during the ITEM registration event.
     *
     * <p>The enum is also used as pure flavor metadata by the molasses fluid registry, which is
     * initialized before the item registry opens. Constructing Item instances from the enum
     * constructor therefore causes "Registry is already frozen" on NeoForge 1.21.1. Keep the
     * enum safe to initialize at any time and only create the actual bottle item when registration
     * asks for it.</p>
     */
    private Item item;
    private Item essenceItem;
    private final String bottleDisplayName;

    BottledMolassesFlavors(String name){
        this.bottleDisplayName = stripBrackets(name);
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT); // Locale.ROOT will ensure consistent behavior (prevent crashes) on all locales
    }

    public Item getItem(){
        if (item == null) {
            // Molasses and Essence bottles each represent a full 1000 mB processing batch.
            item = new BottledMolassesItem(new Item.Properties().stacksTo(1));
        }
        return item;
    }

    public ItemStack getStack(){
        return new ItemStack(getItem());
    }

    public boolean isPlain() {
        return this == BOTTLED_MOLASSES_PLAIN;
    }

    /** Lazily registered single-use essence item for every non-plain flavor. */
    public Item getEssenceItem() {
        if (isPlain()) {
            return null;
        }
        if (essenceItem == null) {
            essenceItem = new FlavoringEssenceItem(new Item.Properties().stacksTo(1));
        }
        return essenceItem;
    }

    public ItemStack getEssenceStack() {
        Item essence = getEssenceItem();
        return essence == null ? ItemStack.EMPTY : new ItemStack(essence);
    }

    /** Stable registry path for this molasses flavor fluid. */
    public String getFluidName() {
        String path = getName();
        if (path.startsWith("bottled_")) {
            path = path.substring("bottled_".length());
        }
        if (path.endsWith("_flavor")) {
            path = path.substring(0, path.length() - "_flavor".length());
        }
        return path;
    }

    /** English display label stored by Shisha metadata. */
    public String getShishaFlavorTag() {
        return isPlain() ? "Molasses" : getFlavorDisplayName();
    }

    /** Stable registry path for this flavor's concentrated essence fluid. */
    public String getEssenceFluidName() {
        return "essence_" + getFlavorPath();
    }

    /** Stable registry path for this flavor's essence bottle. */
    public String getEssenceItemName() {
        return "flavoring_essence_" + getFlavorPath();
    }

    public String getFlavorDisplayName() {
        int open = bottleDisplayName.indexOf('(');
        int close = bottleDisplayName.lastIndexOf(')');
        if (open < 0 || close <= open) {
            return "Plain";
        }
        String flavor = bottleDisplayName.substring(open + 1, close);
        if (flavor.endsWith(" Flavored")) {
            flavor = flavor.substring(0, flavor.length() - " Flavored".length());
        }
        return flavor;
    }

    public String getFluidDisplayName() {
        int open = bottleDisplayName.indexOf('(');
        int close = bottleDisplayName.lastIndexOf(')');
        if (open < 0 || close <= open) {
            return "Molasses";
        }

        return getFlavorDisplayName() + " Molasses";
    }

    public String getFlavorPath() {
        String fluid = getFluidName();
        return fluid.startsWith("molasses_") ? fluid.substring("molasses_".length()) : fluid;
    }

    /** Ingredient tag shared by Brewing Stand and Create essence recipes. */
    public TagKey<Item> getFlavoringIngredientTag() {
        return TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "flavorings/" + getFlavorPath())
        );
    }

    /** Composite essences are produced from an existing essence plus another flavor ingredient. */
    public boolean hasDirectEssenceInfusion() {
        return !isPlain()
                && this != BOTTLED_MOLASSES_TWO_APPLES_FLAVOR
                && this != BOTTLED_MOLASSES_TWO_GOLDENAPPLES_FLAVOR
                && this != BOTTLED_MOLASSES_TWO_ROYALAPPLES_FLAVOR
                && this != BOTTLED_MOLASSES_BERRY_FLAVOR;
    }

    public static BottledMolassesFlavors fromItem(Item item) {
        for (BottledMolassesFlavors flavor : values()) {
            if (flavor.item == item) {
                return flavor;
            }
        }
        return null;
    }

    public static BottledMolassesFlavors fromEssenceItem(Item item) {
        for (BottledMolassesFlavors flavor : values()) {
            if (!flavor.isPlain() && flavor.getEssenceItem() == item) {
                return flavor;
            }
        }
        return null;
    }

    private static String stripBrackets(String value) {
        if (value.length() >= 2 && value.charAt(0) == '[' && value.charAt(value.length() - 1) == ']') {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }


}
