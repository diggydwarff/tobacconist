package com.diggydwarff.tobacconistmod;

import com.diggydwarff.tobacconistmod.client.LooseTobaccoModelProperties;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.command.TobacconistCommands;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.compat.curios.CuriosCompat;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.fluid.AquaVitaeBottleFluidHandler;
import com.diggydwarff.tobacconistmod.fluid.EssenceBottleFluidHandler;
import com.diggydwarff.tobacconistmod.fluid.ModExtractionFluids;
import com.diggydwarff.tobacconistmod.effect.ModEffects;
import com.diggydwarff.tobacconistmod.fluid.ModMolassesFluids;
import com.diggydwarff.tobacconistmod.fluid.GlassBottleMolassesFluidHandler;
import com.diggydwarff.tobacconistmod.fluid.MolassesBottleFluidHandler;
import com.diggydwarff.tobacconistmod.particle.ModParticles;
import com.diggydwarff.tobacconistmod.recipes.ModRecipeSerializers;
import com.diggydwarff.tobacconistmod.recipes.ModRecipes;
import com.diggydwarff.tobacconistmod.screen.ModMenuTypes;
import com.diggydwarff.tobacconistmod.villager.ModVillagers;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(TobacconistMod.MODID)
public class TobacconistMod {
    public static final String MODID = "tobacconistmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TobacconistMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerDynamicItems);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        ModMolassesFluids.register(modEventBus);
        ModExtractionFluids.register(modEventBus);
        ModParticles.register(modEventBus);
        CreateCompat.init(modEventBus);

        ModItems.register(modEventBus);
        CuriosCompat.init(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModEffects.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModVillagers.register(modEventBus);
        TobacconistCreativeTab.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, TobacconistConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, TobacconistConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, TobacconistConfig.SERVER_SPEC);

        NeoForge.EVENT_BUS.register(this);
    }


    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> ((net.minecraft.world.level.block.FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT)
                .addPlant(ResourceLocation.fromNamespaceAndPath(MODID, "wild_flowering_tobacco_block"),
                        ModBlocks.POTTED_WILD_FLOWERING_TOBACCO));
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(LooseTobaccoModelProperties::register);
        CreateCompat.initClient();
    }

    private void registerDynamicItems(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
                // Plain molasses is already registered through ModItems.
                if (flavor == BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN) continue;
                helper.register(ResourceLocation.fromNamespaceAndPath(MODID, flavor.getName()), flavor.getItem());
                helper.register(ResourceLocation.fromNamespaceAndPath(MODID, flavor.getEssenceItemName()), flavor.getEssenceItem());
            }
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.HOOKAH.get(),
                (hookah, side) -> hookah.getItemHandler()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.TOBACCO_DRYING_RACK.get(),
                (rack, side) -> rack.getItemHandler(side)
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.TOBACCO_BARREL.get(),
                (barrel, side) -> barrel.getItemHandler(side)
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.FLUE_FIREBOX.get(),
                (firebox, side) -> firebox.getItemHandler(side)
        );

        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            event.registerItem(
                    Capabilities.FluidHandler.ITEM,
                    (stack, context) -> new MolassesBottleFluidHandler(stack, flavor),
                    flavor.getItem()
            );
            if (!flavor.isPlain()) {
                event.registerItem(
                        Capabilities.FluidHandler.ITEM,
                        (stack, context) -> new EssenceBottleFluidHandler(stack, flavor),
                        flavor.getEssenceItem()
                );
            }
        }

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new AquaVitaeBottleFluidHandler(stack),
                ModItems.BOTTLED_AQUA_VITAE.get()
        );

        // Empty processing bottles collapse to vanilla glass bottles. One generic fill handler
        // lets tanks/Spouts refill molasses, Aqua Vitae and essence containers.
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new GlassBottleMolassesFluidHandler(stack),
                Items.GLASS_BOTTLE
        );
    }

    @SubscribeEvent
    public void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        // Plain molasses requires a Water Potion and Sugar Cane.
        event.getBuilder().addRecipe(
                DataComponentIngredient.of(false, PotionContents.createItemStack(Items.POTION, Potions.WATER)),
                Ingredient.of(Items.SUGAR_CANE),
                new ItemStack(BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getItem())
        );

        // Aqua Vitae brewing starts from a Mundane Potion and Wheat.
        event.getBuilder().addRecipe(
                DataComponentIngredient.of(false, PotionContents.createItemStack(Items.POTION, Potions.MUNDANE)),
                Ingredient.of(Items.WHEAT),
                new ItemStack(ModItems.BOTTLED_AQUA_VITAE.get())
        );

        registerFlavorEssenceBrewing(event);
    }

    private void registerFlavorEssenceBrewing(RegisterBrewingRecipesEvent event) {
        // Flavor tags are shared by Brewing Stand and Create recipes.
        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (flavor.hasDirectEssenceInfusion() && hasFlavoringIngredient(event, flavor)) {
                addEssenceRecipe(event, flavor);
            }
        }

        // Double Apple variants use the matching single-strength essence as their base.
        addCompositeEssenceRecipe(event,
                BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_TWO_APPLES_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR);
        addCompositeEssenceRecipe(event,
                BottledMolassesFlavors.BOTTLED_MOLASSES_GOLDENAPPLE_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_TWO_GOLDENAPPLES_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_GOLDENAPPLE_FLAVOR);
        addCompositeEssenceRecipe(event,
                BottledMolassesFlavors.BOTTLED_MOLASSES_ROYALAPPLE_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_TWO_ROYALAPPLES_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_ROYALAPPLE_FLAVOR);

        // Mixed Berry uses Sweet Berry Essence plus Glow Berry to avoid generic berry-tag overlap.
        addCompositeEssenceRecipe(event,
                BottledMolassesFlavors.BOTTLED_MOLASSES_SWEETBERRY_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_BERRY_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_GLOWBERRY_FLAVOR);
    }

    private boolean hasFlavoringIngredient(RegisterBrewingRecipesEvent event, BottledMolassesFlavors flavor) {
        return event.getRegistryAccess()
                .lookupOrThrow(Registries.ITEM)
                .get(flavor.getFlavoringIngredientTag())
                .map(tag -> tag.size() > 0)
                .orElse(false);
    }

    private void addEssenceRecipe(RegisterBrewingRecipesEvent event, BottledMolassesFlavors flavor) {
        event.getBuilder().addRecipe(
                Ingredient.of(ModItems.BOTTLED_AQUA_VITAE.get()),
                Ingredient.of(flavor.getFlavoringIngredientTag()),
                flavor.getEssenceStack()
        );
    }

    private void addCompositeEssenceRecipe(RegisterBrewingRecipesEvent event,
                                           BottledMolassesFlavors base,
                                           BottledMolassesFlavors result,
                                           BottledMolassesFlavors ingredientFlavor) {
        if (!hasFlavoringIngredient(event, ingredientFlavor)) {
            return;
        }
        event.getBuilder().addRecipe(
                DataComponentIngredient.of(false, base.getEssenceStack()),
                Ingredient.of(ingredientFlavor.getFlavoringIngredientTag()),
                result.getEssenceStack()
        );
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TobacconistCommands.register(event.getDispatcher());
    }
}
