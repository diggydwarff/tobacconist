package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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
                new LabelDuplicateRecipeCategory(guiHelper),
                new TobaccoBarrelRecipeCategory(guiHelper),
                new DryingRackRecipeCategory(guiHelper)
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
                Component.literal("Used to ferment and age dry tobacco leaves."),
                Component.literal(""),
                Component.literal("Fermentation:"),
                Component.literal("- Put dry leaves into the barrel"),
                Component.literal("- Requires warmth 3+ and humidity 25+"),
                Component.literal("- Ferments after 2 in-game days"),
                Component.literal(""),
                Component.literal("Aging:"),
                Component.literal("- Fermented leaves can continue aging"),
                Component.literal("- Best in cool, dark storage"),
                Component.literal("- Lower warmth and moderate humidity preferred")
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
                Component.literal("Dry leaves can be fermented and aged in a tobacco barrel.")
        );

        registration.addIngredientInfo(
                java.util.List.of(new ItemStack(ModBlocks.TOBACCO_DRYING_RACK.get().asItem())),
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                Component.literal("Used to cure raw tobacco leaves."),
                Component.literal(""),
                Component.literal("Air curing: shade or indirect light"),
                Component.literal("Sun curing: direct sunlight"),
                Component.literal("Flue curing: hot dry enclosed heat"),
                Component.literal("Fire curing: smoke and low heat")
        );

        registration.addRecipes(DryingRackRecipeCategory.TYPE, DryingRackJeiRecipe.createAll());
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
    }

}