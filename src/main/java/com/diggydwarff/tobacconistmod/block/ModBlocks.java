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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TobacconistMod.MODID);

    public static final RegistryObject<Block> HOOKAH = registerBlock("hookah_block",
            () -> new HookahBlock(BlockBehaviour.Properties.copy(Blocks.STONE).lightLevel(state -> state.getValue(HookahBlock.LIT) ? 6 : 0).strength(6f).requiresCorrectToolForDrops().noOcclusion().noLootTable()));

    public static final RegistryObject<Block> WILD_FLOWERING_TOBACCO = registerBlock("wild_flowering_tobacco_block",
            () -> new FlowerBlock(() -> MobEffects.CONFUSION, 5,
                    BlockBehaviour.Properties.copy(Blocks.ALLIUM).noOcclusion().noCollission()));
    public static final RegistryObject<Block> WILD_TOBACCO_CROP = BLOCKS.register("tobacco_crop_wild",
            () -> new WildCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final RegistryObject<Block> VIRGINIA_TOBACCO_CROP = BLOCKS.register("tobacco_crop_virginia",
            () -> new VirginiaCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final RegistryObject<Block> BURLEY_TOBACCO_CROP = BLOCKS.register("tobacco_crop_burley",
            () -> new BurleyCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final RegistryObject<Block> ORIENTAL_TOBACCO_CROP = BLOCKS.register("tobacco_crop_oriental",
            () -> new OrientalCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final RegistryObject<Block> DOKHA_TOBACCO_CROP = BLOCKS.register("tobacco_crop_dokha",
            () -> new DokhaCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final RegistryObject<Block> SHADE_TOBACCO_CROP = BLOCKS.register("tobacco_crop_shade",
            () -> new ShadeCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));

    public static final RegistryObject<Block> VIRGINIA_TOBACCO_CRATE = registerBlock("virginia_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)              // lower = faster (try 0.7–1.5)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final RegistryObject<Block> BURLEY_TOBACCO_CRATE = registerBlock("burley_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)              // lower = faster (try 0.7–1.5)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final RegistryObject<Block> ORIENTAL_TOBACCO_CRATE = registerBlock("oriental_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)              // lower = faster (try 0.7–1.5)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final RegistryObject<Block> DOKHA_TOBACCO_CRATE = registerBlock("dokha_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)              // lower = faster (try 0.7–1.5)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final RegistryObject<Block> SHADE_TOBACCO_CRATE = registerBlock("shade_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)              // lower = faster (try 0.7–1.5)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    public static final RegistryObject<Block> WILD_TOBACCO_CRATE = registerBlock("wild_tobacco_crate",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.0F)              // lower = faster (try 0.7–1.5)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            ));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}