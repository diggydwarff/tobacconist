package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class BurleyCropBlock extends AbstractTallTobaccoCropBlock {

    public BurleyCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected TobaccoGrowthHelper.Variety getVariety() {
        return TobaccoGrowthHelper.Variety.BURLEY;
    }

    @Override
    protected Item getLeafItem() {
        return ModItems.BURLEY_TOBACCO_LEAF.get();
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.BURLEY_TOBACCO_SEEDS.get();
    }
}