package com.diggydwarff.tobacconistmod.compat.create.ponder;

import com.google.common.collect.ImmutableList;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.IndustrialDryingRackBlock;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour.Mode;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Animated Ponder scenes covering Tobacconist Create workflows. */
public final class TobacconistPonderStoryboards {
    private TobacconistPonderStoryboards() {}

    public static void curing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_curing");

        BlockPos rack = util.grid().at(4, 1, 3);
        BlockPos fan = util.grid().at(1, 1, 3);
        BlockPos fanShaft = util.grid().at(0, 1, 3);
        BlockPos cog = util.grid().at(0, 1, 4);
        BlockPos utilitySpot = util.grid().at(5, 1, 5);
        BlockPos catalyst = util.grid().at(2, 1, 3);
        BlockPos belowRack = rack.below();

        scene.world().setBlock(rack, ModBlocks.TOBACCO_DRYING_RACK.get().defaultBlockState(), false);
        reveal(scene, util, rack);

        ItemStack rawLeaves = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class,
                be -> be.setItem(0, rawLeaves.copy()));
        scene.overlay().showControls(util.vector().topOf(rack), Pointing.DOWN, 35).withItem(rawLeaves.copy());
        scene.overlay().showText(75)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(rack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing.text_1").getString());
        scene.idle(85);

        // Air curing: Create airflow accelerates the ordinary covered/open-air cure.
        scene.world().setBlock(fan, AllBlocks.ENCASED_FAN.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST), false);
        scene.world().setBlock(fanShaft, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        scene.world().setBlock(cog, AllBlocks.COGWHEEL.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        Selection fanDrive = util.select().position(fan).add(util.select().position(fanShaft))
                .add(util.select().position(cog));
        scene.world().showSection(fanDrive, Direction.DOWN);
        scene.world().setKineticSpeed(fanDrive, 32);
        scene.effects().rotationDirectionIndicator(cog);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(fan))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing.text_2").getString());
        scene.idle(90);

        // Sun curing: clear the fan away and show the resulting cure on the same rack.
        scene.world().setBlock(fan, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(fanShaft, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(cog, Blocks.AIR.defaultBlockState(), false);
        ItemStack sunCured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(sunCured, TobaccoCuringHelper.CURE_SUN, 75);
        scene.overlay().showControls(util.vector().topOf(rack), Pointing.DOWN, 45).withItem(sunCured);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(rack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing.text_3").getString());
        scene.idle(90);

        // Fire curing: a lit campfire directly beneath the rack supplies smoke and heat.
        scene.world().setBlock(belowRack, Blocks.CAMPFIRE.defaultBlockState()
                .setValue(BlockStateProperties.LIT, true), false);
        ItemStack fireCured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(fireCured, TobaccoCuringHelper.CURE_FIRE, 75);
        scene.overlay().showControls(util.vector().centerOf(belowRack).add(0, .5, 0), Pointing.DOWN, 45)
                .withItem(fireCured);
        scene.overlay().showText(85)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(belowRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing.text_4").getString());
        scene.idle(95);

        // Create can accelerate fire curing by blowing campfire smoke directly across the rack.
        scene.world().setBlock(belowRack, Blocks.SMOOTH_STONE.defaultBlockState(), false);
        scene.world().setBlock(fan, AllBlocks.ENCASED_FAN.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST), false);
        scene.world().setBlock(fanShaft, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        scene.world().setBlock(cog, AllBlocks.COGWHEEL.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        scene.world().setBlock(catalyst, Blocks.CAMPFIRE.defaultBlockState()
                .setValue(BlockStateProperties.LIT, true), false);
        scene.world().showSection(fanDrive, Direction.DOWN);
        reveal(scene, util, catalyst);
        scene.world().setKineticSpeed(fanDrive, 32);
        scene.effects().rotationDirectionIndicator(cog);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(catalyst))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing.text_5").getString());
        scene.idle(100);

        // Flue curing: remove the smoke source and demonstrate indirect heat from the firebox.
        scene.world().setBlock(fan, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(fanShaft, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(cog, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(catalyst, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(utilitySpot, ModBlocks.FLUE_FIREBOX.get().defaultBlockState()
                .setValue(BlockStateProperties.LIT, true)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST), false);
        reveal(scene, util, utilitySpot);
        ItemStack flueCured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(flueCured, TobaccoCuringHelper.CURE_FLUE, 75);
        scene.overlay().showControls(util.vector().topOf(utilitySpot), Pointing.DOWN, 45).withItem(flueCured);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(utilitySpot))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing.text_6").getString());
        scene.idle(105);

        // Create blasting airflow from lava provides the same smoke-free cure at a faster rate.
        scene.world().setBlock(utilitySpot, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(fan, AllBlocks.ENCASED_FAN.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST), false);
        scene.world().setBlock(fanShaft, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        scene.world().setBlock(cog, AllBlocks.COGWHEEL.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        scene.world().setBlock(catalyst, Blocks.LAVA.defaultBlockState(), false);
        scene.world().showSection(fanDrive, Direction.DOWN);
        reveal(scene, util, catalyst);
        scene.world().setKineticSpeed(fanDrive, 32);
        scene.effects().rotationDirectionIndicator(cog);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(catalyst))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing.text_7").getString());
        scene.idle(100);

        // Finish with the Create automation layer shared by every rack method.
        scene.world().setBlock(fan, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(fanShaft, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(cog, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(catalyst, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(utilitySpot, AllBlocks.MECHANICAL_ARM.get().defaultBlockState(), false);
        scene.world().setKineticSpeed(util.select().position(utilitySpot), 32);
        ItemStack curedLeaves = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(curedLeaves, TobaccoCuringHelper.CURE_AIR, 75);
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class,
                be -> be.setItem(0, curedLeaves.copy()));
        scene.effects().indicateSuccess(rack);
        scene.overlay().showControls(util.vector().topOf(rack), Pointing.DOWN, 45).withItem(curedLeaves.copy());
        scene.overlay().showText(85)
                .placeNearTarget()
                .pointAt(util.vector().centerOf(utilitySpot))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing.text_8").getString());
        scene.idle(95);
    }

    public static void industrialCuring(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_industrial_curing");

        BlockPos lowerRack = util.grid().at(4, 1, 3);
        BlockPos upperRack = lowerRack.above();
        BlockPos lowerFan = util.grid().at(1, 1, 3);
        BlockPos upperFan = util.grid().at(1, 2, 3);
        BlockPos lowerShaft = util.grid().at(0, 1, 3);
        BlockPos upperShaft = util.grid().at(0, 2, 3);

        BlockState lowerState = ModBlocks.INDUSTRIAL_DRYING_RACK.get().defaultBlockState()
                .setValue(IndustrialDryingRackBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upperState = lowerState.setValue(IndustrialDryingRackBlock.HALF, DoubleBlockHalf.UPPER);
        scene.world().setBlock(lowerRack, lowerState, false);
        scene.world().setBlock(upperRack, upperState, false);
        scene.world().showSection(util.select().position(lowerRack).add(util.select().position(upperRack)), Direction.DOWN);

        ItemStack rawLeaves = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 32);
        scene.overlay().showControls(util.vector().topOf(lowerRack), Pointing.DOWN, 35).withItem(rawLeaves);
        scene.overlay().showText(85)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(lowerRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing.text_1").getString());
        scene.idle(95);

        scene.world().setBlock(lowerFan, AllBlocks.ENCASED_FAN.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST), false);
        scene.world().setBlock(upperFan, AllBlocks.ENCASED_FAN.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST), false);
        scene.world().setBlock(lowerShaft, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        scene.world().setBlock(upperShaft, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);

        Selection airflow = util.select().position(lowerFan).add(util.select().position(upperFan))
                .add(util.select().position(lowerShaft)).add(util.select().position(upperShaft));
        scene.world().showSection(airflow, Direction.EAST);
        scene.world().setKineticSpeed(airflow, 32);
        scene.effects().rotationDirectionIndicator(lowerShaft);
        scene.effects().rotationDirectionIndicator(upperShaft);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(upperFan))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing.text_2").getString());
        scene.idle(110);

        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(lowerRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing.text_3").getString());
        scene.effects().indicateSuccess(lowerRack);
        scene.idle(110);
    }

    public static void processing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_processing");

        BlockPos depotA = util.grid().at(2, 1, 3);
        BlockPos deployer = util.grid().at(2, 3, 3);
        BlockPos depotB = util.grid().at(5, 1, 3);
        BlockPos press = util.grid().at(5, 3, 3);
        BlockPos shaftA = util.grid().at(2, 3, 4);
        BlockPos driveA = util.grid().at(2, 3, 5);
        BlockPos shaftB = util.grid().at(5, 3, 4);
        BlockPos driveB = util.grid().at(5, 3, 5);

        scene.world().setBlock(depotA, AllBlocks.DEPOT.get().defaultBlockState(), false);
        scene.world().setBlock(deployer, AllBlocks.DEPLOYER.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.DOWN), false);
        scene.world().setBlock(depotB, AllBlocks.DEPOT.get().defaultBlockState(), false);
        scene.world().setBlock(press, AllBlocks.MECHANICAL_PRESS.get().defaultBlockState(), false);
        scene.world().setBlock(shaftA, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);
        scene.world().setBlock(driveA, AllBlocks.COGWHEEL.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);
        scene.world().setBlock(shaftB, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);
        scene.world().setBlock(driveB, AllBlocks.COGWHEEL.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);

        Selection deployerAssembly = util.select().position(depotA).add(util.select().position(deployer))
                .add(util.select().position(shaftA)).add(util.select().position(driveA));
        scene.world().showSection(deployerAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(deployer).add(util.select().position(shaftA))
                .add(util.select().position(driveA)), 32);
        scene.effects().rotationDirectionIndicator(driveA);

        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        ItemStack loose = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        ItemStack rough = loose.copy();
        TobaccoCuringHelper.setCutType(rough, TobaccoCuringHelper.CUT_ROUGH);
        ItemStack flake = loose.copy();
        TobaccoCuringHelper.setCutType(flake, TobaccoCuringHelper.CUT_FLAKE);
        ItemStack chaveta = new ItemStack(ModItems.STONE_CHAVETA.get());
        scene.world().modifyBlockEntityNBT(util.select().position(deployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", chaveta.saveOptional(scene.world().getHolderLookupProvider())));
        scene.world().createItemOnBeltLike(depotA, Direction.UP, cured);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(depotA))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_processing.text_1").getString());
        scene.idle(35);
        scene.world().moveDeployer(deployer, 1, 25);
        scene.idle(26);
        scene.world().removeItemsFromBelt(depotA);
        scene.world().createItemOnBeltLike(depotA, Direction.UP, loose);
        scene.effects().indicateSuccess(depotA);
        scene.world().moveDeployer(deployer, -1, 25);
        scene.idle(35);

        Selection pressAssembly = util.select().position(depotB).add(util.select().position(press))
                .add(util.select().position(shaftB)).add(util.select().position(driveB));
        scene.world().showSection(pressAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(press).add(util.select().position(shaftB))
                .add(util.select().position(driveB)), 32);
        scene.effects().rotationDirectionIndicator(driveB);
        scene.world().createItemOnBeltLike(depotB, Direction.UP, rough);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(press))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_processing.text_2").getString());
        scene.idle(30);
        scene.world().modifyBlockEntity(press, MechanicalPressBlockEntity.class,
                pte -> pte.getPressingBehaviour().start(Mode.BELT));
        scene.idle(30);
        scene.world().modifyBlockEntity(press, MechanicalPressBlockEntity.class,
                pte -> pte.getPressingBehaviour().makePressingParticleEffect(
                        util.vector().centerOf(depotB).add(0, 8 / 16f, 0), rough));
        scene.world().removeItemsFromBelt(depotB);
        scene.world().createItemOnBeltLike(depotB, Direction.UP, flake);
        scene.effects().indicateSuccess(depotB);
        scene.idle(65);
    }

    public static void homogenizing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_homogenizing");

        BlockPos basin = util.grid().at(2, 1, 3);
        BlockPos mixer = util.grid().at(2, 3, 3);
        BlockPos idlerCog = util.grid().at(2, 3, 4);
        BlockPos drive = util.grid().at(2, 3, 5);
        BlockPos redstone = util.grid().at(3, 3, 3);

        scene.world().setBlock(basin, AllBlocks.BASIN.get().defaultBlockState(), false);
        scene.world().setBlock(mixer, AllBlocks.MECHANICAL_MIXER.get().defaultBlockState(), false);
        scene.world().setBlock(idlerCog, AllBlocks.COGWHEEL.get().defaultBlockState(), false);
        scene.world().setBlock(drive, AllBlocks.COGWHEEL.get().defaultBlockState(), false);

        Selection mixerAssembly = util.select().position(basin).add(util.select().position(mixer))
                .add(util.select().position(idlerCog)).add(util.select().position(drive));
        scene.world().showSection(mixerAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(mixer).add(util.select().position(idlerCog))
                .add(util.select().position(drive)), 32);
        scene.effects().rotationDirectionIndicator(drive);

        ItemStack low = rawLeafWithQuality(49, 32);
        ItemStack high = rawLeafWithQuality(59, 32);
        scene.world().createItemOnBeltLike(basin, Direction.UP, low);
        scene.world().createItemOnBeltLike(basin, Direction.UP, high);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_homogenizing.text_1").getString());
        scene.idle(110);

        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(mixer))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_homogenizing.text_2").getString());
        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, pte -> pte.startProcessingBasin());
        scene.idle(65);
        scene.world().removeItemsFromBelt(basin);
        ItemStack standardized = rawLeafWithQuality(54, 64);
        visualizeBasinResult(scene, util, basin, standardized);
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 50).withItem(standardized);
        scene.effects().indicateSuccess(basin);
        scene.idle(70);

        clearBasinResult(scene, util, basin);
        scene.world().setBlock(redstone, Blocks.REDSTONE_BLOCK.defaultBlockState(), false);
        scene.world().showSection(util.select().position(redstone), Direction.DOWN);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(redstone))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_homogenizing.text_3").getString());
        scene.idle(110);

        scene.overlay().showText(100)
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_homogenizing.text_4").getString());
        scene.idle(110);
    }

    private static ItemStack rawLeafWithQuality(int quality, int count) {
        ItemStack stack = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), count);
        LegacyItemTags.getOrCreateTag(stack).putInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY, quality);
        return stack;
    }

    public static void blending(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_blending");

        BlockPos basin = util.grid().at(2, 1, 3);
        BlockPos mixer = util.grid().at(2, 3, 3);
        BlockPos idlerCog = util.grid().at(2, 3, 4);
        BlockPos drive = util.grid().at(2, 3, 5);
        BlockPos barrel = util.grid().at(5, 1, 3);
        BlockPos packager = util.grid().at(5, 1, 4);
        BlockPos arm = util.grid().at(4, 1, 5);

        scene.world().setBlock(basin, AllBlocks.BASIN.get().defaultBlockState(), false);
        scene.world().setBlock(mixer, AllBlocks.MECHANICAL_MIXER.get().defaultBlockState(), false);
        scene.world().setBlock(idlerCog, AllBlocks.COGWHEEL.get().defaultBlockState(), false);
        scene.world().setBlock(drive, AllBlocks.COGWHEEL.get().defaultBlockState(), false);
        scene.world().setBlock(barrel, ModBlocks.TOBACCO_BARREL.get().defaultBlockState(), false);
        scene.world().setBlock(packager, AllBlocks.PACKAGER.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.SOUTH), false);
        scene.world().setBlock(arm, AllBlocks.MECHANICAL_ARM.get().defaultBlockState(), false);

        Selection mixerAssembly = util.select().position(basin).add(util.select().position(mixer))
                .add(util.select().position(idlerCog)).add(util.select().position(drive));
        scene.world().showSection(mixerAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(mixer).add(util.select().position(idlerCog))
                .add(util.select().position(drive)), 32);
        scene.effects().rotationDirectionIndicator(drive);

        ItemStack virginia = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        ItemStack burley = new ItemStack(ModItems.TOBACCO_LOOSE_BURLEY.get());
        ItemStack blend = new ItemStack(ModItems.BLENDED_TOBACCO.get());
        scene.world().createItemOnBeltLike(basin, Direction.UP, virginia);
        scene.world().createItemOnBeltLike(basin, Direction.UP, burley);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_blending.text_1").getString());
        scene.idle(35);
        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, pte -> pte.startProcessingBasin());
        scene.idle(65);
        scene.world().removeItemsFromBelt(basin);
        visualizeBasinResult(scene, util, basin, blend);
        scene.effects().indicateSuccess(basin);
        scene.overlay().showText(70)
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_blending.text_2").getString());
        scene.idle(80);

        Selection barrelLine = util.select().position(barrel).add(util.select().position(packager)).add(util.select().position(arm));
        scene.world().showSection(barrelLine, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(arm), 32);
        scene.rotateCameraY(-15);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(barrel))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_blending.text_3").getString());
        scene.idle(100);
    }

    public static void flavoring(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_flavoring");

        BlockPos burner = util.grid().at(2, 1, 3);
        BlockPos basin = util.grid().at(2, 2, 3);
        BlockPos mixer = util.grid().at(2, 4, 3);
        BlockPos idlerCog = util.grid().at(2, 4, 4);
        BlockPos drive = util.grid().at(2, 4, 5);
        BlockPos depot = util.grid().at(5, 1, 3);
        BlockPos spout = util.grid().at(5, 3, 3);

        scene.world().setBlock(burner, AllBlocks.BLAZE_BURNER.get().defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), false);
        scene.world().setBlock(basin, AllBlocks.BASIN.get().defaultBlockState(), false);
        scene.world().setBlock(mixer, AllBlocks.MECHANICAL_MIXER.get().defaultBlockState(), false);
        scene.world().setBlock(idlerCog, AllBlocks.COGWHEEL.get().defaultBlockState(), false);
        scene.world().setBlock(drive, AllBlocks.COGWHEEL.get().defaultBlockState(), false);
        scene.world().setBlock(depot, AllBlocks.DEPOT.get().defaultBlockState(), false);
        scene.world().setBlock(spout, AllBlocks.SPOUT.get().defaultBlockState(), false);

        Selection heatedMixer = util.select().position(burner).add(util.select().position(basin))
                .add(util.select().position(mixer)).add(util.select().position(idlerCog)).add(util.select().position(drive));
        scene.world().showSection(heatedMixer, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(mixer).add(util.select().position(idlerCog))
                .add(util.select().position(drive)), 32);
        scene.effects().rotationDirectionIndicator(drive);

        scene.world().createItemOnBeltLike(basin, Direction.UP, new ItemStack(Items.SUGAR));
        scene.world().createItemOnBeltLike(basin, Direction.UP, new ItemStack(Items.WHEAT));
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.LEFT, 35).withItem(new ItemStack(Items.WATER_BUCKET));
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_flavoring.text_1").getString());
        scene.idle(30);
        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, pte -> pte.startProcessingBasin());
        scene.idle(65);
        scene.world().removeItemsFromBelt(basin);
        ItemStack aquaVitae = new ItemStack(ModItems.BOTTLED_AQUA_VITAE.get());
        visualizeBasinResult(scene, util, basin, aquaVitae);
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 45)
                .withItem(aquaVitae);
        scene.effects().indicateSuccess(basin);
        scene.idle(30);

        clearBasinResult(scene, util, basin);
        scene.world().createItemOnBeltLike(basin, Direction.UP, new ItemStack(Items.APPLE));
        scene.overlay().showControls(util.vector().topOf(basin).add(.35, 0, 0), Pointing.DOWN, 40)
                .withItem(aquaVitae);
        scene.overlay().showText(85)
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_flavoring.text_2").getString());
        scene.idle(25);
        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, pte -> pte.startProcessingBasin());
        scene.idle(55);
        scene.world().removeItemsFromBelt(basin);
        ItemStack appleEssence = BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR.getEssenceStack();
        visualizeBasinResult(scene, util, basin, appleEssence);
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 45)
                .withItem(appleEssence);
        scene.effects().indicateSuccess(basin);
        scene.idle(35);

        scene.world().showSection(util.select().position(depot).add(util.select().position(spout)), Direction.DOWN);
        ItemStack loose = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        scene.world().createItemOnBeltLike(depot, Direction.UP, loose);
        scene.overlay().showControls(util.vector().centerOf(spout).add(.4, 0, 0), Pointing.RIGHT, 40)
                .withItem(appleEssence);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(spout))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_flavoring.text_3").getString());
        scene.idle(70);
        scene.effects().indicateSuccess(depot);
        scene.idle(35);

        scene.rotateCameraY(-15);
        clearBasinResult(scene, util, basin);
        scene.world().removeItemsFromBelt(basin);
        scene.world().createItemOnBeltLike(basin, Direction.UP, loose);
        scene.overlay().showControls(util.vector().topOf(basin).add(.35, 0, 0), Pointing.DOWN, 40)
                .withItem(BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR.getStack());
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_flavoring.text_4").getString());
        scene.idle(30);
        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, pte -> pte.startProcessingBasin());
        scene.idle(65);
        scene.world().removeItemsFromBelt(basin);
        ItemStack shisha = new ItemStack(ModItems.SHISHA_TOBACCO.get());
        visualizeBasinResult(scene, util, basin, shisha);
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 50)
                .withItem(shisha);
        scene.effects().indicateSuccess(basin);
        scene.idle(70);
    }

    public static void cigaretteProduction(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginLarge(builder, "tobacconist_cigarette_production");

        BlockPos inputBelt = util.grid().at(3, 1, 0);
        BlockPos firstCutBelt = util.grid().at(3, 1, 1);
        BlockPos secondCutBelt = util.grid().at(3, 1, 2);
        BlockPos rollingBelt = util.grid().at(3, 1, 3);
        BlockPos cornerBelt = util.grid().at(3, 1, 4);
        BlockPos pressBelt = util.grid().at(5, 1, 4);
        BlockPos outputBelt = util.grid().at(6, 1, 4);
        BlockPos firstCutDeployer = util.grid().at(3, 3, 1);
        BlockPos secondCutDeployer = util.grid().at(3, 3, 2);
        BlockPos rollingDeployer = util.grid().at(3, 3, 3);
        BlockPos press = util.grid().at(5, 3, 4);
        BlockPos outputBarrel = util.grid().at(7, 2, 4);

        Selection factory = util.select().fromTo(0, 0, 0, 7, 4, 7);
        scene.world().showSection(factory, Direction.DOWN);
        scene.world().setKineticSpeed(factory, 32);
        scene.effects().rotationDirectionIndicator(util.grid().at(0, 3, 1));
        scene.effects().rotationDirectionIndicator(util.grid().at(5, 3, 6));
        scene.idle(20);

        ItemStack chaveta = new ItemStack(ModItems.STONE_CHAVETA.get());
        scene.world().modifyBlockEntityNBT(util.select().position(firstCutDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", chaveta.saveOptional(scene.world().getHolderLookupProvider())));
        scene.world().modifyBlockEntityNBT(util.select().position(secondCutDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", chaveta.saveOptional(scene.world().getHolderLookupProvider())));

        ItemStack paper = new ItemStack(ModItems.ROLLING_PAPER.get());
        scene.world().modifyBlockEntityNBT(util.select().position(rollingDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", paper.saveOptional(scene.world().getHolderLookupProvider())));

        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        ItemStack rough = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        TobaccoCuringHelper.setCutType(rough, TobaccoCuringHelper.CUT_ROUGH);
        ItemStack shag = rough.copy();
        TobaccoCuringHelper.setCutType(shag, TobaccoCuringHelper.CUT_SHAG);
        ItemStack incomplete = new ItemStack(ModItems.INCOMPLETE_CIGARETTE.get());
        ItemStack cigarette = new ItemStack(ModItems.CIGARETTE.get());

        scene.world().createItemOnBeltLike(inputBelt, Direction.UP, cured);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(firstCutBelt))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_1").getString());
        scene.idle(35);
        scene.world().removeItemsFromBelt(inputBelt);
        scene.world().createItemOnBeltLike(firstCutBelt, Direction.UP, cured);
        runDeployerStep(scene, firstCutBelt, firstCutDeployer, rough);
        scene.idle(20);

        scene.world().createItemOnBeltLike(secondCutBelt, Direction.UP, rough);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(secondCutBelt))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_2").getString());
        scene.idle(25);
        runDeployerStep(scene, secondCutBelt, secondCutDeployer, shag);
        scene.idle(25);

        scene.world().createItemOnBeltLike(rollingBelt, Direction.UP, shag);
        scene.overlay().showControls(util.vector().topOf(rollingDeployer), Pointing.DOWN, 40).withItem(paper);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(rollingBelt))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_3").getString());
        scene.idle(30);
        runDeployerStep(scene, rollingBelt, rollingDeployer, incomplete);
        scene.idle(20);

        scene.world().createItemOnBeltLike(cornerBelt, Direction.UP, incomplete);
        scene.idle(20);
        scene.world().removeItemsFromBelt(cornerBelt);
        scene.world().createItemOnBeltLike(pressBelt, Direction.UP, incomplete);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(press))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_4").getString());
        scene.idle(30);
        runPressStep(scene, pressBelt, press, cigarette);
        scene.idle(25);

        scene.world().removeItemsFromBelt(pressBelt);
        scene.world().createItemOnBeltLike(outputBelt, Direction.UP, cigarette);
        scene.effects().indicateSuccess(outputBarrel);
        scene.overlay().showText(115)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(outputBelt))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_5").getString());
        scene.overlay().showControls(util.vector().topOf(outputBelt), Pointing.DOWN, 55).withItem(cigarette);
        scene.idle(125);
    }

    public static void cigarProduction(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginLarge(builder, "tobacconist_cigar_production");

        BlockPos inputBelt = util.grid().at(3, 1, 0);
        BlockPos cutBelt = util.grid().at(3, 1, 1);
        BlockPos wrapperBelt = util.grid().at(3, 1, 2);
        BlockPos travelBelt = util.grid().at(3, 1, 3);
        BlockPos cornerBelt = util.grid().at(3, 1, 4);
        BlockPos pressBelt = util.grid().at(5, 1, 4);
        BlockPos outputBelt = util.grid().at(6, 1, 4);
        BlockPos cutDeployer = util.grid().at(3, 3, 1);
        BlockPos wrapperDeployer = util.grid().at(3, 3, 2);
        BlockPos press = util.grid().at(5, 3, 4);
        BlockPos outputBarrel = util.grid().at(7, 2, 4);

        Selection factory = util.select().fromTo(0, 0, 0, 7, 4, 7);
        scene.world().showSection(factory, Direction.DOWN);
        scene.world().setKineticSpeed(factory, 32);
        scene.effects().rotationDirectionIndicator(util.grid().at(0, 3, 1));
        scene.effects().rotationDirectionIndicator(util.grid().at(5, 3, 6));
        scene.idle(20);

        ItemStack chaveta = new ItemStack(ModItems.STONE_CHAVETA.get());
        scene.world().modifyBlockEntityNBT(util.select().position(cutDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", chaveta.saveOptional(scene.world().getHolderLookupProvider())));

        ItemStack wrapper = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        scene.world().modifyBlockEntityNBT(util.select().position(wrapperDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", wrapper.saveOptional(scene.world().getHolderLookupProvider())));

        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        ItemStack rough = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        TobaccoCuringHelper.setCutType(rough, TobaccoCuringHelper.CUT_ROUGH);
        ItemStack incomplete = new ItemStack(ModItems.INCOMPLETE_CIGAR.get());
        ItemStack cigar = new ItemStack(ModItems.CIGAR.get());

        scene.world().createItemOnBeltLike(inputBelt, Direction.UP, cured);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(cutBelt))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_1").getString());
        scene.idle(35);
        scene.world().removeItemsFromBelt(inputBelt);
        scene.world().createItemOnBeltLike(cutBelt, Direction.UP, cured);
        runDeployerStep(scene, cutBelt, cutDeployer, rough);
        scene.idle(25);

        scene.world().createItemOnBeltLike(wrapperBelt, Direction.UP, rough);
        scene.overlay().showControls(util.vector().topOf(wrapperDeployer), Pointing.DOWN, 45).withItem(wrapper);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(wrapperBelt))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_2").getString());
        scene.idle(35);
        runDeployerStep(scene, wrapperBelt, wrapperDeployer, incomplete);
        scene.idle(25);

        scene.world().createItemOnBeltLike(travelBelt, Direction.UP, incomplete);
        scene.idle(15);
        scene.world().removeItemsFromBelt(travelBelt);
        scene.world().createItemOnBeltLike(cornerBelt, Direction.UP, incomplete);
        scene.idle(15);
        scene.world().removeItemsFromBelt(cornerBelt);
        scene.world().createItemOnBeltLike(pressBelt, Direction.UP, incomplete);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(press))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_3").getString());
        scene.idle(30);
        runPressStep(scene, pressBelt, press, cigar);
        scene.idle(30);

        scene.overlay().showText(110)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(pressBelt))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_4").getString());
        scene.idle(115);

        scene.world().removeItemsFromBelt(pressBelt);
        scene.world().createItemOnBeltLike(outputBelt, Direction.UP, cigar);
        scene.effects().indicateSuccess(outputBarrel);
        scene.overlay().showText(115)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(outputBelt))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_5").getString());
        scene.overlay().showControls(util.vector().topOf(outputBelt), Pointing.DOWN, 55).withItem(cigar);
        scene.idle(125);
    }

    private static void runDeployerStep(CreateSceneBuilder scene, BlockPos depot, BlockPos deployer, ItemStack result) {
        scene.world().moveDeployer(deployer, 1, 20);
        scene.idle(21);
        scene.world().removeItemsFromBelt(depot);
        scene.world().createItemOnBeltLike(depot, Direction.UP, result);
        scene.world().moveDeployer(deployer, -1, 20);
        scene.effects().indicateSuccess(depot);
        scene.idle(22);
        scene.world().removeItemsFromBelt(depot);
    }

    private static void runPressStep(CreateSceneBuilder scene, BlockPos depot, BlockPos press, ItemStack result) {
        scene.world().modifyBlockEntity(press, MechanicalPressBlockEntity.class,
                pte -> pte.getPressingBehaviour().start(Mode.BELT));
        scene.idle(30);
        scene.world().removeItemsFromBelt(depot);
        scene.world().createItemOnBeltLike(depot, Direction.UP, result);
        scene.effects().indicateSuccess(depot);
    }

    public static void productionMonitor(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_production_monitor");

        BlockPos lowerBarrel = util.grid().at(1, 1, 2);
        BlockPos hopper = util.grid().at(1, 2, 2);
        BlockPos monitor = util.grid().at(2, 2, 2);
        BlockPos lamp = util.grid().at(3, 2, 2);
        BlockPos displayLink = util.grid().at(2, 2, 3);
        BlockPos displayBacking = util.grid().at(1, 3, 2);
        BlockPos nixie = util.grid().at(1, 4, 2);

        Selection transport = util.select().position(lowerBarrel).add(util.select().position(hopper));
        scene.world().showSection(transport, Direction.DOWN);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(hopper))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_production_monitor.text_1").getString());
        scene.idle(100);

        scene.world().showSection(util.select().position(monitor), Direction.DOWN);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(monitor))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_production_monitor.text_2").getString());
        scene.idle(115);

        ItemStack leaves = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 16);
        scene.overlay().showControls(util.vector().topOf(hopper), Pointing.DOWN, 45).withItem(leaves);
        scene.effects().indicateSuccess(monitor);
        scene.idle(45);

        Selection display = util.select().position(displayLink)
                .add(util.select().position(displayBacking))
                .add(util.select().position(nixie));
        scene.world().showSection(display, Direction.DOWN);
        scene.overlay().showText(115)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(nixie))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_production_monitor.text_3").getString());
        scene.idle(125);

        scene.world().showSection(util.select().position(lamp), Direction.DOWN);
        scene.world().setBlock(lamp, Blocks.REDSTONE_LAMP.defaultBlockState()
                .setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(lamp);
        scene.overlay().showText(115)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(lamp))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_production_monitor.text_4").getString());
        scene.idle(125);
    }

    public static void productionMonitorBelt(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_production_monitor_belt");

        BlockPos beltStart = util.grid().at(5, 1, 2);
        BlockPos beltMonitorPoint = util.grid().at(3, 1, 2);
        BlockPos beltOutput = util.grid().at(1, 1, 2);
        BlockPos monitor = util.grid().at(3, 1, 3);
        BlockPos repeater = util.grid().at(3, 1, 4);
        BlockPos lamp = util.grid().at(3, 1, 5);
        BlockPos outputBarrel = util.grid().at(0, 2, 2);
        BlockPos outputFunnel = util.grid().at(1, 2, 2);

        Selection beltLine = util.select().fromTo(0, 0, 0, 5, 4, 2)
                .add(util.select().position(outputBarrel))
                .add(util.select().position(outputFunnel));
        scene.world().showSection(beltLine, Direction.DOWN);
        scene.world().setKineticSpeed(beltLine, 32);
        scene.effects().rotationDirectionIndicator(util.grid().at(5, 1, 0));
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(beltMonitorPoint))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_production_monitor_belt.text_1").getString());
        scene.idle(105);

        Selection control = util.select().position(monitor)
                .add(util.select().position(repeater))
                .add(util.select().position(lamp));
        scene.world().showSection(control, Direction.DOWN);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(monitor))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_production_monitor_belt.text_2").getString());
        scene.idle(70);

        ItemStack tobacco = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get(), 16);
        scene.world().createItemOnBeltLike(beltStart, Direction.UP, tobacco);
        scene.idle(25);
        scene.world().removeItemsFromBelt(beltStart);
        scene.world().createItemOnBeltLike(beltMonitorPoint, Direction.UP, tobacco);
        scene.effects().indicateSuccess(monitor);
        scene.overlay().showText(110)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(beltMonitorPoint))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_production_monitor_belt.text_3").getString());
        scene.idle(65);

        scene.world().removeItemsFromBelt(beltMonitorPoint);
        scene.world().createItemOnBeltLike(beltOutput, Direction.UP, tobacco);
        scene.world().setBlock(lamp, Blocks.REDSTONE_LAMP.defaultBlockState()
                .setValue(BlockStateProperties.LIT, true), false);
        scene.effects().indicateRedstone(lamp);
        scene.overlay().showText(115)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(lamp))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_production_monitor_belt.text_4").getString());
        scene.idle(125);
    }

    public static void logistics(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_logistics");

        BlockPos chest = util.grid().at(1, 1, 4);
        BlockPos stockLink = util.grid().at(1, 2, 4);
        BlockPos sourcePackager = util.grid().at(2, 1, 4);
        BlockPos destinationPackager = util.grid().at(4, 1, 3);
        BlockPos gauge = util.grid().at(4, 2, 3);
        BlockPos rack = util.grid().at(5, 1, 3);
        BlockPos arm = util.grid().at(5, 1, 5);
        BlockPos outputDepot = util.grid().at(3, 1, 5);

        scene.world().setBlock(chest, Blocks.CHEST.defaultBlockState(), false);
        scene.world().setBlock(stockLink, AllBlocks.STOCK_LINK.get().defaultBlockState(), false);
        scene.world().setBlock(sourcePackager, AllBlocks.PACKAGER.get().defaultBlockState(), false);
        scene.world().setBlock(destinationPackager, AllBlocks.PACKAGER.get().defaultBlockState(), false);
        scene.world().setBlock(gauge, AllBlocks.FACTORY_GAUGE.get().defaultBlockState(), false);
        scene.world().setBlock(rack, ModBlocks.TOBACCO_DRYING_RACK.get().defaultBlockState(), false);
        scene.world().setBlock(arm, AllBlocks.MECHANICAL_ARM.get().defaultBlockState(), false);
        scene.world().setBlock(outputDepot, AllBlocks.DEPOT.get().defaultBlockState(), false);

        Selection sourceNetwork = util.select().position(chest).add(util.select().position(stockLink));
        scene.world().showSection(sourceNetwork, Direction.DOWN);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(stockLink))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_logistics.text_1").getString());
        scene.idle(90);

        scene.world().showSection(util.select().position(sourcePackager).add(util.select().position(destinationPackager)), Direction.DOWN);
        reveal(scene, util, rack);
        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        scene.overlay().showControls(util.vector().topOf(sourcePackager), Pointing.DOWN, 35).withItem(raw.copy());
        scene.idle(20);
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.effects().indicateSuccess(rack);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(destinationPackager))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_logistics.text_2").getString());
        scene.idle(105);

        scene.world().showSection(util.select().position(arm).add(util.select().position(outputDepot)), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(arm), 32);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.effects().indicateSuccess(rack);
        scene.idle(15);
        scene.world().createItemOnBeltLike(outputDepot, Direction.UP, cured);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(arm))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_logistics.text_3").getString());
        scene.idle(105);

        reveal(scene, util, gauge);
        scene.overlay().showControls(util.vector().topOf(gauge), Pointing.DOWN, 40)
                .withItem(new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()));
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(gauge))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_logistics.text_4").getString());
        scene.idle(110);

        scene.rotateCameraY(15);
        scene.overlay().showControls(util.vector().topOf(chest), Pointing.DOWN, 45)
                .withItem(new ItemStack(ModItems.TOBACCO_BOX.get()));
        scene.overlay().showText(80)
                .placeNearTarget()
                .pointAt(util.vector().topOf(chest))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_logistics.text_5").getString());
        scene.idle(90);
    }

    private static void visualizeBasinResult(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos basin, ItemStack stack) {
        scene.world().modifyBlockEntityNBT(util.select().position(basin), BasinBlockEntity.class, nbt ->
                nbt.put("VisualizedItems", NBTHelper.writeCompoundList(
                        ImmutableList.of(IntAttached.with(1, stack)),
                        entry -> (CompoundTag) entry.getValue().saveOptional(scene.world().getHolderLookupProvider()))));
    }

    private static void clearBasinResult(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos basin) {
        scene.world().modifyBlockEntityNBT(util.select().position(basin), BasinBlockEntity.class,
                nbt -> nbt.remove("VisualizedItems"));
    }

    private static CreateSceneBuilder beginLarge(SceneBuilder builder, String id) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(id, Component.translatable("tobacconistmod.ponder." + id + ".header").getString());
        scene.configureBasePlate(0, 0, 8);
        scene.scaleSceneView(0.90f);
        scene.setSceneOffsetY(-1);
        scene.rotateCameraY(180);
        scene.showBasePlate();
        scene.idle(10);
        return scene;
    }

    private static CreateSceneBuilder begin(SceneBuilder builder, String id) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(id, Component.translatable("tobacconistmod.ponder." + id + ".header").getString());
        scene.configureBasePlate(0, 0, 6);
        scene.scaleSceneView(0.96f);
        scene.setSceneOffsetY(-1);
        scene.showBasePlate();
        scene.idle(10);
        return scene;
    }

    private static void reveal(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos pos) {
        scene.world().showSection(util.select().position(pos), Direction.DOWN);
        scene.idle(5);
    }
}
