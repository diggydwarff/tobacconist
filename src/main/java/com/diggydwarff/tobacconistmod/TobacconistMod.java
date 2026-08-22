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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.fml.ModList;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

@Mod(TobacconistMod.MODID)
public class TobacconistMod {
    public static final String MODID = "tobacconistmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TobacconistMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerDynamicItems);
        modEventBus.addListener(this::registerCapabilities);
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
        // Keep the original small-scale molasses route, but require an actual Water Potion.
        event.getBuilder().addRecipe(
                DataComponentIngredient.of(false, PotionContents.createItemStack(Items.POTION, Potions.WATER)),
                Ingredient.of(Items.SUGAR_CANE),
                new ItemStack(BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getItem())
        );

        // Mundane Potion is the deliberately vanilla stepping stone: Water + Sugar -> Mundane,
        // then Wheat represents the mash/distillation step that produces our extraction spirit.
        event.getBuilder().addRecipe(
                DataComponentIngredient.of(false, PotionContents.createItemStack(Items.POTION, Potions.MUNDANE)),
                Ingredient.of(Items.WHEAT),
                new ItemStack(ModItems.BOTTLED_AQUA_VITAE.get())
        );

        registerVanillaEssenceBrewing(event);
        if (ModList.get().isLoaded("farmersdelight")) registerFarmersDelightEssenceBrewing(event);
        if (ModList.get().isLoaded("fruitsdelight")) registerFruitsDelightEssenceBrewing(event);
    }

    private void registerVanillaEssenceBrewing(RegisterBrewingRecipesEvent event) {
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR, Items.APPLE);
        addDoubleEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_TWO_APPLES_FLAVOR, Items.APPLE);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_GOLDENAPPLE_FLAVOR, Items.GOLDEN_APPLE);
        addDoubleEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_GOLDENAPPLE_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_TWO_GOLDENAPPLES_FLAVOR, Items.GOLDEN_APPLE);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_ROYALAPPLE_FLAVOR, Items.ENCHANTED_GOLDEN_APPLE);
        addDoubleEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_ROYALAPPLE_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_TWO_ROYALAPPLES_FLAVOR, Items.ENCHANTED_GOLDEN_APPLE);

        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_MELON_FLAVOR, Items.MELON_SLICE);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_SWEETBERRY_FLAVOR, Items.SWEET_BERRIES);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_GLOWBERRY_FLAVOR, Items.GLOW_BERRIES);
        // "Berry" is the mixed-berry concentrate, avoiding a duplicate Sweet Berries brewing recipe.
        addDoubleEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_SWEETBERRY_FLAVOR,
                BottledMolassesFlavors.BOTTLED_MOLASSES_BERRY_FLAVOR, Items.GLOW_BERRIES);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_CHORUS_FRUIT_FLAVOR, Items.CHORUS_FRUIT);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_HONEY_FLAVOR, Items.HONEY_BOTTLE);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_CAKE_FLAVOR, Items.CAKE);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_COOKIES_FLAVOR, Items.COOKIE);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_CREAM_FLAVOR, Items.MILK_BUCKET);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_COCOA_FLAVOR, Items.COCOA_BEANS);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_PUMPKIN_FLAVOR, Items.PUMPKIN);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_SUGAR_FLAVOR, Items.SUGAR);
        addEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_CARROT_FLAVOR, Items.CARROT);
    }

    private void registerFarmersDelightEssenceBrewing(RegisterBrewingRecipesEvent event) {
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_APPLEPIE_FLAVOR, "farmersdelight:apple_pie_slice");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_SWEETBERRY_CHEESECAKE_FLAVOR, "farmersdelight:sweet_berry_cheesecake_slice");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_CHOCOLATEPIE_FLAVOR, "farmersdelight:chocolate_pie_slice");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_SWEETBERRY_COOKIE_FLAVOR, "farmersdelight:sweet_berry_cookie");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_HONEYCOOKIE_FLAVOR, "farmersdelight:honey_cookie");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_MELONPOPSICLE_FLAVOR, "farmersdelight:melon_popsicle");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_GLOWBERRY_CUSTARD_FLAVOR, "farmersdelight:glow_berry_custard");
    }

    private void registerFruitsDelightEssenceBrewing(RegisterBrewingRecipesEvent event) {
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_PEACH_FLAVOR, "fruitsdelight:peach");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_PEAR_FLAVOR, "fruitsdelight:pear");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_MANGO_FLAVOR, "fruitsdelight:mango");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_LYCHEE_FLAVOR, "fruitsdelight:lychee");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_HAWBERRY_FLAVOR, "fruitsdelight:hawberry");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_ORANGE_FLAVOR, "fruitsdelight:orange");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_PERSIMMON_FLAVOR, "fruitsdelight:persimmon");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_BLUEBERRY_FLAVOR, "fruitsdelight:blueberry");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_LEMON_FLAVOR, "fruitsdelight:lemon");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_HAMIMELON_FLAVOR, "fruitsdelight:hamimelon_slice");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_PINEAPPLE_FLAVOR, "fruitsdelight:pineapple");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_MANGOSTEEN_FLAVOR, "fruitsdelight:mangosteen");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_CRANBERRY_FLAVOR, "fruitsdelight:cranberry");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_BAYBERRY_FLAVOR, "fruitsdelight:bayberry");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_FIG_FLAVOR, "fruitsdelight:fig");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_KIWI_FLAVOR, "fruitsdelight:kiwi");
        addOptionalEssenceRecipe(event, BottledMolassesFlavors.BOTTLED_MOLASSES_DURIAN_FLAVOR, "fruitsdelight:durian");
    }

    private void addEssenceRecipe(RegisterBrewingRecipesEvent event, BottledMolassesFlavors flavor, net.minecraft.world.item.Item ingredient) {
        event.getBuilder().addRecipe(
                Ingredient.of(ModItems.BOTTLED_AQUA_VITAE.get()),
                Ingredient.of(ingredient),
                flavor.getEssenceStack()
        );
    }

    private void addDoubleEssenceRecipe(RegisterBrewingRecipesEvent event,
                                        BottledMolassesFlavors base,
                                        BottledMolassesFlavors doubled,
                                        net.minecraft.world.item.Item ingredient) {
        // Component-aware input requires a fresh single-use essence bottle, preventing
        // already-processed bottles from being converted into a fresh double-strength bottle.
        event.getBuilder().addRecipe(
                DataComponentIngredient.of(false, base.getEssenceStack()),
                Ingredient.of(ingredient),
                doubled.getEssenceStack()
        );
    }

    private void addOptionalEssenceRecipe(RegisterBrewingRecipesEvent event, BottledMolassesFlavors flavor, String itemId) {
        ResourceLocation id = ResourceLocation.parse(itemId);
        BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> addEssenceRecipe(event, flavor, item));
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TobacconistCommands.register(event.getDispatcher());
    }
}
