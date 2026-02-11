package com.diggydwarff.tobacconistmod.datagen.items;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.custom.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TobacconistMod.MODID);

    // TOOL ITEMS
    public static final RegistryObject<Item> WOODEN_SMOKING_PIPE = ITEMS.register("wooden_smoking_pipe", () -> new WoodenSmokingPipeItem(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_SMOKING_PIPE = ITEMS.register("gold_smoking_pipe", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PIPE_TOBACCO = ITEMS.register("pipe_tobacco", () -> new Item(new Item.Properties()));

    //public static final RegistryObject<Item> TOBACCO_POUCH = ITEMS.register("tobacco_pouch", () -> new TobaccoPouchItem(new Item.Properties()));

    public static final RegistryObject<Item> ROLLING_PAPER = ITEMS.register("rolling_paper", () -> new RollingPaperItem(new Item.Properties()));
    //public static final RegistryObject<Item> HOOKAH_COAL = ITEMS.register("hookah_coal", () -> new HookahCoalItem(new Item.Properties()));


    // FINAL PRODUCTS
    public static final RegistryObject<Item> CIGAR = ITEMS.register("cigar",
            () -> new CigarItem(new Item.Properties().durability(100)));

    public static final RegistryObject<Item> CIGARETTE = ITEMS.register("cigarette",
            () -> new CigaretteItem(new Item.Properties().durability(15)));

    public static final RegistryObject<Item> HOOKAH_HOSE = ITEMS.register("hookah_hose", () -> new HookahHoseItem(new Item.Properties()));
    //public static final RegistryObject<Item> UPGRADED_HOOKAH_HOSE = ITEMS.register("upgraded_hookah_hose", () -> new UpgradedHookahHoseItem(new Item.Properties()));
    //public static final RegistryObject<Item> DELUXE_HOOKAH_HOSE = ITEMS.register("deluxe_hookah_hose", () -> new DeluxeHookahHoseItem(new Item.Properties()));

    public static final RegistryObject<Item> SHISHA_TOBACCO = ITEMS.register("shisha_tobacco", () -> new ShishaTobaccoItem(new Item.Properties().durability(5000)));

    // TOBACCO LEAF ITEMS (wet)
    public static final RegistryObject<Item> WILD_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_wild", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VIRGINIA_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_virginia", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BURLEY_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_burley", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ORIENTAL_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_oriental", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DOKHA_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_dokha", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SHADE_TOBACCO_LEAF = ITEMS.register("tobacco_leaf_shade", () -> new Item(new Item.Properties()));


    // TOBACCO LEAF ITEMS (dry)
    public static final RegistryObject<Item> WILD_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_wild_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> VIRGINIA_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_virginia_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> BURLEY_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_burley_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> ORIENTAL_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_oriental_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> DOKHA_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_dokha_dry", () -> new TobaccoLeafItem(new Item.Properties()));
    public static final RegistryObject<Item> SHADE_TOBACCO_LEAF_DRY = ITEMS.register("tobacco_leaf_shade_dry", () -> new TobaccoLeafItem(new Item.Properties()));

    public static final RegistryObject<Item> TOBACCO_LOOSE_WILD =
            ITEMS.register("tobacco_loose_wild", () -> new LooseTobaccoItem(new Item.Properties(), 20, 5));

    public static final RegistryObject<Item> TOBACCO_LOOSE_VIRGINIA =
            ITEMS.register("tobacco_loose_virginia", () -> new LooseTobaccoItem(new Item.Properties(), 20, 10));

    public static final RegistryObject<Item> TOBACCO_LOOSE_BURLEY =
            ITEMS.register("tobacco_loose_burley", () -> new LooseTobaccoItem(new Item.Properties(), 20, 15));

    public static final RegistryObject<Item> TOBACCO_LOOSE_ORIENTAL =
            ITEMS.register("tobacco_loose_oriental", () -> new LooseTobaccoItem(new Item.Properties(), 20, 15));

    public static final RegistryObject<Item> TOBACCO_LOOSE_DOKHA =
            ITEMS.register("tobacco_loose_dokha", () -> new LooseTobaccoItem(new Item.Properties(), 20, 20));

    public static final RegistryObject<Item> TOBACCO_LOOSE_SHADE =
            ITEMS.register("tobacco_loose_shade", () -> new LooseTobaccoItem(new Item.Properties(), 20, 10));

    public static final RegistryObject<Item> BOTTLED_MOLASSES_PLAIN = ITEMS.register("bottled_molasses_plain", () -> BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getItem());

    // TOBACCO SEED ITEMS
    public static final RegistryObject<Item> WILD_TOBACCO_SEEDS = ITEMS.register("tobacco_seeds_wild",
            () -> new ItemNameBlockItem(ModBlocks.WILD_TOBACCO_CROP.get(), new Item.Properties()));

    public static final RegistryObject<Item> VIRGINIA_TOBACCO_SEEDS = ITEMS.register("tobacco_seeds_virginia",
            () -> new ItemNameBlockItem(ModBlocks.VIRGINIA_TOBACCO_CROP.get(), new Item.Properties()));

    public static final RegistryObject<Item> BURLEY_TOBACCO_SEEDS = ITEMS.register("tobacco_seeds_burley",
            () -> new ItemNameBlockItem(ModBlocks.BURLEY_TOBACCO_CROP.get(), new Item.Properties()));

    public static final RegistryObject<Item> ORIENTAL_TOBACCO_SEEDS = ITEMS.register("tobacco_seeds_oriental",
            () -> new ItemNameBlockItem(ModBlocks.ORIENTAL_TOBACCO_CROP.get(), new Item.Properties()));

    public static final RegistryObject<Item> DOKHA_TOBACCO_SEEDS = ITEMS.register("tobacco_seeds_dokha",
            () -> new ItemNameBlockItem(ModBlocks.DOKHA_TOBACCO_CROP.get(), new Item.Properties()));

    public static final RegistryObject<Item> SHADE_TOBACCO_SEEDS = ITEMS.register("tobacco_seeds_shade",
            () -> new ItemNameBlockItem(ModBlocks.SHADE_TOBACCO_CROP.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}