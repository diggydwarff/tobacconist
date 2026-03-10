package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class VirginiaCropBlock extends AbstractTallTobaccoCropBlock {

    public VirginiaCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected TobaccoGrowthHelper.Variety getVariety() {
        return TobaccoGrowthHelper.Variety.VIRGINIA;
    }

    @Override
    protected Item getLeafItem() {
        return ModItems.VIRGINIA_TOBACCO_LEAF.get();
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.VIRGINIA_TOBACCO_SEEDS.get();
    }
}