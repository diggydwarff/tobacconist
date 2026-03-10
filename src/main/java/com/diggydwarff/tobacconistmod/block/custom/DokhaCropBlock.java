package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class DokhaCropBlock extends AbstractTallTobaccoCropBlock {

    public DokhaCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected TobaccoGrowthHelper.Variety getVariety() {
        return TobaccoGrowthHelper.Variety.DOKHA;
    }

    @Override
    protected Item getLeafItem() {
        return ModItems.DOKHA_TOBACCO_LEAF.get();
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.DOKHA_TOBACCO_SEEDS.get();
    }
}