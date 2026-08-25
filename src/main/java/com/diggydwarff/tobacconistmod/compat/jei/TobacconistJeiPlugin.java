package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

@JeiPlugin
public class TobacconistJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "jei_plugin");
    }

    private static final ISubtypeInterpreter<ItemStack> LOOSE_TOBACCO_SUBTYPE = new ISubtypeInterpreter<>() {
        @Override
        public Object getSubtypeData(ItemStack stack, UidContext context) {
            return getLooseSubtype(stack);
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
            return getLooseSubtype(stack);
        }
    };

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_WILD.get(), LOOSE_TOBACCO_SUBTYPE);
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_VIRGINIA.get(), LOOSE_TOBACCO_SUBTYPE);
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_BURLEY.get(), LOOSE_TOBACCO_SUBTYPE);
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_ORIENTAL.get(), LOOSE_TOBACCO_SUBTYPE);
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_DOKHA.get(), LOOSE_TOBACCO_SUBTYPE);
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_SHADE.get(), LOOSE_TOBACCO_SUBTYPE);
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
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(LeafCuttingRecipeCategory.TYPE, LeafCuttingJeiRecipe.createAll());
        registration.addRecipes(AverageLeavesRecipeCategory.TYPE, AverageLeavesJeiRecipe.createAll());
        registration.addRecipes(mezz.jei.api.constants.RecipeTypes.CRAFTING, WoodenPipeJeiRecipe.createAll());
        registration.addRecipes(CigaretteRecipeCategory.TYPE, CigaretteJeiRecipe.createAll());
        registration.addRecipes(CigarRecipeCategory.TYPE, CigarJeiRecipe.createAll());
        registration.addRecipes(WoodenPipeFillRecipeCategory.TYPE, WoodenPipeFillJeiRecipe.createAll());

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
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.STONE_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.COPPER_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.IRON_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GOLD_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DIAMOND_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.NETHERITE_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TOBACCO_DRYING_RACK.get().asItem()), DryingRackRecipeCategory.TYPE);
        if (com.diggydwarff.tobacconistmod.compat.create.CreateCompat.loaded()) {
            registration.addRecipeCatalyst(new ItemStack(ModBlocks.INDUSTRIAL_DRYING_RACK.get().asItem()), DryingRackRecipeCategory.TYPE);
        }

        registration.addRecipeCatalyst(new ItemStack(ModItems.WILD_TOBACCO_LEAF.get()), DryingRackRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get()), DryingRackRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BURLEY_TOBACCO_LEAF.get()), DryingRackRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.ORIENTAL_TOBACCO_LEAF.get()), DryingRackRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DOKHA_TOBACCO_LEAF.get()), DryingRackRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.SHADE_TOBACCO_LEAF.get()), DryingRackRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TOBACCO_BARREL.get().asItem()), TobaccoBarrelRecipeCategory.TYPE);
        JeiItemLists.getAllDryLeaves().forEach(stack ->
                registration.addRecipeCatalyst(stack, TobaccoBarrelRecipeCategory.TYPE)
        );

        JeiItemLists.getAllSmokingPipes().forEach(pipe ->
                registration.addRecipeCatalyst(pipe, WoodenPipeFillRecipeCategory.TYPE)
        );

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TOBACCO_DRYING_RACK.get().asItem()), DryingRackRecipeCategory.TYPE);

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

}