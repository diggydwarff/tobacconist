package com.diggydwarff.tobacconistmod.compat.create.ponder;

import com.google.common.collect.ImmutableList;
import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
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
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Seven animated Ponder scenes covering Tobacconist's Create production chain. */
public final class TobacconistPonderStoryboards {
    private TobacconistPonderStoryboards() {}

    public static void harvesting(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_harvesting", "Automating Tobacco Harvesting");

        BlockState matureLower = ModBlocks.VIRGINIA_TOBACCO_CROP.get().defaultBlockState()
                .setValue(AbstractTallTobaccoCropBlock.AGE, 3)
                .setValue(AbstractTallTobaccoCropBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState matureUpper = ModBlocks.VIRGINIA_TOBACCO_CROP.get().defaultBlockState()
                .setValue(AbstractTallTobaccoCropBlock.AGE, 7)
                .setValue(AbstractTallTobaccoCropBlock.HALF, DoubleBlockHalf.UPPER);
        BlockState youngLower = ModBlocks.VIRGINIA_TOBACCO_CROP.get().defaultBlockState()
                .setValue(AbstractTallTobaccoCropBlock.AGE, 0)
                .setValue(AbstractTallTobaccoCropBlock.HALF, DoubleBlockHalf.LOWER);

        for (int x = 2; x <= 4; x++) {
            scene.world().setBlock(util.grid().at(x, 0, 2), Blocks.FARMLAND.defaultBlockState(), false);
            scene.world().setBlock(util.grid().at(x, 1, 2), matureLower, false);
            scene.world().setBlock(util.grid().at(x, 2, 2), matureUpper, false);
        }

        Selection cropRow = util.select().fromTo(2, 0, 2, 4, 2, 2);
        scene.world().showSection(cropRow, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(70)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 2, 2))
                .text("Mature tobacco grows two blocks tall and carries its rolled quality into the harvested leaves");
        scene.idle(80);

        BlockState harvesterState = AllBlocks.MECHANICAL_HARVESTER.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        for (int x = 2; x <= 4; x++) {
            scene.world().setBlock(util.grid().at(x, 1, 4), harvesterState, false);
            scene.world().setBlock(util.grid().at(x, 1, 5), AllBlocks.LINEAR_CHASSIS.get().defaultBlockState(), false);
        }

        Selection rigSelection = util.select().fromTo(2, 1, 4, 4, 1, 5);
        ElementLink<WorldSectionElement> rig = scene.world().showIndependentSection(rigSelection, Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(3, 1, 4))
                .text("A Mechanical Harvester on a moving Contraption can harvest mature tobacco automatically");
        scene.idle(45);

        scene.world().moveSection(rig, util.vector().of(0, 0, -2), 55);
        scene.idle(30);
        for (int x = 2; x <= 4; x++) {
            scene.world().setBlock(util.grid().at(x, 1, 2), youngLower, false);
            scene.world().setBlock(util.grid().at(x, 2, 2), Blocks.AIR.defaultBlockState(), false);
            scene.effects().indicateSuccess(util.grid().at(x, 1, 2));
            scene.world().createItemEntity(util.vector().topOf(x, 1, 2), util.vector().of(0, .08, 0),
                    new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get()));
        }
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(3, 1, 2), Pointing.DOWN, 45)
                .withItem(new ItemStack(ModItems.VIRGINIA_TOBACCO_SEEDS.get()));
        scene.overlay().showText(70)
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 1, 2))
                .text("Automation preserves Tobacconist quality metadata and also produces the normal seed drops");
        scene.idle(80);
    }

    public static void curing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_curing", "Automating Tobacco Curing");

        BlockPos rack = util.grid().at(4, 1, 3);
        BlockPos fan = util.grid().at(1, 1, 3);
        BlockPos fanShaft = util.grid().at(0, 1, 3);
        BlockPos cog = util.grid().at(0, 1, 4);
        BlockPos arm = util.grid().at(5, 1, 5);

        scene.world().setBlock(rack, ModBlocks.TOBACCO_DRYING_RACK.get().defaultBlockState(), false);
        scene.world().setBlock(fan, AllBlocks.ENCASED_FAN.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST), false);
        scene.world().setBlock(fanShaft, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        scene.world().setBlock(cog, AllBlocks.COGWHEEL.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        scene.world().setBlock(arm, AllBlocks.MECHANICAL_ARM.get().defaultBlockState(), false);

        reveal(scene, util, rack);
        ItemStack rawLeaves = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class,
                be -> be.setItem(0, rawLeaves.copy()));
        scene.overlay().showControls(util.vector().topOf(rack), Pointing.DOWN, 35).withItem(rawLeaves.copy());
        scene.overlay().showText(70)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(rack))
                .text("Drying Racks accept raw tobacco leaves while preserving variety and quality");
        scene.idle(80);

        Selection fanDrive = util.select().position(fan).add(util.select().position(fanShaft))
                .add(util.select().position(cog));
        scene.world().showSection(fanDrive, Direction.DOWN);
        scene.world().setKineticSpeed(fanDrive, 32);
        scene.effects().rotationDirectionIndicator(cog);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(fan))
                .text("Create Encased Fan airflow reaching the rack accelerates curing");
        scene.idle(85);

        ItemStack curedLeaves = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class,
                be -> be.setItem(0, curedLeaves.copy()));
        scene.effects().indicateSuccess(rack);
        scene.idle(15);
        reveal(scene, util, arm);
        scene.world().setKineticSpeed(util.select().position(arm), 32);
        scene.overlay().showControls(util.vector().topOf(rack), Pointing.DOWN, 45).withItem(curedLeaves.copy());
        scene.overlay().showText(80)
                .placeNearTarget()
                .pointAt(util.vector().centerOf(arm))
                .text("Mechanical Arms can load raw leaves and only take leaves after curing is complete");
        scene.idle(90);
    }

    public static void processing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_processing", "Cutting and Pressing Tobacco");

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
                .text("A Deployer holding a Chaveta cuts cured leaf through Rough, Ribbon and Shag stages");
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
                .text("The Mechanical Press converts Rough-cut tobacco into Flake while preserving tobacco metadata");
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

    public static void blending(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_blending", "Blending and Fermenting Tobacco");

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
                .text("A Mechanical Mixer combines two or three compatible loose tobaccos into metadata-aware blends");
        scene.idle(35);
        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, pte -> pte.startProcessingBasin());
        scene.idle(65);
        scene.world().removeItemsFromBelt(basin);
        visualizeBasinResult(scene, util, basin, blend);
        scene.effects().indicateSuccess(basin);
        scene.overlay().showText(70)
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text("Blend components and secret blend identity are carried forward");
        scene.idle(80);

        Selection barrelLine = util.select().position(barrel).add(util.select().position(packager)).add(util.select().position(arm));
        scene.world().showSection(barrelLine, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(arm), 32);
        scene.rotateCameraY(-15);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(barrel))
                .text("Mechanical Arms and Packagers can feed compatible batches into Tobacco Barrels without bypassing batch rules");
        scene.idle(100);
    }

    public static void flavoring(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = begin(builder, "tobacconist_flavoring", "Aqua Vitae, Essences and Shisha");

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
                .text("With heat, the Mechanical Mixer combines water, sugar and wheat into Aqua Vitae");
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
                .text("Aqua Vitae plus a flavor ingredient produces the corresponding Flavoring Essence");
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
                .text("A Spout can apply a full 1000 mB Essence to loose tobacco for a light aromatic casing");
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
                .text("Flavored Molasses mixed heavily with suitable loose tobacco produces Shisha");
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
        CreateSceneBuilder scene = begin(builder, "tobacconist_assembly", "Automating Cigarettes and Cigars");

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
                .text("Deployers assemble tobacco with the required paper or wrapper into metadata-carrying intermediates");
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
                .text("A Mechanical Press finishes the Cigarette or Cigar without discarding quality, flavor or blend metadata");
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
        CreateSceneBuilder scene = begin(builder, "tobacconist_logistics", "Tobacco Factory Logistics");

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
                .text("Stock Links expose Tobacconist inventories and storage to Create's logistics network");
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
                .text("Packagers can deliver valid inputs directly to Drying Racks, Barrels, Fireboxes and Hookahs");
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
                .text("Mechanical Arms can route inputs and completed products while each Tobacconist machine enforces its own rules");
        scene.idle(105);

        reveal(scene, util, gauge);
        scene.overlay().showControls(util.vector().topOf(gauge), Pointing.DOWN, 40)
                .withItem(new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()));
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(gauge))
                .text("Factory Gauges can restock tobacco by quality tier, so different numeric scores in the same tier can satisfy demand");
        scene.idle(110);

        scene.rotateCameraY(15);
        scene.overlay().showControls(util.vector().topOf(chest), Pointing.DOWN, 45)
                .withItem(new ItemStack(ModItems.TOBACCO_BOX.get()));
        scene.overlay().showText(80)
                .placeNearTarget()
                .pointAt(util.vector().topOf(chest))
                .text("Create packaging preserves Tobacconist components, keeping factory logistics metadata-safe");
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

    private static CreateSceneBuilder begin(SceneBuilder builder, String id, String title) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(id, title);
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
