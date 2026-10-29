package com.diggydwarff.tobacconistmod;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.compat.FlavorCompatibility;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe;
import com.diggydwarff.tobacconistmod.util.PaintingTabHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.vapor.VaporContent;
import com.diggydwarff.tobacconistmod.vapor.VaporData;
import com.diggydwarff.tobacconistmod.vapor.LiquidBlend;
import com.diggydwarff.tobacconistmod.novelty.ToyPipeItem;
import com.diggydwarff.tobacconistmod.novelty.NoveltyContent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.diggydwarff.tobacconistmod.datagen.items.ModItems.*;

public class TobacconistCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TobacconistMod.MODID);

    public static final Supplier<CreativeModeTab> COURSE_TAB = CREATIVE_MODE_TABS.register("tobacconistmod",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(CIGAR.get()))
                    .title(Component.translatable("creativetab.tobacconistmod"))
                    .displayItems((displayParameters, output) -> {
                        // Guide and tools
                        output.accept(TOBACCO_GUIDE.get());
                        output.accept(TOBACCONISTS_SPECTACLES.get());

                        // Growing
                        output.accept(WILD_TOBACCO_SEEDS.get());
                        output.accept(VIRGINIA_TOBACCO_SEEDS.get());
                        output.accept(BURLEY_TOBACCO_SEEDS.get());
                        output.accept(ORIENTAL_TOBACCO_SEEDS.get());
                        output.accept(DOKHA_TOBACCO_SEEDS.get());
                        output.accept(SHADE_TOBACCO_SEEDS.get());
                        output.accept(ModBlocks.WILD_FLOWERING_TOBACCO.get());

                        output.accept(creativeLeaf(new ItemStack(WILD_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(VIRGINIA_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(BURLEY_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(ORIENTAL_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(DOKHA_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(SHADE_TOBACCO_LEAF.get()), false));

                        // Curing
                        output.accept(ModBlocks.TOBACCO_DRYING_RACK.get());
                        if (CreateCompat.loaded()) {
                            output.accept(ModBlocks.INDUSTRIAL_DRYING_RACK.get());
                        }
                        output.accept(ModBlocks.FLUE_FIREBOX.get());
                        output.accept(ModBlocks.TOBACCO_BARREL.get());

                        output.accept(creativeLeaf(new ItemStack(WILD_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(VIRGINIA_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(BURLEY_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(ORIENTAL_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(DOKHA_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(SHADE_TOBACCO_LEAF_DRY.get()), true));

                        // Cutting and blending
                        output.accept(STONE_CHAVETA.get());
                        output.accept(COPPER_CHAVETA.get());
                        output.accept(IRON_CHAVETA.get());
                        output.accept(GOLD_CHAVETA.get());
                        output.accept(DIAMOND_CHAVETA.get());
                        output.accept(NETHERITE_CHAVETA.get());

                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_WILD.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_VIRGINIA.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_BURLEY.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_ORIENTAL.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_DOKHA.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_SHADE.get()));
                        output.accept(BLENDED_TOBACCO.get());

                        // Shared flavor ingredients
                        output.accept(BOTTLED_AQUA_VITAE.get());
                        addAllEssences(output);

                        // Storage, labels and logistics
                        output.accept(TOBACCO_POUCH.get());
                        output.accept(TOBACCO_BOX.get());
                        output.accept(TOBACCO_LABEL.get());
                        if (CreateCompat.loaded()) {
                            output.accept(BRASS_NAME_TAG.get());
                            output.accept(ModBlocks.PRODUCTION_MONITOR.get());
                        }

                        output.accept(ModBlocks.RAW_WILD_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.RAW_VIRGINIA_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.RAW_BURLEY_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.RAW_ORIENTAL_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.RAW_DOKHA_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.RAW_SHADE_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.WILD_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.VIRGINIA_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.BURLEY_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.ORIENTAL_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.DOKHA_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.SHADE_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.BLENDED_TOBACCO_CRATE.get());

                        // Cigarettes and cigars
                        output.accept(ROLLING_PAPER.get());
                        output.accept(CIGARETTE.get());
                        output.accept(CIGAR.get());

                        // Traditional pipes
                        output.accept(makePipe(Items.OAK_PLANKS));
                        output.accept(makePipe(Items.SPRUCE_PLANKS));
                        output.accept(makePipe(Items.BIRCH_PLANKS));
                        output.accept(makePipe(Items.JUNGLE_PLANKS));
                        output.accept(makePipe(Items.ACACIA_PLANKS));
                        output.accept(makePipe(Items.DARK_OAK_PLANKS));
                        output.accept(makePipe(Items.MANGROVE_PLANKS));
                        output.accept(makePipe(Items.CHERRY_PLANKS));
                        output.accept(makePipe(Items.BAMBOO_PLANKS));
                        output.accept(makePipe(Items.CRIMSON_PLANKS));
                        output.accept(makePipe(Items.WARPED_PLANKS));
                        output.accept(CORN_COB_SMOKING_PIPE.get());
                        output.accept(CLAY_SMOKING_PIPE.get());
                        output.accept(KISERU_SMOKING_PIPE.get());
                        output.accept(COPPER_SMOKING_PIPE.get());
                        output.accept(IRON_SMOKING_PIPE.get());
                        output.accept(BRASS_SMOKING_PIPE.get());
                        output.accept(GOLD_SMOKING_PIPE.get());
                        output.accept(LAPIS_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(GEM_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(DIAMOND_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(EMERALD_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(EMERALD_AZTEC_SMOKING_PIPE.get());
                        output.accept(NETHERITE_SMOKING_PIPE.get());

                        // Novelty pipes and rare finds
                        output.accept(VaporContent.BUBBLE_PIPE.get());
                        output.accept(ToyPipeItem.loaded(NoveltyContent.CONFETTI_PIPE.get()));
                        output.accept(ToyPipeItem.loaded(NoveltyContent.PETAL_PIPE.get()));
                        output.accept(ToyPipeItem.loaded(NoveltyContent.WHISTLING_PIPE.get()));
                        output.accept(NoveltyContent.WIZARDS_LONGPIPE.get());
                        output.accept(NoveltyContent.SUNKEEPERS_PIPE.get());
                        output.accept(NoveltyContent.SOULGLASS_PIPE.get());
                        output.accept(NoveltyContent.ECHO_PIPE.get());
                        output.accept(NoveltyContent.ASTRAL_PIPE.get());

                        // Shisha ingredients and hookahs
                        output.accept(BOTTLED_MOLASSES_PLAIN.get());
                        addAllFlavoredMolasses(output);

                        output.accept(SHISHA_TOBACCO.get());
                        output.accept(BAMBOO_CHARCOAL.get());
                        output.accept(HOOKAH_HOSE.get());
                        output.accept(DIRTY_HOOKAH_WATER.get());
                        output.accept(ModBlocks.HOOKAH.get());
                        output.accept(ModBlocks.TALL_HOOKAH.get());
                        output.accept(ModBlocks.PUMPKIN_HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_COPPER_HOOKAH.get());
                        output.accept(ModBlocks.EXPOSED_COPPER_HOOKAH.get());
                        output.accept(ModBlocks.WEATHERED_COPPER_HOOKAH.get());
                        output.accept(ModBlocks.OXIDIZED_COPPER_HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_IRON_HOOKAH.get());
                        output.accept(ModBlocks.REDSTONE_HOOKAH.get());
                        output.accept(ModBlocks.LAPIS_HOOKAH.get());
                        output.accept(ModBlocks.OBSIDIAN_HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_GOLD_HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_AMETHYST_HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_DIAMOND_HOOKAH.get());
                        output.accept(ModBlocks.EMERALD_HOOKAH.get());
                        output.accept(ModBlocks.NETHERITE_HOOKAH.get());

                        // Vape production and bottles
                        output.accept(VaporContent.BLENDING_BENCH.get());
                        output.accept(VaporContent.RECIPE_CARD.get());
                        output.accept(VaporContent.VEGETABLE_BASE.get());
                        output.accept(VaporContent.CLEAR_BASE.get());
                        output.accept(VaporContent.CRUDE_EXTRACT.get());
                        output.accept(VaporContent.NICOTINE_EXTRACT.get());
                        output.accept(VaporContent.SMALL_GLASS_BOTTLE.get());
                        output.accept(VaporContent.LARGE_GLASS_BOTTLE.get());
                        output.accept(VaporData.bottle(new LiquidBlend(700000, 300000, 0, java.util.Map.of()), VaporData.SMALL_BOTTLE, new ItemStack(VaporContent.SMALL_GLASS_BOTTLE.get())));
                        output.accept(VaporData.bottle(new LiquidBlend(700000, 300000, 0, java.util.Map.of()), 250));
                        output.accept(VaporData.bottle(new LiquidBlend(700000, 300000, 0, java.util.Map.of()), 1000, new ItemStack(VaporContent.LARGE_GLASS_BOTTLE.get())));

                        // Devices by tank capacity
                        output.accept(VaporContent.CIGALIKE.get());
                        output.accept(VaporContent.VAPE_PEN.get());
                        output.accept(VaporContent.BOX_MOD.get());

                        // Decorations
                        output.accept(PaintingTabHelper.paintingVariant("american_tobacco_fields_small"));
                        output.accept(PaintingTabHelper.paintingVariant("camel_american_cigarette"));
                        output.accept(PaintingTabHelper.paintingVariant("american_lone_cowboy"));
                        output.accept(PaintingTabHelper.paintingVariant("american_cigarette"));
                        output.accept(PaintingTabHelper.paintingVariant("ottoman_hookah"));
                        output.accept(PaintingTabHelper.paintingVariant("morrocan_hookah"));
                        output.accept(PaintingTabHelper.paintingVariant("american_cowboy_pair_wide"));
                        output.accept(PaintingTabHelper.paintingVariant("peace_pipe"));
                        output.accept(PaintingTabHelper.paintingVariant("japanese_kiseru_wide"));
                        output.accept(PaintingTabHelper.paintingVariant("american_tobacco_fields_wide"));
                        output.accept(PaintingTabHelper.paintingVariant("american_tobacco_fields"));
                        output.accept(PaintingTabHelper.paintingVariant("japanese_kiseru"));
                        output.accept(PaintingTabHelper.paintingVariant("arabian_nights"));
                        output.accept(PaintingTabHelper.paintingVariant("american_cowboy_pair"));
                        output.accept(PaintingTabHelper.paintingVariant("havana_cigar"));
                        output.accept(PaintingTabHelper.paintingVariant("andean_mapacho"));


                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    private static ItemStack creativeLeaf(ItemStack base, boolean cured) {
        ItemStack stack = base.copy();
        TobaccoCuringHelper.applyCreativeLeafDefaults(stack, cured);
        return stack;
    }

    private static void addLooseVariants(CreativeModeTab.Output output, ItemStack base) {
        output.accept(TobaccoCuringHelper.makeCreativeLoose(base, TobaccoCuringHelper.CUT_RIBBON));
        output.accept(TobaccoCuringHelper.makeCreativeLoose(base, TobaccoCuringHelper.CUT_SHAG));
        output.accept(TobaccoCuringHelper.makeCreativeLoose(base, TobaccoCuringHelper.CUT_ROUGH));
        output.accept(TobaccoCuringHelper.makeCreativeLoose(base, TobaccoCuringHelper.CUT_PLUG));
        output.accept(TobaccoCuringHelper.makeCreativeLoose(base, TobaccoCuringHelper.CUT_FLAKE));
    }

    private static void addAllEssences(CreativeModeTab.Output output) {
        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (flavor.isPlain() || !FlavorCompatibility.isFlavorAvailable(flavor)) continue;
            output.accept(flavor.getEssenceItem());
        }
    }

    private static void addAllFlavoredMolasses(CreativeModeTab.Output output) {
        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (flavor.isPlain() || !FlavorCompatibility.isFlavorAvailable(flavor)) continue;
            output.accept(flavor.getItem());
        }
    }

    private static ItemStack makePipe(Item plankItem) {
        ItemStack pipe = new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get());
        LegacyItemTags.getOrCreateTag(pipe).putString(
                WoodenPipeRecipe.NBT_WOOD_PLANK,
                BuiltInRegistries.ITEM.getKey(plankItem).toString()
        );
        return pipe;
    }

}