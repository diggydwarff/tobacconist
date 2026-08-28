package com.diggydwarff.tobacconistmod;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.ModPaintings;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.client.LooseTobaccoModelProperties;
import com.diggydwarff.tobacconistmod.command.TobacconistCommands;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.compat.curios.CuriosCompat;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.effect.ModEffects;
import com.diggydwarff.tobacconistmod.fluid.ModExtractionFluids;
import com.diggydwarff.tobacconistmod.fluid.ModMolassesFluids;
import com.diggydwarff.tobacconistmod.network.TobacconistNetwork;
import com.diggydwarff.tobacconistmod.particle.ModParticles;
import com.diggydwarff.tobacconistmod.recipes.ModRecipeSerializers;
import com.diggydwarff.tobacconistmod.recipes.ModRecipes;
import com.diggydwarff.tobacconistmod.screen.ModMenuTypes;
import com.diggydwarff.tobacconistmod.villager.ModVillagers;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(TobacconistMod.MODID)
public class TobacconistMod {
    public static final String MODID = "tobacconistmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TobacconistMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerDynamicItems);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        ModMolassesFluids.register(modEventBus);
        ModExtractionFluids.register(modEventBus);
        ModParticles.register(modEventBus);
        CreateCompat.init(modEventBus);
        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.register(modEventBus);
        }

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModEffects.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModVillagers.register(modEventBus);
        ModPaintings.PAINTING_VARIANTS.register(modEventBus);
        TobacconistCreativeTab.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);

        TobacconistNetwork.register();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, TobacconistConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TobacconistConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TobacconistConfig.SERVER_SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(
                    new ResourceLocation(MODID, "wild_flowering_tobacco_block"),
                    ModBlocks.POTTED_WILD_FLOWERING_TOBACCO);

            // NeoForge 1.21 uses a compostables data map; Forge 1.20.1 registers these directly.
            ComposterBlock.COMPOSTABLES.put(ModItems.WILD_TOBACCO_SEEDS.get(), 0.30F);
            ComposterBlock.COMPOSTABLES.put(ModItems.VIRGINIA_TOBACCO_SEEDS.get(), 0.30F);
            ComposterBlock.COMPOSTABLES.put(ModItems.BURLEY_TOBACCO_SEEDS.get(), 0.30F);
            ComposterBlock.COMPOSTABLES.put(ModItems.ORIENTAL_TOBACCO_SEEDS.get(), 0.30F);
            ComposterBlock.COMPOSTABLES.put(ModItems.DOKHA_TOBACCO_SEEDS.get(), 0.30F);
            ComposterBlock.COMPOSTABLES.put(ModItems.SHADE_TOBACCO_SEEDS.get(), 0.30F);

            ComposterBlock.COMPOSTABLES.put(ModItems.WILD_TOBACCO_LEAF.get(), 0.50F);
            ComposterBlock.COMPOSTABLES.put(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 0.50F);
            ComposterBlock.COMPOSTABLES.put(ModItems.BURLEY_TOBACCO_LEAF.get(), 0.50F);
            ComposterBlock.COMPOSTABLES.put(ModItems.ORIENTAL_TOBACCO_LEAF.get(), 0.50F);
            ComposterBlock.COMPOSTABLES.put(ModItems.DOKHA_TOBACCO_LEAF.get(), 0.50F);
            ComposterBlock.COMPOSTABLES.put(ModItems.SHADE_TOBACCO_LEAF.get(), 0.50F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.WILD_FLOWERING_TOBACCO.get().asItem(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModItems.SPOILED_TOBACCO.get(), 1.00F);

            registerBrewingRecipes();
        });
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LooseTobaccoModelProperties.register();
            CreateCompat.initClient();
        });
    }

    private void registerDynamicItems(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
                if (flavor == BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN) continue;
                helper.register(new ResourceLocation(MODID, flavor.getName()), flavor.getItem());
                helper.register(new ResourceLocation(MODID, flavor.getEssenceItemName()), flavor.getEssenceItem());
            }
        });
    }

    private void registerBrewingRecipes() {
        ItemStack waterPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        BrewingRecipeRegistry.addRecipe(
                StrictNBTIngredient.of(waterPotion),
                Ingredient.of(Items.SUGAR_CANE),
                new ItemStack(BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getItem()));

        ItemStack mundanePotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.MUNDANE);
        BrewingRecipeRegistry.addRecipe(
                StrictNBTIngredient.of(mundanePotion),
                Ingredient.of(Items.WHEAT),
                new ItemStack(ModItems.BOTTLED_AQUA_VITAE.get()));

        // Forge 1.20.1 registers brewing recipes before datapack item tags are bound. Register
        // the tag-backed recipes unconditionally: an unavailable flavor has an empty ingredient
        // tag and therefore cannot match, while an installed/datapack-provided ingredient becomes
        // usable without rebuilding the global BrewingRecipeRegistry.
        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (flavor.hasDirectEssenceInfusion()) {
                addEssenceRecipe(flavor);
            }
        }

        addCompositeEssenceRecipe(BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_TWO_APPLES_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR);
        addCompositeEssenceRecipe(BottledMolassesFlavors.BOTTLED_MOLASSES_GOLDENAPPLE_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_TWO_GOLDENAPPLES_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_GOLDENAPPLE_FLAVOR);
        addCompositeEssenceRecipe(BottledMolassesFlavors.BOTTLED_MOLASSES_ROYALAPPLE_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_TWO_ROYALAPPLES_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_ROYALAPPLE_FLAVOR);
        addCompositeEssenceRecipe(BottledMolassesFlavors.BOTTLED_MOLASSES_SWEETBERRY_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_BERRY_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_GLOWBERRY_FLAVOR);
    }

    private void addEssenceRecipe(BottledMolassesFlavors flavor) {
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(ModItems.BOTTLED_AQUA_VITAE.get()),
                Ingredient.of(flavor.getFlavoringIngredientTag()),
                flavor.getEssenceStack());
    }

    private void addCompositeEssenceRecipe(BottledMolassesFlavors base,
                                           BottledMolassesFlavors result,
                                           BottledMolassesFlavors ingredientFlavor) {
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(base.getEssenceItem()),
                Ingredient.of(ingredientFlavor.getFlavoringIngredientTag()),
                result.getEssenceStack());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TobacconistCommands.register(event.getDispatcher());
    }
}
