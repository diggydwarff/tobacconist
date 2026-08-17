package com.diggydwarff.tobacconistmod.datagen;

import java.util.function.Supplier;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

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

    private ItemModelBuilder simpleItem(Supplier<Item> item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.get());
        return withExistingParent(id.getPath(), ResourceLocation.withDefaultNamespace("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "item/" + id.getPath()));
    }

    private ItemModelBuilder simpleBlockItemBlockTexture(Supplier<Block> block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block.get());
        return withExistingParent(id.getPath(), ResourceLocation.withDefaultNamespace("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "block/" + id.getPath()));
    }

}
