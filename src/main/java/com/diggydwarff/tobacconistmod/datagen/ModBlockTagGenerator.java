package com.diggydwarff.tobacconistmod.datagen;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class ModBlockTagGenerator  extends BlockTagsProvider {
    public ModBlockTagGenerator(DataGenerator output, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, TobacconistMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {

    }
}
