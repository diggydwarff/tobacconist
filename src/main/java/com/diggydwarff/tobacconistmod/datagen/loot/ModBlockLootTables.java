package com.diggydwarff.tobacconistmod.datagen.loot;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.*;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {

        // Utility/placeable blocks should drop themselves. Their block entities
        // handle dropping any stored contents separately.
        this.dropSelf(ModBlocks.HOOKAH.get());
        this.dropSelf(ModBlocks.ORNATE_COPPER_HOOKAH.get());
        this.dropSelf(ModBlocks.ORNATE_GOLD_HOOKAH.get());
        this.dropSelf(ModBlocks.ORNATE_DIAMOND_HOOKAH.get());
        this.dropSelf(ModBlocks.ORNATE_IRON_HOOKAH.get());
        this.dropSelf(ModBlocks.ORNATE_AMETHYST_HOOKAH.get());
        this.dropSelf(ModBlocks.TOBACCO_DRYING_RACK.get());
        this.dropSelf(ModBlocks.TOBACCO_BARREL.get());
        this.dropSelf(ModBlocks.FLUE_FIREBOX.get());

        // Potted plants drop both the vanilla pot and the plant used to fill it.
        this.add(ModBlocks.POTTED_WILD_FLOWERING_TOBACCO.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(net.minecraft.world.item.Items.FLOWER_POT)))
                        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModBlocks.WILD_FLOWERING_TOBACCO.get()))));

        this.add(ModBlocks.SHADE_TOBACCO_CRATE.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.TOBACCO_LOOSE_SHADE.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(9.0f))))));
        this.add(ModBlocks.DOKHA_TOBACCO_CRATE.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.TOBACCO_LOOSE_DOKHA.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(9.0f))))));
        this.add(ModBlocks.ORIENTAL_TOBACCO_CRATE.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.TOBACCO_LOOSE_ORIENTAL.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(9.0f))))));
        this.add(ModBlocks.BURLEY_TOBACCO_CRATE.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.TOBACCO_LOOSE_BURLEY.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(9.0f))))));
        this.add(ModBlocks.VIRGINIA_TOBACCO_CRATE.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.TOBACCO_LOOSE_VIRGINIA.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(9.0f))))));
        this.add(ModBlocks.WILD_TOBACCO_CRATE.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.TOBACCO_LOOSE_WILD.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(9.0f))))));

        LootItemCondition.Builder wildBuilder = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.WILD_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(WildCropBlock.AGE, 7));


        LootItemCondition.Builder virginiaUpper = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.VIRGINIA_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(VirginiaCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder virginiaUpperOnly = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.VIRGINIA_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(VirginiaCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder virginiaLower = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.VIRGINIA_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(VirginiaCropBlock.HALF, DoubleBlockHalf.LOWER));

        LootItemCondition.Builder burleyUpper = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.BURLEY_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(BurleyCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder burleyUpperOnly = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.BURLEY_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(BurleyCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder burleyLower = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.BURLEY_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(BurleyCropBlock.HALF, DoubleBlockHalf.LOWER));

        LootItemCondition.Builder dokhaUpper = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.DOKHA_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(DokhaCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder dokhaUpperOnly = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.DOKHA_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(DokhaCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder dokhaLower = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.DOKHA_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(DokhaCropBlock.HALF, DoubleBlockHalf.LOWER));

        LootItemCondition.Builder orientalUpper = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.ORIENTAL_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(OrientalCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder orientalUpperOnly = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.ORIENTAL_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(OrientalCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder orientalLower = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.ORIENTAL_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(OrientalCropBlock.HALF, DoubleBlockHalf.LOWER));

        LootItemCondition.Builder shadeUpper = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.SHADE_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(ShadeCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder shadeUpperOnly = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.SHADE_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(ShadeCropBlock.HALF, DoubleBlockHalf.UPPER));

        LootItemCondition.Builder shadeLower = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(ModBlocks.SHADE_TOBACCO_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(ShadeCropBlock.HALF, DoubleBlockHalf.LOWER));

        this.add(ModBlocks.WILD_TOBACCO_CROP.get(),
                LootTable.lootTable()
                        // LOWER seeds
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.WILD_TOBACCO_CROP.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(WildCropBlock.HALF, DoubleBlockHalf.LOWER)))
                                .add(LootItem.lootTableItem(ModItems.WILD_TOBACCO_SEEDS.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))))

                        // UPPER leaves (no age gate)
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.WILD_TOBACCO_CROP.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(WildCropBlock.HALF, DoubleBlockHalf.UPPER)))
                                .add(LootItem.lootTableItem(ModItems.WILD_TOBACCO_LEAF.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f)))))
        );

        this.add(ModBlocks.VIRGINIA_TOBACCO_CROP.get(),
                LootTable.lootTable()
                        // LOWER seeds
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.VIRGINIA_TOBACCO_CROP.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(VirginiaCropBlock.HALF, DoubleBlockHalf.LOWER)))
                                .add(LootItem.lootTableItem(ModItems.VIRGINIA_TOBACCO_SEEDS.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))))

                        // UPPER leaves (no age gate)
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(virginiaUpperOnly)
                                .add(LootItem.lootTableItem(ModItems.VIRGINIA_TOBACCO_LEAF.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f)))))
        );

        this.add(ModBlocks.BURLEY_TOBACCO_CROP.get(),
                LootTable.lootTable()
                        // LOWER seeds
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.BURLEY_TOBACCO_CROP.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(BurleyCropBlock.HALF, DoubleBlockHalf.LOWER)))
                                .add(LootItem.lootTableItem(ModItems.BURLEY_TOBACCO_SEEDS.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))))

                        // UPPER leaves (no age gate)
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(burleyUpperOnly)
                                .add(LootItem.lootTableItem(ModItems.BURLEY_TOBACCO_LEAF.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f)))))
        );

        this.add(ModBlocks.DOKHA_TOBACCO_CROP.get(),
                LootTable.lootTable()
                        // LOWER seeds
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.DOKHA_TOBACCO_CROP.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(DokhaCropBlock.HALF, DoubleBlockHalf.LOWER)))
                                .add(LootItem.lootTableItem(ModItems.DOKHA_TOBACCO_SEEDS.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))))

                        // UPPER leaves (no age gate)
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(dokhaUpperOnly)
                                .add(LootItem.lootTableItem(ModItems.DOKHA_TOBACCO_LEAF.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f)))))
        );

        this.add(ModBlocks.ORIENTAL_TOBACCO_CROP.get(),
                LootTable.lootTable()
                        // LOWER seeds
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.ORIENTAL_TOBACCO_CROP.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(OrientalCropBlock.HALF, DoubleBlockHalf.LOWER)))
                                .add(LootItem.lootTableItem(ModItems.ORIENTAL_TOBACCO_SEEDS.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))))

                        // UPPER leaves (no age gate)
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(orientalUpperOnly)
                                .add(LootItem.lootTableItem(ModItems.ORIENTAL_TOBACCO_LEAF.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f)))))
        );

        this.add(ModBlocks.SHADE_TOBACCO_CROP.get(),
                LootTable.lootTable()
                        // LOWER seeds
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.SHADE_TOBACCO_CROP.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(ShadeCropBlock.HALF, DoubleBlockHalf.LOWER)))
                                .add(LootItem.lootTableItem(ModItems.SHADE_TOBACCO_SEEDS.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))))

                        // UPPER leaves (no age gate)
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(shadeUpperOnly)
                                .add(LootItem.lootTableItem(ModItems.SHADE_TOBACCO_LEAF.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f)))))
        );

        // Flowering wild tobacco is a simple worldgen plant, not an age-gated crop.
        // Always drop one leaf plus one or two seeds.
        this.add(ModBlocks.WILD_FLOWERING_TOBACCO.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.WILD_TOBACCO_LEAF.get())))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.WILD_TOBACCO_SEEDS.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f))))));

    }


    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())::iterator;
    }
}
