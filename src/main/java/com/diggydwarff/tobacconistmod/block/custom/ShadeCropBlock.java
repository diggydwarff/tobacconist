package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class ShadeCropBlock extends AbstractTallTobaccoCropBlock {

    public ShadeCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected TobaccoGrowthHelper.Variety getVariety() {
        return TobaccoGrowthHelper.Variety.SHADE;
    }

    @Override
    protected Item getLeafItem() {
        return ModItems.SHADE_TOBACCO_LEAF.get();
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.SHADE_TOBACCO_SEEDS.get();
    }
}