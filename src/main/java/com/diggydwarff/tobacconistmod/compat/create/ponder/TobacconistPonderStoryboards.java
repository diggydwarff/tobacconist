package com.diggydwarff.tobacconistmod.compat.create.ponder;

import com.google.common.collect.ImmutableList;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
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

    public static void assembly(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_assembly");

        BlockPos depot = util.grid().at(2, 1, 3);
        BlockPos deployer = util.grid().at(2, 3, 3);
        BlockPos deployerShaft = util.grid().at(2, 3, 4);
        BlockPos deployerDrive = util.grid().at(2, 3, 5);
        BlockPos pressDepot = util.grid().at(5, 1, 3);
        BlockPos press = util.grid().at(5, 3, 3);
        BlockPos pressShaft = util.grid().at(5, 3, 4);
        BlockPos pressDrive = util.grid().at(5, 3, 5);

        scene.world().setBlock(depot, AllBlocks.DEPOT.get().defaultBlockState(), false);
        scene.world().setBlock(deployer, AllBlocks.DEPLOYER.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.DOWN), false);
        scene.world().setBlock(deployerShaft, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);
        scene.world().setBlock(deployerDrive, AllBlocks.COGWHEEL.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);
        scene.world().setBlock(pressDepot, AllBlocks.DEPOT.get().defaultBlockState(), false);
        scene.world().setBlock(press, AllBlocks.MECHANICAL_PRESS.get().defaultBlockState(), false);
        scene.world().setBlock(pressShaft, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);
        scene.world().setBlock(pressDrive, AllBlocks.COGWHEEL.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);

        Selection deployerAssembly = util.select().position(depot).add(util.select().position(deployer))
                .add(util.select().position(deployerShaft)).add(util.select().position(deployerDrive));
        scene.world().showSection(deployerAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(deployer).add(util.select().position(deployerShaft))
                .add(util.select().position(deployerDrive)), 32);
        scene.effects().rotationDirectionIndicator(deployerDrive);

        ItemStack loose = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        ItemStack paper = new ItemStack(ModItems.ROLLING_PAPER.get());
        ItemStack incomplete = new ItemStack(ModItems.INCOMPLETE_CIGARETTE.get());
        scene.world().modifyBlockEntityNBT(util.select().position(deployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", paper.saveOptional(scene.world().getHolderLookupProvider())));
        scene.world().createItemOnBeltLike(depot, Direction.UP, loose);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(depot))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_assembly.text_1").getString());
        scene.idle(35);
        scene.world().moveDeployer(deployer, 1, 25);
        scene.idle(26);
        scene.world().removeItemsFromBelt(depot);
        scene.world().createItemOnBeltLike(depot, Direction.UP, incomplete);
        scene.world().moveDeployer(deployer, -1, 25);
        scene.effects().indicateSuccess(depot);
        scene.idle(45);

        Selection pressAssembly = util.select().position(pressDepot).add(util.select().position(press))
                .add(util.select().position(pressShaft)).add(util.select().position(pressDrive));
        scene.world().showSection(pressAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(press).add(util.select().position(pressShaft))
                .add(util.select().position(pressDrive)), 32);
        scene.effects().rotationDirectionIndicator(pressDrive);
        scene.world().createItemOnBeltLike(pressDepot, Direction.UP, incomplete);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(press))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_assembly.text_2").getString());
        scene.idle(30);
        scene.world().modifyBlockEntity(press, MechanicalPressBlockEntity.class,
                pte -> pte.getPressingBehaviour().start(Mode.BELT));
        scene.idle(30);
        scene.world().removeItemsFromBelt(pressDepot);
        scene.world().createItemOnBeltLike(pressDepot, Direction.UP, new ItemStack(ModItems.CIGARETTE.get()));
        scene.effects().indicateSuccess(pressDepot);
        scene.idle(25);
        scene.overlay().showControls(util.vector().topOf(pressDepot), Pointing.DOWN, 45)
                .withItem(new ItemStack(ModItems.CIGARETTE.get()));
        scene.overlay().showControls(util.vector().topOf(pressDepot).add(.5, 0, 0), Pointing.DOWN, 45)
                .withItem(new ItemStack(ModItems.CIGAR.get()));
        scene.idle(70);
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
