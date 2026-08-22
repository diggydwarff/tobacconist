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
        helper.forComponents(
                        id(ModItems.VIRGINIA_TOBACCO_SEEDS.get()),
                        id(ModItems.VIRGINIA_TOBACCO_LEAF.get())
                )
                .addStoryBoard("tobacco/harvesting", TobacconistPonderStoryboards::harvesting);

        helper.forComponents(
                        id(ModBlocks.TOBACCO_DRYING_RACK.get()),
                        id(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get())
                )
                .addStoryBoard("tobacco/curing", TobacconistPonderStoryboards::curing);

        helper.forComponents(
                        id(ModItems.STONE_CHAVETA.get()),
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

        helper.forComponents(
                        id(ModItems.TOBACCO_BOX.get()),
                        id(ModBlocks.FLUE_FIREBOX.get()),
                        id(ModBlocks.HOOKAH.get())
                )
                .addStoryBoard("tobacco/logistics", TobacconistPonderStoryboards::logistics);
    }

    private static ResourceLocation id(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem());
    }
}
