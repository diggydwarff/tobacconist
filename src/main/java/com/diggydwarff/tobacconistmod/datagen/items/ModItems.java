package com.diggydwarff.tobacconistmod.datagen.items;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.*;
import com.diggydwarff.tobacconistmod.datagen.items.custom.*;
import com.diggydwarff.tobacconistmod.datagen.items.custom.pipeitems.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TobacconistMod.MODID);

    // PIPE ITEMS
    public static final RegistryObject<Item> WOODEN_SMOKING_PIPE = ITEMS.register("wooden_smoking_pipe", () -> new WoodenSmokingPipeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GOLD_SMOKING_PIPE =
            ITEMS.register("gold_smoking_pipe",
                    () -> new GoldSmokingPipeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> IRON_SMOKING_PIPE =
            ITEMS.register("iron_smoking_pipe",
                    () -> new IronSmokingPipeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COPPER_SMOKING_PIPE =
            ITEMS.register("copper_smoking_pipe",
                    () -> new CopperSmokingPipeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GEM_ENCRUSTED_SMOKING_PIPE =
            ITEMS.register("gem_encrusted_smoking_pipe",
                    () -> new GemEncrustedSmokingPipeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DIAMOND_ENCRUSTED_SMOKING_PIPE =
            ITEMS.register("diamond_encrusted_smoking_pipe",
                    () -> new DiamondEncrustedSmokingPipeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LAPIS_ENCRUSTED_SMOKING_PIPE =
            ITEMS.register("lapis_encrusted_smoking_pipe",
                    () -> new LapisEncrustedSmokingPipeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> EMERALD_ENCRUSTED_SMOKING_PIPE =
            ITEMS.register("emerald_encrusted_smoking_pipe",
                    () -> new EmeraldEncrustedSmokingPipeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> EMERALD_AZTEC_SMOKING_PIPE =
            ITEMS.register("emerald_aztec_smoking_pipe",
                    () -> new EmeraldAztecSmokingPipeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> NETHERITE_SMOKING_PIPE =
            ITEMS.register("netherite_smoking_pipe",
                    () -> new NetheriteSmokingPipeItem(new Item.Properties().stacksTo(1)));

    // CHAVETA TOOLS
    public static final RegistryObject<Item> STONE_CHAVETA =
            ITEMS.register("stone_chaveta",
                    () -> new ChavetaItem(new Item.Properties().durability(131)));

    public static final RegistryObject<Item> COPPER_CHAVETA =
            ITEMS.register("copper_chaveta",
                    () -> new ChavetaItem(new Item.Properties().durability(180)));

    public static final RegistryObject<Item> IRON_CHAVETA =
            ITEMS.register("iron_chaveta",
                    () -> new ChavetaItem(new Item.Properties().durability(250)));

    public static final RegistryObject<Item> GOLD_CHAVETA =
            ITEMS.register("gold_chaveta",
                    () -> new ChavetaItem(new Item.Properties().durability(32)));

    public static final RegistryObject<Item> DIAMOND_CHAVETA =
            ITEMS.register("diamond_chaveta",
                    () -> new ChavetaItem(new Item.Properties().durability(1561)));

    public static final RegistryObject<Item> NETHERITE_CHAVETA =
            ITEMS.register("netherite_chaveta",
                    () -> new ChavetaItem(new Item.Properties().durability(2031)));


    // OTHER ITEMS

    public static final RegistryObject<Item> ROLLING_PAPER = ITEMS.register("rolling_paper", () -> new RollingPaperItem(new Item.Properties()));

    public static final RegistryObject<Item> BAMBOO_CHARCOAL = ITEMS.register("bamboo_charcoal", () -> new BambooCharcoalItem(new Item.Properties()));


    public static final RegistryObject<Item> CIGAR = ITEMS.register("cigar",
            () -> new CigarItem(new Item.Properties().durability(100)));

    public static final RegistryObject<Item> CIGARETTE = ITEMS.register("cigarette",
            () -> new CigaretteItem(new Item.Properties().durability(15)));

    public static final RegistryObject<Item> HOOKAH_HOSE = ITEMS.register("hookah_hose", () -> new HookahHoseItem(new Item.Properties()));

    public static final RegistryObject<Item> SHISHA_TOBACCO = ITEMS.register("shisha_tobacco", () -> new ShishaTobaccoItem(new Item.Properties().durability(5000)));

    // TOBACCO LEAF ITEMS (wet)
    public static final RegistryObject<Item> WILD_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_wild", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> VIRGINIA_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_virginia", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> BURLEY_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_burley", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> ORIENTAL_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_oriental", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> DOKHA_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_dokha", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> SHADE_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_shade", () -> new TobaccoLeafItem(new Item.Properties()));


    // TOBACCO LEAF ITEMS (dry)
    public static final RegistryObject<Item> WILD_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_wild_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> VIRGINIA_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_virginia_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> BURLEY_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_burley_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> ORIENTAL_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_oriental_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> DOKHA_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_dokha_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> SHADE_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_shade_dry", () -> new TobaccoLeafItem(new Item.Properties()));

    public static final RegistryObject<Item> TOBACCO_LOOSE_WILD =
            ITEMS.register("tobacco_loose_wild", () -> new LooseTobaccoItem(new Item.Properties(), 40, 5));

    public static final RegistryObject<Item> TOBACCO_LOOSE_VIRGINIA =
            ITEMS.register("tobacco_loose_virginia", () -> new LooseTobaccoItem(new Item.Properties(), 40, 10));

    public static final RegistryObject<Item> TOBACCO_LOOSE_BURLEY =
            ITEMS.register("tobacco_loose_burley", () -> new LooseTobaccoItem(new Item.Properties(), 40, 15));

    public static final RegistryObject<Item> TOBACCO_LOOSE_ORIENTAL =
            ITEMS.register("tobacco_loose_oriental", () -> new LooseTobaccoItem(new Item.Properties(), 40, 15));

    public static final RegistryObject<Item> TOBACCO_LOOSE_DOKHA =
            ITEMS.register("tobacco_loose_dokha", () -> new LooseTobaccoItem(new Item.Properties(), 40, 20));
 
    public static final RegistryObject<Item> TOBACCO_LOOSE_SHADE =
            ITEMS.register("tobacco_loose_shade", () -> new LooseTobaccoItem(new Item.Properties(), 40, 10));

    public static final RegistryObject<Item> BOTTLED_MOLASSES_PLAIN = ITEMS.register("bottled_molasses_plain", () -> BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getItem());

    // TOBACCO SEED ITEMS
    public static final RegistryObject<Item> WILD_TOBACCO_SEEDS =
            ITEMS.register("wild_tobacco_seeds",
                    () -> new TallCropSeedsItem(
                            ModBlocks.WILD_TOBACCO_CROP.get(),
                            new Item.Properties(),
                            WildCropBlock.HALF
                    ));

    public static final RegistryObject<Item> VIRGINIA_TOBACCO_SEEDS =
            ITEMS.register("virginia_tobacco_seeds",
                    () -> new TallCropSeedsItem(
                            ModBlocks.VIRGINIA_TOBACCO_CROP.get(),
                            new Item.Properties(),
                            VirginiaCropBlock.HALF
                    ));

    public static final RegistryObject<Item> BURLEY_TOBACCO_SEEDS =
            ITEMS.register("burley_tobacco_seeds",
                    () -> new TallCropSeedsItem(
                            ModBlocks.BURLEY_TOBACCO_CROP.get(),
                            new Item.Properties(),
                            BurleyCropBlock.HALF
                    ));

    public static final RegistryObject<Item> ORIENTAL_TOBACCO_SEEDS =
            ITEMS.register("oriental_tobacco_seeds",
                    () -> new TallCropSeedsItem(
                            ModBlocks.ORIENTAL_TOBACCO_CROP.get(),
                            new Item.Properties(),
                            OrientalCropBlock.HALF
                    ));

    public static final RegistryObject<Item> DOKHA_TOBACCO_SEEDS =
            ITEMS.register("dokha_tobacco_seeds",
                    () -> new TallCropSeedsItem(
                            ModBlocks.DOKHA_TOBACCO_CROP.get(),
                            new Item.Properties(),
                            DokhaCropBlock.HALF
                    ));

    public static final RegistryObject<Item> SHADE_TOBACCO_SEEDS =
            ITEMS.register("shade_tobacco_seeds",
                    () -> new TallCropSeedsItem(
                            ModBlocks.SHADE_TOBACCO_CROP.get(),
                            new Item.Properties(),
                            ShadeCropBlock.HALF
                    ));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}