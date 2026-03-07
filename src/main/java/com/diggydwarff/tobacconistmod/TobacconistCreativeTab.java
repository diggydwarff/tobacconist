package com.diggydwarff.tobacconistmod;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.diggydwarff.tobacconistmod.datagen.items.ModItems.*;
public class TobacconistCreativeTab  {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TobacconistMod.MODID);

    public static final RegistryObject<CreativeModeTab> COURSE_TAB = CREATIVE_MODE_TABS.register("tobacconistmod",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(CIGAR.get()))
                    .title(Component.translatable("creativetab.tobacconistmod"))
                    .displayItems((displayParameters, output) -> {

                        output.accept(ROLLING_PAPER.get());
                        output.accept(BAMBOO_CHARCOAL.get());
                        output.accept(CIGAR.get());
                        output.accept(CIGARETTE.get());
                        output.accept(HOOKAH_HOSE.get());
                        output.accept(SHISHA_TOBACCO.get());

                        output.accept(BOTTLED_MOLASSES_PLAIN.get());

                        output.accept(WILD_TOBACCO_LEAF.get());
                        output.accept(VIRGINIA_TOBACCO_LEAF.get());
                        output.accept(BURLEY_TOBACCO_LEAF.get());
                        output.accept(ORIENTAL_TOBACCO_LEAF.get());
                        output.accept(DOKHA_TOBACCO_LEAF.get());
                        output.accept(SHADE_TOBACCO_LEAF.get());

                        output.accept(WILD_TOBACCO_LEAF_DRY.get());
                        output.accept(VIRGINIA_TOBACCO_LEAF_DRY.get());
                        output.accept(BURLEY_TOBACCO_LEAF_DRY.get());
                        output.accept(ORIENTAL_TOBACCO_LEAF_DRY.get());
                        output.accept(DOKHA_TOBACCO_LEAF_DRY.get());
                        output.accept(SHADE_TOBACCO_LEAF_DRY.get());

                        output.accept(TOBACCO_LOOSE_WILD.get());
                        output.accept(TOBACCO_LOOSE_VIRGINIA.get());
                        output.accept(TOBACCO_LOOSE_BURLEY.get());
                        output.accept(TOBACCO_LOOSE_ORIENTAL.get());
                        output.accept(TOBACCO_LOOSE_DOKHA.get());
                        output.accept(TOBACCO_LOOSE_SHADE.get());

                        output.accept(ModBlocks.WILD_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.VIRGINIA_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.BURLEY_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.ORIENTAL_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.DOKHA_TOBACCO_CRATE.get());
                        output.accept(ModBlocks.SHADE_TOBACCO_CRATE.get());

                        output.accept(WILD_TOBACCO_SEEDS.get());
                        output.accept(VIRGINIA_TOBACCO_SEEDS.get());
                        output.accept(BURLEY_TOBACCO_SEEDS.get());
                        output.accept(ORIENTAL_TOBACCO_SEEDS.get());
                        output.accept(DOKHA_TOBACCO_SEEDS.get());
                        output.accept(SHADE_TOBACCO_SEEDS.get());


                        output.accept(ModBlocks.HOOKAH.get());
                        output.accept(ModBlocks.WILD_TOBACCO_CROP.get());
                        output.accept(ModBlocks.VIRGINIA_TOBACCO_CROP.get());
                        output.accept(ModBlocks.BURLEY_TOBACCO_CROP.get());
                        output.accept(ModBlocks.ORIENTAL_TOBACCO_CROP.get());
                        output.accept(ModBlocks.DOKHA_TOBACCO_CROP.get());
                        output.accept(ModBlocks.SHADE_TOBACCO_CROP.get());

                        output.accept(GOLD_SMOKING_PIPE.get());
                        output.accept(GEM_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(DIAMOND_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(LAPIS_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(EMERALD_ENCRUSTED_SMOKING_PIPE.get());
                        output.accept(EMERALD_AZTEC_SMOKING_PIPE.get());
                        output.accept(NETHERITE_SMOKING_PIPE.get());

                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
