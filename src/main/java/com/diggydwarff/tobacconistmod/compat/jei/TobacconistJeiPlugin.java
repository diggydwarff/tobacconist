package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

@JeiPlugin
public class TobacconistJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(TobacconistMod.MODID, "jei_plugin");
    }


    /** Wood variants have distinct names and appearances even though they share one item ID. */
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.WOODEN_SMOKING_PIPE.get(), (stack, context) -> {
            var tag = stack.getTag();
            String wood = tag == null ? "" : tag.getString(com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe.NBT_WOOD_PLANK);
            return wood.isEmpty() ? "minecraft:oak_planks" : wood;
        });
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_WILD.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_VIRGINIA.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_BURLEY.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_ORIENTAL.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_DOKHA.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_SHADE.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.BLENDED_TOBACCO.get(), (stack, context) -> getLooseSubtype(stack));
    }

    private static String getLooseSubtype(ItemStack stack) {
        String cutType = TobaccoCuringHelper.getCutType(stack);
        if (cutType == null || cutType.isBlank()) {
            cutType = "uncut";
        }
        return cutType;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(
                new ExtractionRecipeCategory(guiHelper),
                new LeafCuttingRecipeCategory(guiHelper),
                new AverageLeavesRecipeCategory(
                        guiHelper,
                        guiHelper.createDrawableItemStack(new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()))
                ),
                new CigaretteRecipeCategory(guiHelper),
                new CigarRecipeCategory(guiHelper),
                new WoodenPipeFillRecipeCategory(guiHelper),
                new ShishaMixRecipeCategory(guiHelper),
                new TobaccoBoxFillRecipeCategory(guiHelper),
                new TobaccoBoxLabelRecipeCategory(guiHelper),
                new LabelDuplicateRecipeCategory(guiHelper),
                new TobaccoBarrelRecipeCategory(guiHelper),
                new DryingRackRecipeCategory(guiHelper),
                new HookahStationRecipeCategory(guiHelper),
                new HookahUseRecipeCategory(guiHelper)
        );

        if (com.diggydwarff.tobacconistmod.compat.create.CreateCompat.loaded()) {
            registration.addRecipeCategories(new HomogenizationRecipeCategory(guiHelper));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level != null) registration.addRecipes(ExtractionRecipeCategory.TYPE,
                level.getRecipeManager().getAllRecipesFor(com.diggydwarff.tobacconistmod.vapor.VaporContent.EXTRACTION.get()).stream()
                        .filter(recipe -> !recipe.getId().getPath().equals("extraction/nicotine") && !recipe.getId().getPath().equals("extraction/clear_base"))
                        .toList());
        var brewing = registration.getVanillaRecipeFactory();
        registration.addRecipes(mezz.jei.api.constants.RecipeTypes.BREWING, java.util.List.of(
                brewing.createBrewingRecipe(java.util.List.of(new ItemStack(Items.PAPER)),
                        new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.CRUDE_EXTRACT.get()),
                        new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.NICOTINE_EXTRACT.get())),
                brewing.createBrewingRecipe(java.util.List.of(new ItemStack(Items.SUGAR_CANE)),
                        new ItemStack(ModItems.BOTTLED_AQUA_VITAE.get()),
                        new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.CLEAR_BASE.get()))));
        registration.addIngredientInfo(java.util.List.of(new ItemStack(net.minecraft.world.level.block.Blocks.CAULDRON)), VanillaTypes.ITEM_STACK,
                Component.translatable("tobacconistmod.vapor.jei_cauldron"));
        registration.addIngredientInfo(java.util.List.of(new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.BLENDING_BENCH.get()),
                        new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.VAPE_PEN.get()),
                        new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.BOX_MOD.get()),
                        new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.CIGALIKE.get())), VanillaTypes.ITEM_STACK,
                Component.translatable("tobacconistmod.vapor.jei_blending"), Component.translatable("tobacconistmod.vapor.jei_pen"));
        for (var pipe : new net.minecraft.world.item.Item[]{com.diggydwarff.tobacconistmod.novelty.NoveltyContent.WIZARDS_LONGPIPE.get(),
                com.diggydwarff.tobacconistmod.novelty.NoveltyContent.SOULGLASS_PIPE.get(), com.diggydwarff.tobacconistmod.novelty.NoveltyContent.ASTRAL_PIPE.get(), com.diggydwarff.tobacconistmod.novelty.NoveltyContent.SUNKEEPERS_PIPE.get(), com.diggydwarff.tobacconistmod.novelty.NoveltyContent.ECHO_PIPE.get()}) {
            var relic = (com.diggydwarff.tobacconistmod.novelty.RelicPipeItem) pipe;
            registration.addIngredientInfo(java.util.List.of(new ItemStack(pipe)), VanillaTypes.ITEM_STACK,
                    Component.translatable("tobacconistmod.novelty." + relic.key() + "_source"),
                    Component.translatable("tobacconistmod.novelty.jei_relic"));
        }
        for(var toy : new net.minecraft.world.item.Item[]{com.diggydwarff.tobacconistmod.novelty.NoveltyContent.PETAL_PIPE.get(), com.diggydwarff.tobacconistmod.novelty.NoveltyContent.WHISTLING_PIPE.get()}) {
            registration.addIngredientInfo(java.util.List.of(new ItemStack(toy)), VanillaTypes.ITEM_STACK,
                    Component.translatable(((com.diggydwarff.tobacconistmod.novelty.ToyPipeItem)toy).refillKey()), Component.translatable("tobacconistmod.novelty.toy_controls"));
        }
        registration.addIngredientInfo(java.util.List.of(new ItemStack(com.diggydwarff.tobacconistmod.novelty.NoveltyContent.CONFETTI_PIPE.get())), VanillaTypes.ITEM_STACK,
                Component.translatable("tobacconistmod.novelty.confetti_refill"), Component.translatable("tobacconistmod.novelty.toy_controls"));
        registration.addIngredientInfo(java.util.List.of(new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.CRUDE_EXTRACT.get()),
                        new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.NICOTINE_EXTRACT.get()),
                        new ItemStack(com.diggydwarff.tobacconistmod.vapor.VaporContent.CLEAR_BASE.get())), VanillaTypes.ITEM_STACK,
                Component.translatable("tobacconistmod.vapor.jei_brewing"));
        registration.addRecipes(LeafCuttingRecipeCategory.TYPE, LeafCuttingJeiRecipe.createAll());
        registration.addRecipes(AverageLeavesRecipeCategory.TYPE, AverageLeavesJeiRecipe.createAll());
        registration.addRecipes(mezz.jei.api.constants.RecipeTypes.CRAFTING, WoodenPipeJeiRecipe.createAll());
        registration.addRecipes(CigaretteRecipeCategory.TYPE, CigaretteJeiRecipe.createAll());
        registration.addRecipes(CigarRecipeCategory.TYPE, CigarJeiRecipe.createAll());
        registration.addRecipes(WoodenPipeFillRecipeCategory.TYPE, WoodenPipeFillJeiRecipe.createAll());
        registration.addRecipes(mezz.jei.api.constants.RecipeTypes.CRAFTING, FlavorMolassesCraftingJeiRecipe.createAll());

        if (com.diggydwarff.tobacconistmod.compat.create.CreateCompat.loaded()) {
            registration.addRecipes(HomogenizationRecipeCategory.TYPE, HomogenizationJeiRecipe.createAll());
        }

        registration.addRecipes(ShishaMixRecipeCategory.TYPE, ShishaMixJeiRecipe.createAll());
        registration.addRecipes(TobaccoBoxFillRecipeCategory.TYPE, TobaccoBoxFillJeiRecipe.createAll());
        registration.addRecipes(TobaccoBoxLabelRecipeCategory.TYPE, TobaccoBoxLabelJeiRecipe.createAll());
        registration.addRecipes(LabelDuplicateRecipeCategory.TYPE, LabelDuplicateJeiRecipe.createAll());
        registration.addRecipes(TobaccoBarrelRecipeCategory.TYPE, BarrelProcessJeiRecipe.createAll());

        registration.addIngredientInfo(
                java.util.List.of(new ItemStack(ModBlocks.TOBACCO_BARREL.get().asItem())),
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                Component.translatable("tobacconistmod.jei.info.barrel.summary"),
                Component.empty(),
                Component.translatable("tobacconistmod.jei.info.barrel.fermentation"),
                Component.translatable("tobacconistmod.jei.info.barrel.fermentation_1"),
                Component.translatable("tobacconistmod.jei.info.barrel.fermentation_2"),
                Component.translatable("tobacconistmod.jei.info.barrel.fermentation_3"),
                Component.empty(),
                Component.translatable("tobacconistmod.jei.info.barrel.aging"),
                Component.translatable("tobacconistmod.jei.info.barrel.aging_1"),
                Component.translatable("tobacconistmod.jei.info.barrel.aging_2"),
                Component.translatable("tobacconistmod.jei.info.barrel.aging_3")
        );

        registration.addIngredientInfo(
                java.util.List.of(
                        new ItemStack(ModItems.WILD_TOBACCO_LEAF_DRY.get()),
                        new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()),
                        new ItemStack(ModItems.BURLEY_TOBACCO_LEAF_DRY.get()),
                        new ItemStack(ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get()),
                        new ItemStack(ModItems.DOKHA_TOBACCO_LEAF_DRY.get()),
                        new ItemStack(ModItems.SHADE_TOBACCO_LEAF_DRY.get())
                ),
                VanillaTypes.ITEM_STACK,
                Component.translatable("tobacconistmod.jei.info.dry_leaf_barrel")
        );

        registration.addIngredientInfo(
                java.util.List.of(new ItemStack(ModBlocks.TOBACCO_DRYING_RACK.get().asItem())),
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                Component.translatable("tobacconistmod.jei.info.rack.summary"),
                Component.empty(),
                Component.translatable("tobacconistmod.jei.info.rack.air"),
                Component.translatable("tobacconistmod.jei.info.rack.sun"),
                Component.translatable("tobacconistmod.jei.info.rack.flue"),
                Component.translatable("tobacconistmod.jei.info.rack.fire")
        );

        if (com.diggydwarff.tobacconistmod.compat.create.CreateCompat.loaded()) {
            registration.addIngredientInfo(
                    java.util.List.of(new ItemStack(ModBlocks.INDUSTRIAL_DRYING_RACK.get().asItem())),
                    mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                    Component.translatable("tobacconistmod.jei.info.rack.industrial")
            );
        }

        registration.addIngredientInfo(
                java.util.List.of(new ItemStack(ModItems.HOOKAH_HOSE.get())),
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                Component.translatable("tobacconistmod.jei.info.hose.summary"),
                Component.empty(),
                Component.translatable("tobacconistmod.jei.info.hose.how_to"),
                Component.translatable("tobacconistmod.jei.info.hose.step_1"),
                Component.translatable("tobacconistmod.jei.info.hose.step_2"),
                Component.translatable("tobacconistmod.jei.info.hose.step_3"),
                Component.empty(),
                Component.translatable("tobacconistmod.jei.info.hose.consume")
        );

        registration.addRecipes(DryingRackRecipeCategory.TYPE, DryingRackJeiRecipe.createAll());

        registration.addRecipes(HookahStationRecipeCategory.TYPE, HookahStationJeiRecipe.createAll());
        registration.addRecipes(HookahUseRecipeCategory.TYPE, HookahUseJeiRecipe.createAll());

        List<ItemStack> unavailableFlavorItems = JeiItemLists.getUnavailableFlavorItems();
        if (!unavailableFlavorItems.isEmpty()) {
            registration.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, unavailableFlavorItems);
        }

        List<FluidStack> unavailableFlavorFluids = JeiItemLists.getUnavailableFlavorFluids();
        if (!unavailableFlavorFluids.isEmpty()) {
            registration.getIngredientManager().removeIngredientsAtRuntime(ForgeTypes.FLUID_STACK, unavailableFlavorFluids);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(net.minecraft.world.level.block.Blocks.CAULDRON), ExtractionRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(net.minecraft.world.level.block.Blocks.CAMPFIRE), ExtractionRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.STONE_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.COPPER_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.IRON_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GOLD_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DIAMOND_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.NETHERITE_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TOBACCO_DRYING_RACK.get().asItem()), DryingRackRecipeCategory.TYPE);
        if (com.diggydwarff.tobacconistmod.compat.create.CreateCompat.loaded()) {
            registration.addRecipeCatalyst(new ItemStack(ModBlocks.INDUSTRIAL_DRYING_RACK.get().asItem()), DryingRackRecipeCategory.TYPE);
            addCreateCatalyst(registration, "mechanical_mixer", HomogenizationRecipeCategory.TYPE);
            addCreateCatalyst(registration, "basin", HomogenizationRecipeCategory.TYPE);
        }


        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TOBACCO_BARREL.get().asItem()), TobaccoBarrelRecipeCategory.TYPE);

        JeiItemLists.getAllSmokingPipes().forEach(pipe ->
                registration.addRecipeCatalyst(pipe, WoodenPipeFillRecipeCategory.TYPE)
        );


        registration.addRecipeCatalyst(new ItemStack(ModItems.SHISHA_TOBACCO.get()), ShishaMixRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.TOBACCO_BOX.get()), TobaccoBoxFillRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.TOBACCO_BOX.get()), TobaccoBoxLabelRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.TOBACCO_LABEL.get()), LabelDuplicateRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ORNATE_IRON_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ORNATE_GOLD_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ORNATE_COPPER_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ORNATE_AMETHYST_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ORNATE_DIAMOND_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TALL_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PUMPKIN_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.REDSTONE_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LAPIS_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.OBSIDIAN_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.EMERALD_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.NETHERITE_HOOKAH.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BAMBOO_CHARCOAL.get().asItem()), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.COAL), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.CHARCOAL), HookahStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.SHISHA_TOBACCO.get()), HookahStationRecipeCategory.TYPE);
    }

    private static void addCreateCatalyst(IRecipeCatalystRegistration registration, String path, RecipeType<?> type) {
        Item item = Registry.ITEM.get(new ResourceLocation("create", path));
        if (item != Items.AIR) {
            registration.addRecipeCatalyst(new ItemStack(item), type);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (!com.diggydwarff.tobacconistmod.compat.create.CreateCompat.loaded()) return;

        jeiRuntime.getRecipeManager()
                .getRecipeType(new ResourceLocation("create", "mixing"))
                .ifPresent(type -> hideCreateHomogenizationMarkers(jeiRuntime, type));
    }

    private static <T> void hideCreateHomogenizationMarkers(IJeiRuntime jeiRuntime, RecipeType<T> recipeType) {
        List<T> markers = jeiRuntime.getRecipeManager()
                .createRecipeLookup(recipeType)
                .includeHidden()
                .get()
                .filter(TobacconistJeiPlugin::isCreateHomogenizationMarker)
                .toList();
        if (!markers.isEmpty()) {
            jeiRuntime.getRecipeManager().hideRecipes(recipeType, markers);
        }
    }

    private static boolean isCreateHomogenizationMarker(Object recipe) {
        return recipe != null && recipe.getClass().getName().equals(
                "com.diggydwarff.tobacconistmod.compat.create.CreateTobaccoHomogenizingRecipe");
    }

}
