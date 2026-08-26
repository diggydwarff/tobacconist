package com.diggydwarff.tobacconistmod.datagen.items;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static class Items {

        public static final TagKey<Item> TOBACCO_SEEDS = tag("tobacco_seeds");
        public static final TagKey<Item> RAW_TOBACCO_LEAVES = tag("raw_tobacco_leaves");
        public static final TagKey<Item> CURED_TOBACCO_LEAVES = tag("cured_tobacco_leaves");
        public static final TagKey<Item> TOBACCO_LEAVES = tag("tobacco_leaves");
        public static final TagKey<Item> LOOSE_TOBACCO = tag("loose_tobacco");
        public static final TagKey<Item> SMOKING_PIPES = tag("smoking_pipes");

        // Common tags are limited to cross-mod tobacco seed/crop concepts.
        public static final TagKey<Item> COMMON_TOBACCO_SEEDS = commonTag("seeds/tobacco");
        public static final TagKey<Item> COMMON_TOBACCO_CROPS = commonTag("crops/tobacco");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(TobacconistMod.MODID, name));
        }

        private static TagKey<Item> commonTag(String name) {
            return ItemTags.create(new ResourceLocation("c", name));
        }

    }

    public static class Blocks {

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(TobacconistMod.MODID, name));
        }

    }

}
