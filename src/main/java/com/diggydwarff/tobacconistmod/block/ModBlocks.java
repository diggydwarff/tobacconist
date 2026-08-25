package com.diggydwarff.tobacconistmod.block;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.custom.*;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, TobacconistMod.MODID);

    public static final Supplier<Block> HOOKAH = registerSingleStackBlock("hookah_block",
            () -> new HookahBlock(BlockBehaviour.Properties.of()
                    .strength(0.8F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> {
                        if (state.getValue(HookahBlock.GLOWING)) return 12;
                        if (state.getValue(HookahBlock.LIT)) return 6;
                        return 0;
                    })
                    .noOcclusion()));

    public static final Supplier<Block> TALL_HOOKAH = registerSingleStackBlock("tall_hookah_block",
            () -> new DyeableDoubleHookahBlock(hookahProperties()
                    .lightLevel(state -> {
                        if (state.getValue(DyeableDoubleHookahBlock.GLOWING)) return 12;
                        return state.getValue(DoubleHookahBlock.LIT) ? 6 : 0;
                    })));

    // Preserve existing material Hookah registry IDs for world compatibility.
    public static final Supplier<Block> ORNATE_COPPER_HOOKAH = registerSingleStackBlock("ornate_copper_hookah_block",
            () -> new CopperHookahBlock(hookahProperties(), 0, false));
    public static final Supplier<Block> EXPOSED_COPPER_HOOKAH = registerSingleStackBlock("exposed_copper_hookah_block",
            () -> new CopperHookahBlock(hookahProperties(), 1, false));
    public static final Supplier<Block> WEATHERED_COPPER_HOOKAH = registerSingleStackBlock("weathered_copper_hookah_block",
            () -> new CopperHookahBlock(hookahProperties(), 2, false));
    public static final Supplier<Block> OXIDIZED_COPPER_HOOKAH = registerSingleStackBlock("oxidized_copper_hookah_block",
            () -> new CopperHookahBlock(hookahProperties(), 3, false));
    public static final Supplier<Block> WAXED_COPPER_HOOKAH = registerSingleStackBlock("waxed_copper_hookah_block",
            () -> new CopperHookahBlock(hookahProperties(), 0, true));
    public static final Supplier<Block> WAXED_EXPOSED_COPPER_HOOKAH = registerSingleStackBlock("waxed_exposed_copper_hookah_block",
            () -> new CopperHookahBlock(hookahProperties(), 1, true));
    public static final Supplier<Block> WAXED_WEATHERED_COPPER_HOOKAH = registerSingleStackBlock("waxed_weathered_copper_hookah_block",
            () -> new CopperHookahBlock(hookahProperties(), 2, true));
    public static final Supplier<Block> WAXED_OXIDIZED_COPPER_HOOKAH = registerSingleStackBlock("waxed_oxidized_copper_hookah_block",
            () -> new CopperHookahBlock(hookahProperties(), 3, true));

    public static final Supplier<Block> ORNATE_GOLD_HOOKAH = registerSingleStackBlock("ornate_gold_hookah_block",
            () -> new DoubleHookahBlock(hookahProperties()));
    public static final Supplier<Block> ORNATE_DIAMOND_HOOKAH = registerSingleStackBlock("ornate_diamond_hookah_block",
            () -> new DoubleHookahBlock(hookahProperties()));
    public static final Supplier<Block> ORNATE_IRON_HOOKAH = registerSingleStackBlock("ornate_iron_hookah_block",
            () -> new DoubleHookahBlock(hookahProperties()));
    public static final Supplier<Block> ORNATE_AMETHYST_HOOKAH = registerSingleStackBlock("ornate_amethyst_hookah_block",
            () -> new DoubleHookahBlock(hookahProperties()));

    public static final Supplier<Block> REDSTONE_HOOKAH = registerSingleStackBlock("redstone_hookah_block",
            () -> new DoubleHookahBlock(hookahProperties()));
    public static final Supplier<Block> LAPIS_HOOKAH = registerSingleStackBlock("lapis_hookah_block",
            () -> new DoubleHookahBlock(hookahProperties()));
    public static final Supplier<Block> OBSIDIAN_HOOKAH = registerSingleStackBlock("obsidian_hookah_block",
            () -> new DoubleHookahBlock(hookahProperties().strength(4.0F, 1200.0F).sound(SoundType.STONE)));
    public static final Supplier<Block> EMERALD_HOOKAH = registerSingleStackBlock("emerald_hookah_block",
            () -> new DoubleHookahBlock(hookahProperties()));
    public static final Supplier<Block> NETHERITE_HOOKAH = registerSingleStackBlock("netherite_hookah_block",
            () -> new NetheriteHookahBlock(hookahProperties()
                    .strength(5.0F, 1200.0F)
                    .lightLevel(state -> state.hasProperty(DoubleHookahBlock.LIT) && state.getValue(DoubleHookahBlock.LIT) ? 8 : 3)));

    private static BlockBehaviour.Properties hookahProperties() {
        return BlockBehaviour.Properties.of()
                .strength(0.8F)
                .sound(SoundType.METAL)
                .lightLevel(state -> state.hasProperty(DoubleHookahBlock.LIT) && state.getValue(DoubleHookahBlock.LIT) ? 6 : 0)
                .noOcclusion();
    }

    public static final Supplier<Block> TOBACCO_DRYING_RACK = registerBlock("tobacco_drying_rack_block",
            () -> new TobaccoDryingRackBlock(BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.WOOD).noOcclusion()));

    public static final Supplier<Block> INDUSTRIAL_DRYING_RACK = registerBlock("industrial_drying_rack",
            () -> new IndustrialDryingRackBlock(BlockBehaviour.Properties.of()
                    .strength(2.5F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    // Traditional hanging option: placed directly from a stack of 16 raw or cured leaves, so it has no BlockItem.
    public static final Supplier<Block> HANGING_TOBACCO_LEAVES = BLOCKS.register("hanging_tobacco_leaves",
            () -> new HangingTobaccoBlock(BlockBehaviour.Properties.of()
                    .strength(0.35F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .noCollission()));

    public static final Supplier<Block> WILD_FLOWERING_TOBACCO = registerBlock("wild_flowering_tobacco_block",
            () -> new FlowerBlock(MobEffects.CONFUSION, 5,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.ALLIUM).noOcclusion().noCollission()));

    // No BlockItem: the normal Wild Flowering Tobacco item is what players place into a Flower Pot.
    public static final Supplier<Block> POTTED_WILD_FLOWERING_TOBACCO = BLOCKS.register(
            "potted_wild_flowering_tobacco",
            () -> new FlowerPotBlock(
                    () -> (FlowerPotBlock) Blocks.FLOWER_POT,
                    WILD_FLOWERING_TOBACCO,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_ALLIUM).noOcclusion()
            )
    );
    public static final Supplier<Block> WILD_TOBACCO_CROP = BLOCKS.register("tobacco_crop_wild",
            () -> new WildCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final Supplier<Block> VIRGINIA_TOBACCO_CROP = BLOCKS.register("tobacco_crop_virginia",
            () -> new VirginiaCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final Supplier<Block> BURLEY_TOBACCO_CROP = BLOCKS.register("tobacco_crop_burley",
            () -> new BurleyCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final Supplier<Block> ORIENTAL_TOBACCO_CROP = BLOCKS.register("tobacco_crop_oriental",
            () -> new OrientalCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final Supplier<Block> DOKHA_TOBACCO_CROP = BLOCKS.register("tobacco_crop_dokha",
            () -> new DokhaCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final Supplier<Block> SHADE_TOBACCO_CROP = BLOCKS.register("tobacco_crop_shade",
            () -> new ShadeCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noOcclusion().noCollission()));


    public static final Supplier<Block> RAW_VIRGINIA_TOBACCO_CRATE = registerBlock("raw_virginia_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> RAW_BURLEY_TOBACCO_CRATE = registerBlock("raw_burley_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> RAW_ORIENTAL_TOBACCO_CRATE = registerBlock("raw_oriental_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> RAW_DOKHA_TOBACCO_CRATE = registerBlock("raw_dokha_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> RAW_SHADE_TOBACCO_CRATE = registerBlock("raw_shade_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> RAW_WILD_TOBACCO_CRATE = registerBlock("raw_wild_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> VIRGINIA_TOBACCO_CRATE = registerBlock("virginia_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> BURLEY_TOBACCO_CRATE = registerBlock("burley_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> ORIENTAL_TOBACCO_CRATE = registerBlock("oriental_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> DOKHA_TOBACCO_CRATE = registerBlock("dokha_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> SHADE_TOBACCO_CRATE = registerBlock("shade_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> WILD_TOBACCO_CRATE = registerBlock("wild_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> BLENDED_TOBACCO_CRATE = registerBlock("blended_tobacco_crate",
            () -> new TobaccoCrateBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> TOBACCO_BARREL = registerBlock("tobacco_barrel",
            () -> new TobaccoBarrelBlock(BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.WOOD)));

    private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        Supplier<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> Supplier<T> registerSingleStackBlock(String name, Supplier<T> block) {
        Supplier<T> toReturn = BLOCKS.register(name, block);
        registerSingleStackBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> Supplier<Item> registerBlockItem(String name, Supplier<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> Supplier<Item> registerSingleStackBlockItem(String name, Supplier<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().stacksTo(1)));
    }

    public static final Supplier<Block> FLUE_FIREBOX = registerBlock("flue_firebox",
            () -> new FlueFireboxBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties
                    .of()
                    .strength(1.5F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? 13 : 0)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
