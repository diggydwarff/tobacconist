package com.diggydwarff.tobacconistmod.fluid;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;

/** Shared flavor palette for essence bottle art and rendered factory fluids. */
public final class EssenceFlavorColors {
    private static final int ESSENCE_ALPHA = 0x80;

    private EssenceFlavorColors() {}

    /** Dominant interior-liquid RGB used by the matching bottled_essence_<flavor>.png texture. */
    public static int bottleLiquidRgb(BottledMolassesFlavors flavor) {
        return switch (flavor) {
            case BOTTLED_MOLASSES_APPLE_FLAVOR -> 0xC2ACB0;
            case BOTTLED_MOLASSES_TWO_APPLES_FLAVOR -> 0xBEA4A8;
            case BOTTLED_MOLASSES_GOLDENAPPLE_FLAVOR -> 0xC2BAA4;
            case BOTTLED_MOLASSES_TWO_GOLDENAPPLES_FLAVOR -> 0xBEB49A;
            case BOTTLED_MOLASSES_ROYALAPPLE_FLAVOR -> 0xBCB2A8;
            case BOTTLED_MOLASSES_TWO_ROYALAPPLES_FLAVOR -> 0xB6A8A0;
            case BOTTLED_MOLASSES_MELON_FLAVOR -> 0xB2C2B4;
            case BOTTLED_MOLASSES_SWEETBERRY_FLAVOR -> 0xB8AAB6;
            case BOTTLED_MOLASSES_GLOWBERRY_FLAVOR -> 0xC0BAA6;
            case BOTTLED_MOLASSES_BERRY_FLAVOR -> 0xAEACBE;
            case BOTTLED_MOLASSES_CHORUS_FRUIT_FLAVOR -> 0xB2ACC0;
            case BOTTLED_MOLASSES_HONEY_FLAVOR -> 0xC2BAA4;
            case BOTTLED_MOLASSES_CAKE_FLAVOR -> 0xC4C8C8;
            case BOTTLED_MOLASSES_COOKIES_FLAVOR -> 0xBEBCB8;
            case BOTTLED_MOLASSES_CREAM_FLAVOR -> 0xC6CECE;
            case BOTTLED_MOLASSES_COCOA_FLAVOR -> 0xAEAAA4;
            case BOTTLED_MOLASSES_PUMPKIN_FLAVOR -> 0xC0B2A4;
            case BOTTLED_MOLASSES_SUGAR_FLAVOR -> 0xC8CED0;
            case BOTTLED_MOLASSES_CARROT_FLAVOR -> 0xC0B2A4;
            case BOTTLED_MOLASSES_APPLEPIE_FLAVOR -> 0xC0B8B6;
            case BOTTLED_MOLASSES_SWEETBERRY_CHEESECAKE_FLAVOR -> 0xBEBAC0;
            case BOTTLED_MOLASSES_CHOCOLATEPIE_FLAVOR -> 0xB6B2B0;
            case BOTTLED_MOLASSES_SWEETBERRY_COOKIE_FLAVOR -> 0xBCB6BC;
            case BOTTLED_MOLASSES_HONEYCOOKIE_FLAVOR -> 0xC0BCB4;
            case BOTTLED_MOLASSES_MELONPOPSICLE_FLAVOR -> 0xB8C4BE;
            case BOTTLED_MOLASSES_GLOWBERRY_CUSTARD_FLAVOR -> 0xC2C0B6;
            case BOTTLED_MOLASSES_PEACH_FLAVOR -> 0xC2B4B2;
            case BOTTLED_MOLASSES_PEAR_FLAVOR -> 0xB8C2B0;
            case BOTTLED_MOLASSES_MANGO_FLAVOR -> 0xC2B4A2;
            case BOTTLED_MOLASSES_LYCHEE_FLAVOR -> 0xC2B8BC;
            case BOTTLED_MOLASSES_HAWBERRY_FLAVOR -> 0xBCACB0;
            case BOTTLED_MOLASSES_ORANGE_FLAVOR -> 0xC2B4A0;
            case BOTTLED_MOLASSES_PERSIMMON_FLAVOR -> 0xC0AEA6;
            case BOTTLED_MOLASSES_BLUEBERRY_FLAVOR -> 0xA8AEC0;
            case BOTTLED_MOLASSES_LEMON_FLAVOR -> 0xC2C2A8;
            case BOTTLED_MOLASSES_HAMIMELON_FLAVOR -> 0xB6C4B6;
            case BOTTLED_MOLASSES_PINEAPPLE_FLAVOR -> 0xC2C0A6;
            case BOTTLED_MOLASSES_MANGOSTEEN_FLAVOR -> 0xB0AABC;
            case BOTTLED_MOLASSES_CRANBERRY_FLAVOR -> 0xBCA8B0;
            case BOTTLED_MOLASSES_BAYBERRY_FLAVOR -> 0xB2ACB8;
            case BOTTLED_MOLASSES_FIG_FLAVOR -> 0xB0ACB8;
            case BOTTLED_MOLASSES_KIWI_FLAVOR -> 0xAEBEAE;
            case BOTTLED_MOLASSES_DURIAN_FLAVOR -> 0xBEC0AA;
            case BOTTLED_MOLASSES_COFFEE_FLAVOR -> 0xACA49E;
            case BOTTLED_MOLASSES_VANILLA_FLAVOR -> 0xC8C6B8;
            case BOTTLED_MOLASSES_STRAWBERRY_FLAVOR -> 0xC0A8AE;
            case BOTTLED_MOLASSES_BANANA_FLAVOR -> 0xC5C1A5;
            case BOTTLED_MOLASSES_MINT_FLAVOR -> 0xAEC2B6;
            case BOTTLED_MOLASSES_LIME_FLAVOR -> 0xB6C3A8;
            case BOTTLED_MOLASSES_GRAPEFRUIT_FLAVOR -> 0xC3ADB0;
            case BOTTLED_MOLASSES_CHERRY_FLAVOR -> 0xBDA7AC;
            case BOTTLED_MOLASSES_GRAPE_FLAVOR -> 0xAEA8BE;
            case BOTTLED_MOLASSES_COCONUT_FLAVOR -> 0xC8C6BE;
            case BOTTLED_MOLASSES_BLACKBERRY_FLAVOR -> 0xAAA6BC;
            case BOTTLED_MOLASSES_RASPBERRY_FLAVOR -> 0xB9A8B2;
            case BOTTLED_MOLASSES_CINNAMON_FLAVOR -> 0xB7AAA2;
            case BOTTLED_MOLASSES_CARAMEL_FLAVOR -> 0xC0B49F;
            case BOTTLED_MOLASSES_APRICOT_FLAVOR -> 0xC2B3A8;
            case BOTTLED_MOLASSES_PLUM_FLAVOR -> 0xADA8B9;
            case BOTTLED_MOLASSES_DRAGONFRUIT_FLAVOR -> 0xBBA8B7;
            case BOTTLED_MOLASSES_MARSHMALLOW_FLAVOR -> 0xC8C7C7;
            case BOTTLED_MOLASSES_TEA_FLAVOR -> 0xB4B8A5;
            case BOTTLED_MOLASSES_HIBISCUS_FLAVOR -> 0xB9A5AE;
            case BOTTLED_MOLASSES_LAVENDER_FLAVOR -> 0xB2ADBF;
            case BOTTLED_MOLASSES_PEANUT_FLAVOR -> 0xBCB09F;
            case BOTTLED_MOLASSES_BROWNIE_FLAVOR -> 0xABA6A2;
            case BOTTLED_MOLASSES_CUSTARD_FLAVOR -> 0xC5C0AA;
            case BOTTLED_MOLASSES_PLAIN -> 0xC6D0D6;
        };
    }

    /**
     * Uses the same flavor hue as the bottle, lifted slightly so water-based fluid textures
     * stay readable in Create tanks, pipes, basins, and JEI fluid renderers.
     */
    public static int fluidTint(BottledMolassesFlavors flavor) {
        int rgb = bottleLiquidRgb(flavor);
        int r = lift((rgb >> 16) & 0xFF);
        int g = lift((rgb >> 8) & 0xFF);
        int b = lift(rgb & 0xFF);
        return (ESSENCE_ALPHA << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lift(int channel) {
        // 12.5% toward white: enough to survive fluid-texture multiplication without going neon.
        return channel + ((255 - channel) / 8);
    }
}
