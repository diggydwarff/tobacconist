package com.diggydwarff.tobacconistmod.datagen;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TobacconistMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
/*
        simpleItem(ModItems.ROLLING_PAPER);
        simpleItem(ModItems.CIGAR);
        simpleItem(ModItems.CIGARETTE);
        simpleItem(ModItems.HOOKAH_HOSE);
        simpleItem(ModItems.SHISHA_TOBACCO);
        simpleItem(ModItems.WILD_TOBACCO_LEAF);
        simpleItem(ModItems.VIRGINIA_TOBACCO_LEAF);
        simpleItem(ModItems.BURLEY_TOBACCO_LEAF);
        simpleItem(ModItems.ORIENTAL_TOBACCO_LEAF);
        simpleItem(ModItems.DOKHA_TOBACCO_LEAF);
        simpleItem(ModItems.SHADE_TOBACCO_LEAF);
        simpleItem(ModItems.WILD_TOBACCO_LEAF_DRY);
        simpleItem(ModItems.VIRGINIA_TOBACCO_LEAF_DRY);
        simpleItem(ModItems.BURLEY_TOBACCO_LEAF_DRY);
        simpleItem(ModItems.ORIENTAL_TOBACCO_LEAF_DRY);
        simpleItem(ModItems.DOKHA_TOBACCO_LEAF_DRY);
        simpleItem(ModItems.SHADE_TOBACCO_LEAF_DRY);
        simpleItem(ModItems.WILD_TOBACCO_SEEDS);
        simpleItem(ModItems.VIRGINIA_TOBACCO_SEEDS);
        simpleItem(ModItems.BURLEY_TOBACCO_SEEDS);
        simpleItem(ModItems.ORIENTAL_TOBACCO_SEEDS);
        simpleItem(ModItems.DOKHA_TOBACCO_SEEDS);
        simpleItem(ModItems.SHADE_TOBACCO_SEEDS);

*/
        simpleBlockItemBlockTexture(ModBlocks.WILD_FLOWERING_TOBACCO);

    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(TobacconistMod.MODID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItemBlockTexture(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(TobacconistMod.MODID,"block/" + item.getId().getPath()));
    }

}
