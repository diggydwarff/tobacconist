package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.fluid.ExtractionFluidType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Common registrations have no Create or Herbalist dependency. */
public final class VaporContent {
    private static final String ID = TobacconistMod.MODID;
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);
    private static final DeferredRegister<DataComponentType<?>> DATA = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ID);
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ID);
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ID);

    public static final Supplier<DataComponentType<LiquidBlend>> BLEND = DATA.register("liquid_blend",
            () -> DataComponentType.<LiquidBlend>builder().persistent(VaporData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(VaporData.CODEC)).build());
    public static final Supplier<DataComponentType<Integer>> VOLUME = DATA.register("liquid_volume",
            () -> DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.intRange(0, 1000))
                    .networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<DataComponentType<Integer>> CHARGE = DATA.register("vape_charge",
            () -> DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.intRange(0, 100))
                    .networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<DataComponentType<Integer>> WATTAGE = DATA.register("vape_wattage",
            () -> DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.intRange(10, 40))
                    .networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<DataComponentType<Integer>> PUFF_COUNT = DATA.register("vape_puff_count",
            () -> DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.intRange(0, Integer.MAX_VALUE))
                    .networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<DataComponentType<BottledFluid>> BOTTLED_FLUID = DATA.register("bottled_fluid",
            () -> DataComponentType.<BottledFluid>builder().persistent(BottledFluid.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(BottledFluid.CODEC)).build());
    public static final Supplier<DataComponentType<Integer>> BUBBLE_CHARGE = DATA.register("bubble_pipe_charge",
            () -> DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.intRange(0, 24))
                    .networkSynchronized(ByteBufCodecs.VAR_INT).build());

    public static final Supplier<Item> VEGETABLE_BASE = bottle("vegetable_base", "vg");
    public static final Supplier<Item> CLEAR_BASE = bottle("clear_base", "pg");
    public static final Supplier<Item> CRUDE_EXTRACT = bottle("crude_tobacco_extract", "crude");
    public static final Supplier<Item> NICOTINE_EXTRACT = bottle("nicotine_extract", "nicotine");
    public static final Supplier<Item> VAPE_LIQUID = bottle("vape_liquid", "liquid");
    public static final Supplier<Item> VAPE_PEN = ITEMS.register("vape_pen", () -> new VapePenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> BOX_MOD = ITEMS.register("box_mod_vape",
            () -> new VapePenItem(new Item.Properties().stacksTo(1), VapePenItem.Kind.BOX_MOD));
    public static final Supplier<Item> CIGALIKE = ITEMS.register("cigalike_vape",
            () -> new VapePenItem(new Item.Properties().stacksTo(1), VapePenItem.Kind.CIGALIKE));
    public static final Supplier<Item> RECIPE_CARD = ITEMS.register("liquid_recipe_card", () -> new VaporBottleItem(new Item.Properties().stacksTo(1), "card"));
    public static final Supplier<Item> SMALL_GLASS_BOTTLE = ITEMS.register("small_glass_bottle", () -> new SizedBottleItem(new Item.Properties(), VaporData.SMALL_BOTTLE, false));
    public static final Supplier<Item> LARGE_GLASS_BOTTLE = ITEMS.register("large_glass_bottle", () -> new SizedBottleItem(new Item.Properties(), VaporData.LARGE_BOTTLE, false));
    public static final Supplier<Item> SMALL_FILLED_BOTTLE = ITEMS.register("small_filled_bottle", () -> new SizedBottleItem(new Item.Properties().stacksTo(1), VaporData.SMALL_BOTTLE, true));
    public static final Supplier<Item> LARGE_FILLED_BOTTLE = ITEMS.register("large_filled_bottle", () -> new SizedBottleItem(new Item.Properties().stacksTo(1), VaporData.LARGE_BOTTLE, true));
    public static final Supplier<Item> BUBBLE_PIPE = ITEMS.register("bubble_pipe", () -> new BubblePipeItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Block> EXTRACTION_STATION = BLOCKS.register("extraction_station",
            () -> new LiquidStationBlock(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL), false));
    public static final Supplier<Block> BLENDING_BENCH = BLOCKS.register("liquid_blending_bench",
            () -> new LiquidStationBlock(BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD).noOcclusion(), true));
    public static final Supplier<Block> EXTRACT_CAULDRON = BLOCKS.register("extract_cauldron",
            () -> new ExtractCauldronBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.CAULDRON)));
    static {
        ITEMS.register("extraction_station", () -> new BlockItem(EXTRACTION_STATION.get(), new Item.Properties()));
        ITEMS.register("liquid_blending_bench", () -> new BlockItem(BLENDING_BENCH.get(), new Item.Properties()));
    }
    public static final Supplier<BlockEntityType<LiquidStationEntity>> STATION = ENTITIES.register("liquid_station",
            () -> BlockEntityType.Builder.of(LiquidStationEntity::new, EXTRACTION_STATION.get(), BLENDING_BENCH.get()).build(null));
    public static final Supplier<BlockEntityType<ExtractCauldronEntity>> EXTRACT_CAULDRON_ENTITY = ENTITIES.register("extract_cauldron",
            () -> BlockEntityType.Builder.of(ExtractCauldronEntity::new, EXTRACT_CAULDRON.get()).build(null));
    public static final Supplier<MenuType<LiquidStationMenu>> STATION_MENU = MENUS.register("liquid_station",
            () -> IMenuTypeExtension.create(LiquidStationMenu::new));
    public static final Supplier<MenuType<VapePenMenu>> PEN_MENU = MENUS.register("vape_pen",
            () -> IMenuTypeExtension.create(VapePenMenu::new));
    public static final Supplier<RecipeType<ExtractionRecipe>> EXTRACTION = TYPES.register("extraction",
            () -> new RecipeType<>() { public String toString() { return ID + ":extraction"; } });
    public static final Supplier<RecipeSerializer<ExtractionRecipe>> EXTRACTION_SERIALIZER = SERIALIZERS.register("extraction", ExtractionRecipe.Serializer::new);

    public static final Supplier<Fluid> VEGETABLE_FLUID = fluid("vegetable_base", 0x99E8DF9C);
    public static final Supplier<Fluid> CLEAR_FLUID = fluid("clear_base", 0x77D8EDF1);
    public static final Supplier<Fluid> CRUDE_FLUID = fluid("crude_tobacco_extract", 0xBB71552C);
    public static final Supplier<Fluid> NICOTINE_FLUID = fluid("nicotine_extract", 0x66F4F3EB);
    public static final Supplier<Fluid> LIQUID_FLUID = fluid("vape_liquid", 0x99E8D8AD);

    private VaporContent() {}

    private static Supplier<Item> bottle(String name, String kind) {
        return ITEMS.register(name, () -> new VaporBottleItem(new Item.Properties().stacksTo(1), kind));
    }

    private static Supplier<Fluid> fluid(String name, int color) {
        Supplier<FluidType> type = FLUID_TYPES.register(name, () -> new ExtractionFluidType(color));
        // Factory-only fluids; no placeable source blocks or bucket items.
        FluidEntry entry = new FluidEntry(type);
        entry.source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(entry.properties()));
        entry.flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(entry.properties()));
        return entry.source;
    }
    private static final class FluidEntry {
        private final Supplier<FluidType> type;
        private Supplier<Fluid> source, flowing;
        private FluidEntry(Supplier<FluidType> type) { this.type = type; }
        private BaseFlowingFluid.Properties properties() { return new BaseFlowingFluid.Properties(type, source, flowing); }
    }

    public static void register(IEventBus bus) {
        DATA.register(bus); ITEMS.register(bus); BLOCKS.register(bus); ENTITIES.register(bus);
        MENUS.register(bus); FLUID_TYPES.register(bus); FLUIDS.register(bus); TYPES.register(bus); SERIALIZERS.register(bus);
        bus.addListener(VaporContent::capabilities);
        bus.addListener(VapeDyeing::setup);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, STATION.get(), (station, side) -> station.handler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, STATION.get(), (station, side) -> station.isBlending() ? station.fluidHandler() : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, EXTRACT_CAULDRON_ENTITY.get(), (cauldron, side) -> cauldron.fluidHandler());
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new VaporBottleHandler(stack),
                VEGETABLE_BASE.get(), CLEAR_BASE.get(), CRUDE_EXTRACT.get(), NICOTINE_EXTRACT.get(), VAPE_LIQUID.get());
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new SizedBottleHandler(stack),
                SMALL_GLASS_BOTTLE.get(), LARGE_GLASS_BOTTLE.get(), SMALL_FILLED_BOTTLE.get(), LARGE_FILLED_BOTTLE.get());
    }
}
