package com.diggydwarff.tobacconistmod.datagen;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class ModItemTagGenerator extends ItemTagsProvider {
    public ModItemTagGenerator(DataGenerator generator, net.minecraft.data.tags.BlockTagsProvider blocks,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, blocks, TobacconistMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {

    }

}
