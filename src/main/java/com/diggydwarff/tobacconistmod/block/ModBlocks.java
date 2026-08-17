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

    public static final Supplier<Block> ORNATE_COPPER_HOOKAH = registerSingleStackBlock("ornate_copper_hookah_block",
            () -> new DoubleHookahBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.8F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(DoubleHookahBlock.LIT) ? 6 : 0)
                            .noOcclusion()
            ));

    public static final Supplier<Block> ORNATE_GOLD_HOOKAH = registerSingleStackBlock("ornate_gold_hookah_block",
            () -> new DoubleHookahBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.8F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(DoubleHookahBlock.LIT) ? 6 : 0)
                            .noOcclusion()
            ));

    public static final Supplier<Block> ORNATE_DIAMOND_HOOKAH = registerSingleStackBlock("ornate_diamond_hookah_block",
            () -> new DoubleHookahBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.8F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(DoubleHookahBlock.LIT) ? 6 : 0)
                            .noOcclusion()
            ));

    public static final Supplier<Block> ORNATE_IRON_HOOKAH = registerSingleStackBlock("ornate_iron_hookah_block",
            () -> new DoubleHookahBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.8F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(DoubleHookahBlock.LIT) ? 6 : 0)
                            .noOcclusion()
            ));

    public static final Supplier<Block> ORNATE_AMETHYST_HOOKAH = registerSingleStackBlock("ornate_amethyst_hookah_block",
            () -> new DoubleHookahBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.8F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(DoubleHookahBlock.LIT) ? 6 : 0)
                            .noOcclusion()
            ));

    public static final Supplier<Block> TOBACCO_DRYING_RACK = registerBlock("tobacco_drying_rack_block",
            () -> new TobaccoDryingRackBlock(BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.WOOD).noOcclusion()));

    public static final Supplier<Block> WILD_FLOWERING_TOBACCO = registerBlock("wild_flowering_tobacco_block",
            () -> new FlowerBlock(MobEffects.CONFUSION, 5,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.ALLIUM).noOcclusion().noCollission()));
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

    public static final Supplier<Block> VIRGINIA_TOBACCO_CRATE = registerBlock("virginia_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> BURLEY_TOBACCO_CRATE = registerBlock("burley_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> ORIENTAL_TOBACCO_CRATE = registerBlock("oriental_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> DOKHA_TOBACCO_CRATE = registerBlock("dokha_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> SHADE_TOBACCO_CRATE = registerBlock("shade_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final Supplier<Block> WILD_TOBACCO_CRATE = registerBlock("wild_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
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
