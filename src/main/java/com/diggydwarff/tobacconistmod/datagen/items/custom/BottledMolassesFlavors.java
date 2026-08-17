package com.diggydwarff.tobacconistmod.datagen.items.custom;

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
    BOTTLED_MOLASSES_DURIAN_FLAVOR("[Bottle of Molasses (Durian Flavored)]");

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
    private final String bottleDisplayName;

    BottledMolassesFlavors(String name){
        this.bottleDisplayName = stripBrackets(name);
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT); // Locale.ROOT will ensure consistent behavior (prevent crashes) on all locales
    }

    public Item getItem(){
        if (item == null) {
            item = new ShishaFlavoringItem(new Item.Properties().stacksTo(1).durability(4));
        }
        return item;
    }

    public ItemStack getStack(){
        return new ItemStack(getItem());
    }

    /** Stable registry path for this molasses flavor's real fluid. */
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

    /** English display label stored by the existing Shisha metadata format. */
    public String getShishaFlavorTag() {
        return bottleDisplayName;
    }

    public String getFluidDisplayName() {
        int open = bottleDisplayName.indexOf('(');
        int close = bottleDisplayName.lastIndexOf(')');
        if (open < 0 || close <= open) {
            return "Molasses";
        }

        String flavor = bottleDisplayName.substring(open + 1, close);
        if (flavor.endsWith(" Flavored")) {
            flavor = flavor.substring(0, flavor.length() - " Flavored".length());
        }
        return flavor + " Molasses";
    }

    public static BottledMolassesFlavors fromItem(Item item) {
        for (BottledMolassesFlavors flavor : values()) {
            if (flavor.item == item) {
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
