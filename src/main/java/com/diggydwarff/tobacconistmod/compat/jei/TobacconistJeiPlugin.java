package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class TobacconistJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(TobacconistMod.MODID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_WILD.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_VIRGINIA.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_BURLEY.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_ORIENTAL.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_DOKHA.get(), (stack, context) -> getLooseSubtype(stack));
        registration.registerSubtypeInterpreter(ModItems.TOBACCO_LOOSE_SHADE.get(), (stack, context) -> getLooseSubtype(stack));
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
                new LabelDuplicateRecipeCategory(guiHelper)
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
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.STONE_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.COPPER_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.IRON_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GOLD_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DIAMOND_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.NETHERITE_CHAVETA.get()), LeafCuttingRecipeCategory.TYPE);

        JeiItemLists.getAllSmokingPipes().forEach(pipe ->
                registration.addRecipeCatalyst(pipe, WoodenPipeFillRecipeCategory.TYPE)
        );

        registration.addRecipeCatalyst(new ItemStack(ModItems.SHISHA_TOBACCO.get()), ShishaMixRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.TOBACCO_BOX.get()), TobaccoBoxFillRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.TOBACCO_BOX.get()), TobaccoBoxLabelRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.TOBACCO_LABEL.get()), LabelDuplicateRecipeCategory.TYPE);
    }
}