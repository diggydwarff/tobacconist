package com.diggydwarff.tobacconistmod.datagen.items.custom;


import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class TallCropSeedsItem extends ItemNameBlockItem {

    private final EnumProperty<DoubleBlockHalf> halfProperty;

    public TallCropSeedsItem(Block cropBlock, Properties props, EnumProperty<DoubleBlockHalf> halfProperty) {
        super(cropBlock, props);
        this.halfProperty = halfProperty;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state == null) return null;

        // Only set it if that blockstate actually has this property
        if (state.hasProperty(halfProperty)) {
            return state.setValue(halfProperty, DoubleBlockHalf.LOWER);
        }

        return state;
    }
}
