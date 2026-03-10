package com.diggydwarff.tobacconistmod.block;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModPaintings {
    public static final DeferredRegister<PaintingVariant> PAINTING_VARIANTS =
            DeferredRegister.create(Registries.PAINTING_VARIANT, TobacconistMod.MODID);

    // SMALL SQUARE PAINTINGS 16x16 --------------------------------------------------------------------------------------------------------

    public static final RegistryObject<PaintingVariant> AMERICAN_TOBACCO_FIELDS_SMALL =
            PAINTING_VARIANTS.register("american_tobacco_fields_small", () -> new PaintingVariant(16, 16));


    public static final RegistryObject<PaintingVariant> CAMEL_AMERICAN_CIGARETTE =
            PAINTING_VARIANTS.register("camel_american_cigarette", () -> new PaintingVariant(16, 16));



    // SMALL TALL PAINTINGS 16x32 --------------------------------------------------------------------------------------------------------

    public static final RegistryObject<PaintingVariant> AMERICAN_LONE_COWBOY =
            PAINTING_VARIANTS.register("american_lone_cowboy", () -> new PaintingVariant(16, 32));

    public static final RegistryObject<PaintingVariant> AMERICAN_CIGARETTE =
            PAINTING_VARIANTS.register("american_cigarette", () -> new PaintingVariant(16, 32));

    public static final RegistryObject<PaintingVariant> OTTOMAN_HOOKAH =
            PAINTING_VARIANTS.register("ottoman_hookah", () -> new PaintingVariant(16, 32));



    // LARGE WIDE PAINTINGS 32x16 --------------------------------------------------------------------------------------------------------

    public static final RegistryObject<PaintingVariant> AMERICAN_COWBOY_PAIR_WIDE =
            PAINTING_VARIANTS.register("american_cowboy_pair_wide", () -> new PaintingVariant(32, 16));

    public static final RegistryObject<PaintingVariant> PEACE_PIPE =
            PAINTING_VARIANTS.register("peace_pipe", () -> new PaintingVariant(32, 16));

    public static final RegistryObject<PaintingVariant> JAPANESE_KISERU_WIDE =
            PAINTING_VARIANTS.register("japanese_kiseru_wide", () -> new PaintingVariant(32, 16));

    public static final RegistryObject<PaintingVariant> AMERICAN_TOBACCO_FIELDS_WIDE =
            PAINTING_VARIANTS.register("american_tobacco_fields_wide", () -> new PaintingVariant(32, 16));



    // EXTRA LARGE PAINTINGS 64x32 --------------------------------------------------------------------------------------------------------

    public static final RegistryObject<PaintingVariant> AMERICAN_TOBACCO_FIELDS =
            PAINTING_VARIANTS.register("american_tobacco_fields", () -> new PaintingVariant(64, 32));

    public static final RegistryObject<PaintingVariant> JAPANESE_KISERU =
            PAINTING_VARIANTS.register("japanese_kiseru", () -> new PaintingVariant(64, 32));

    public static final RegistryObject<PaintingVariant> ARABIAN_NIGHTS =
            PAINTING_VARIANTS.register("arabian_nights", () -> new PaintingVariant(64, 32));

    public static final RegistryObject<PaintingVariant> AMERICAN_COWBOY_PAIR =
            PAINTING_VARIANTS.register("american_cowboy_pair", () -> new PaintingVariant(64, 32));



    // SUPER LARGE PAINTINGS 64x48 --------------------------------------------------------------------------------------------------------

    public static final RegistryObject<PaintingVariant> HAVANA_CIGAR =
            PAINTING_VARIANTS.register("havana_cigar", () -> new PaintingVariant(64, 48));

    public static final RegistryObject<PaintingVariant> ANDEAN_MAPACHO =
            PAINTING_VARIANTS.register("andean_mapacho", () -> new PaintingVariant(64, 48));
}
