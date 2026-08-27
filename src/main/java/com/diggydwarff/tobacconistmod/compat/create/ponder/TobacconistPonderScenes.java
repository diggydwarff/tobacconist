package com.diggydwarff.tobacconistmod.compat.create.ponder;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/** Associates Tobacconist items and machines with the Create integration storyboards. */
public final class TobacconistPonderScenes {
    private TobacconistPonderScenes() {}

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // Register curing and homogenization tutorials on every raw and cured leaf variety.
        helper.forComponents(
                        id(ModBlocks.TOBACCO_DRYING_RACK.get()),
                        id(ModItems.WILD_TOBACCO_LEAF.get()),
                        id(ModItems.VIRGINIA_TOBACCO_LEAF.get()),
                        id(ModItems.BURLEY_TOBACCO_LEAF.get()),
                        id(ModItems.ORIENTAL_TOBACCO_LEAF.get()),
                        id(ModItems.DOKHA_TOBACCO_LEAF.get()),
                        id(ModItems.SHADE_TOBACCO_LEAF.get()),
                        id(ModItems.WILD_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.BURLEY_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.DOKHA_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.SHADE_TOBACCO_LEAF_DRY.get())
                )
                .addStoryBoard("tobacco/curing", TobacconistPonderStoryboards::curing);

        helper.forComponents(id(ModBlocks.INDUSTRIAL_DRYING_RACK.get()))
                .addStoryBoard("tobacco/industrial_curing", TobacconistPonderStoryboards::industrialCuring);

        helper.forComponents(
                        id(ModItems.WILD_TOBACCO_LEAF.get()),
                        id(ModItems.VIRGINIA_TOBACCO_LEAF.get()),
                        id(ModItems.BURLEY_TOBACCO_LEAF.get()),
                        id(ModItems.ORIENTAL_TOBACCO_LEAF.get()),
                        id(ModItems.DOKHA_TOBACCO_LEAF.get()),
                        id(ModItems.SHADE_TOBACCO_LEAF.get()),
                        id(ModItems.WILD_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.BURLEY_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.DOKHA_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.SHADE_TOBACCO_LEAF_DRY.get())
                )
                .addStoryBoard("tobacco/homogenizing", TobacconistPonderStoryboards::homogenizing);

        helper.forComponents(
                        id(ModItems.STONE_CHAVETA.get()),
                        id(ModItems.COPPER_CHAVETA.get()),
                        id(ModItems.IRON_CHAVETA.get()),
                        id(ModItems.GOLD_CHAVETA.get()),
                        id(ModItems.DIAMOND_CHAVETA.get()),
                        id(ModItems.NETHERITE_CHAVETA.get()),
                        id(ModItems.WILD_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.BURLEY_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.DOKHA_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.SHADE_TOBACCO_LEAF_DRY.get()),
                        id(ModItems.TOBACCO_LOOSE_VIRGINIA.get())
                )
                .addStoryBoard("tobacco/processing", TobacconistPonderStoryboards::processing);

        helper.forComponents(
                        id(ModItems.BLENDED_TOBACCO.get()),
                        id(ModBlocks.TOBACCO_BARREL.get())
                )
                .addStoryBoard("tobacco/blending", TobacconistPonderStoryboards::blending);

        helper.forComponents(
                        id(ModItems.BOTTLED_AQUA_VITAE.get()),
                        id(ModItems.BOTTLED_MOLASSES_PLAIN.get()),
                        id(ModItems.SHISHA_TOBACCO.get())
                )
                .addStoryBoard("tobacco/flavoring", TobacconistPonderStoryboards::flavoring);

        helper.forComponents(
                        id(ModItems.CIGARETTE.get()),
                        id(ModItems.CIGAR.get())
                )
                .addStoryBoard("tobacco/assembly", TobacconistPonderStoryboards::assembly);

        helper.forComponents(id(ModBlocks.PRODUCTION_MONITOR.get()))
                .addStoryBoard("tobacco/production_monitor", TobacconistPonderStoryboards::productionMonitor);

        // Register the logistics tutorial on automatable processing blocks.
        helper.forComponents(
                        id(ModBlocks.TOBACCO_DRYING_RACK.get()),
                        id(ModBlocks.TOBACCO_BARREL.get()),
                        id(ModBlocks.FLUE_FIREBOX.get())
                )
                .addStoryBoard("tobacco/logistics", TobacconistPonderStoryboards::logistics);
    }

    private static ResourceLocation id(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem());
    }
}
