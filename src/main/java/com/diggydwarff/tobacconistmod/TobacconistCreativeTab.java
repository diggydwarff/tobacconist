package com.diggydwarff.tobacconistmod;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.ShishaFlavoringItem;
import com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe;
import com.diggydwarff.tobacconistmod.util.PaintingTabHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.diggydwarff.tobacconistmod.datagen.items.ModItems.*;

public class TobacconistCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TobacconistMod.MODID);

    public static final RegistryObject<CreativeModeTab> COURSE_TAB = CREATIVE_MODE_TABS.register("tobacconistmod",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(CIGAR.get()))
                    .title(Component.translatable("creativetab.tobacconistmod"))
                    .displayItems((displayParameters, output) -> {
                        output.accept(ROLLING_PAPER.get());
                        output.accept(BAMBOO_CHARCOAL.get());
                        output.accept(CIGAR.get());
                        output.accept(CIGARETTE.get());
                        output.accept(HOOKAH_HOSE.get());
                        output.accept(SHISHA_TOBACCO.get());

                        output.accept(TOBACCO_BOX.get());
                        output.accept(TOBACCO_LABEL.get());

                        output.accept(creativeLeaf(new ItemStack(WILD_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(VIRGINIA_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(BURLEY_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(ORIENTAL_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(DOKHA_TOBACCO_LEAF.get()), false));
                        output.accept(creativeLeaf(new ItemStack(SHADE_TOBACCO_LEAF.get()), false));

                        output.accept(creativeLeaf(new ItemStack(WILD_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(VIRGINIA_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(BURLEY_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(ORIENTAL_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(DOKHA_TOBACCO_LEAF_DRY.get()), true));
                        output.accept(creativeLeaf(new ItemStack(SHADE_TOBACCO_LEAF_DRY.get()), true));

                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_WILD.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_VIRGINIA.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_BURLEY.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_ORIENTAL.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_DOKHA.get()));
                        addLooseVariants(output, new ItemStack(TOBACCO_LOOSE_SHADE.get()));

                        output.accept(ModBlocks.TOBACCO_DRYING_RACK.get());
                        output.accept(ModBlocks.FLUE_FIREBOX.get());
                        output.accept(ModBlocks.TOBACCO_BARREL.get());

                        output.accept(ModBlocks.WILD_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.VIRGINIA_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.BURLEY_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.ORIENTAL_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.DOKHA_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.SHADE_TOBACCO_CRATE.get());

                        output.accept(WILD_TOBACCO_SEEDS.get());
                        output.accept(VIRGINIA_TOBACCO_SEEDS.get());
                        output.accept(BURLEY_TOBACCO_SEEDS.get());
                        output.accept(ORIENTAL_TOBACCO_SEEDS.get());
                        output.accept(DOKHA_TOBACCO_SEEDS.get());
                        output.accept(SHADE_TOBACCO_SEEDS.get());

                        output.accept(ModBlocks.WILD_TOBACCO_CROP.get());
                        output.accept(ModBlocks.VIRGINIA_TOBACCO_CROP.get());
                        output.accept(ModBlocks.BURLEY_TOBACCO_CROP.get());
                        output.accept(ModBlocks.ORIENTAL_TOBACCO_CROP.get());
                        output.accept(ModBlocks.DOKHA_TOBACCO_CROP.get());
                        output.accept(ModBlocks.SHADE_TOBACCO_CROP.get());

                        output.accept(ModBlocks.HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_COPPER_HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_IRON_HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_GOLD_HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_DIAMOND_HOOKAH.get());
                        output.accept(ModBlocks.ORNATE_AMETHYST_HOOKAH.get());

                        output.accept(STONE_CHAVETA.get());
                        output.accept(IRON_CHAVETA.get());
                        output.accept(COPPER_CHAVETA.get());
                        output.accept(GOLD_CHAVETA.get());
                        output.accept(DIAMOND_CHAVETA.get());
                        output.accept(NETHERITE_CHAVETA.get());

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
                        output.accept(GOLD_SMOKING_PIPE.get());
                        output.accept(IRON_SMOKING_PIPE.get());
                        output.accept(COPPER_SMOKING_PIPE.get());
                        output.accept(GEM_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(DIAMOND_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(LAPIS_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(EMERALD_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(EMERALD_AZTEC_SMOKING_PIPE.get());
                        output.accept(NETHERITE_SMOKING_PIPE.get());
                        output.accept(KISERU_SMOKING_PIPE.get());

                        output.accept(PaintingTabHelper.paintingVariant("american_tobacco_fields_small"));
                        output.accept(PaintingTabHelper.paintingVariant("camel_american_cigarette"));
                        output.accept(PaintingTabHelper.paintingVariant("american_lone_cowboy"));
                        output.accept(PaintingTabHelper.paintingVariant("american_cigarette"));
                        output.accept(PaintingTabHelper.paintingVariant("ottoman_hookah"));
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

                        output.accept(BOTTLED_MOLASSES_PLAIN.get());
                        addAllMolassesFlavorings(output);

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
        output.accept(TobaccoCuringHelper.makeCreativeLoose(base, TobaccoCuringHelper.CUT_FLAKE));
    }

    private static void addAllMolassesFlavorings(CreativeModeTab.Output output) {
        List<Item> flavorings = StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
                .filter(item -> item instanceof ShishaFlavoringItem)
                .sorted(Comparator.comparing(item ->
                        BuiltInRegistries.ITEM.getKey(item).toString()
                ))
                .toList();

        for (Item item : flavorings) {
            output.accept(item);
        }
    }

    private static ItemStack makePipe(Item plankItem) {
        ItemStack pipe = new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get());
        pipe.getOrCreateTag().putString(
                WoodenPipeRecipe.NBT_WOOD_PLANK,
                BuiltInRegistries.ITEM.getKey(plankItem).toString()
        );
        return pipe;
    }
}