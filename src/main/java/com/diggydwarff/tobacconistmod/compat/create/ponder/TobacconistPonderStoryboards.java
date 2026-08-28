package com.diggydwarff.tobacconistmod.compat.create.ponder;

import com.google.common.collect.ImmutableList;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.IndustrialDryingRackBlock;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.IndustrialDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.fluid.ModExtractionFluids;
import com.diggydwarff.tobacconistmod.fluid.ModMolassesFluids;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour.Mode;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Animated Ponder scenes covering Tobacconist Create workflows. */
public final class TobacconistPonderStoryboards {
    private TobacconistPonderStoryboards() {}

    public static void curingSun(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_curing_sun", 6, 0.96f, 90);
        BlockPos directRack = util.grid().at(1, 1, 4);
        BlockPos assistedShaft = util.grid().at(3, 1, 1);
        BlockPos assistedFan = util.grid().at(3, 1, 2);
        BlockPos assistedRack = util.grid().at(3, 1, 4);
        Selection assistedSetup = util.select().position(assistedShaft)
                .add(util.select().position(assistedFan))
                .add(util.select().position(assistedRack));
        Selection assistedDrive = util.select().position(assistedShaft)
                .add(util.select().position(assistedFan));

        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_SUN, 75);

        scene.world().showSection(util.select().position(directRack), Direction.DOWN);
        scene.overlay().showControls(util.vector().topOf(directRack), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(directRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_sun.text_1").getString());
        scene.world().modifyBlockEntity(directRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(95);

        scene.world().showSection(assistedSetup, Direction.DOWN);
        scene.world().setKineticSpeed(assistedDrive, 32);
        scene.effects().rotationDirectionIndicator(assistedShaft);
        scene.world().modifyBlockEntity(assistedRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.overlay().showControls(util.vector().topOf(assistedRack), Pointing.DOWN, 35).withItem(raw.copy());
        scene.overlay().showText(110)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(assistedFan))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_sun.text_2").getString());
        scene.idle(115);

        scene.world().modifyBlockEntity(directRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.world().modifyBlockEntity(assistedRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.overlay().showControls(util.vector().topOf(assistedRack), Pointing.DOWN, 45).withItem(cured);
        scene.effects().indicateSuccess(directRack);
        scene.effects().indicateSuccess(assistedRack);
        scene.idle(105);
    }

    public static void curingAir(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_curing_air", 6, 0.94f, 270);
        BlockPos directRack = util.grid().at(2, 1, 3);
        BlockPos assistedRack = util.grid().at(2, 1, 2);
        BlockPos assistedFan = util.grid().at(4, 1, 2);
        BlockPos assistedShaft = util.grid().at(5, 1, 2);

        Selection shelter = util.select().position(util.grid().at(1, 1, 1))
                .add(util.select().position(util.grid().at(3, 1, 1)))
                .add(util.select().position(util.grid().at(1, 2, 1)))
                .add(util.select().position(util.grid().at(3, 2, 1)))
                .add(util.select().position(util.grid().at(1, 3, 1)))
                .add(util.select().position(util.grid().at(3, 3, 1)))
                .add(util.select().position(util.grid().at(1, 4, 1)))
                .add(util.select().position(util.grid().at(2, 4, 1)))
                .add(util.select().position(util.grid().at(3, 4, 1)))
                .add(util.select().position(util.grid().at(1, 4, 2)))
                .add(util.select().position(util.grid().at(2, 4, 2)))
                .add(util.select().position(util.grid().at(3, 4, 2)))
                .add(util.select().position(util.grid().at(2, 4, 3)))
                .add(util.select().position(util.grid().at(3, 4, 3)))
                .add(util.select().position(util.grid().at(3, 4, 4)));

        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_AIR, 75);

        scene.world().showSection(shelter, Direction.DOWN);
        scene.world().showSection(util.select().position(directRack), Direction.DOWN);
        scene.overlay().showControls(util.vector().topOf(directRack), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(directRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_air.text_1").getString());
        scene.world().modifyBlockEntity(directRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(95);

        scene.world().setBlock(assistedFan, AllBlocks.ENCASED_FAN.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.WEST), false);
        scene.world().setBlock(assistedShaft, AllBlocks.SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), false);
        Selection assistedSetup = util.select().position(assistedRack)
                .add(util.select().position(assistedFan))
                .add(util.select().position(assistedShaft));
        Selection assistedDrive = util.select().position(assistedFan)
                .add(util.select().position(assistedShaft));
        scene.world().showSection(assistedSetup, Direction.EAST);
        scene.world().setKineticSpeed(assistedDrive, 32);
        scene.effects().rotationDirectionIndicator(assistedShaft);
        scene.world().modifyBlockEntity(assistedRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.overlay().showControls(util.vector().topOf(assistedRack), Pointing.DOWN, 35).withItem(raw.copy());
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(assistedFan))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_air.text_2").getString());
        scene.idle(110);

        scene.world().modifyBlockEntity(directRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.world().modifyBlockEntity(assistedRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.overlay().showControls(util.vector().topOf(assistedRack), Pointing.DOWN, 45).withItem(cured);
        scene.overlay().showText(100)
                .placeNearTarget()
                .pointAt(util.vector().topOf(assistedRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_air.text_3").getString());
        scene.effects().indicateSuccess(directRack);
        scene.effects().indicateSuccess(assistedRack);
        scene.idle(110);
    }

    public static void curingFire(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_curing_fire", 9, 0.98f, 270);
        BlockPos directCampfire = util.grid().at(1, 1, 3);
        BlockPos directRack = util.grid().at(1, 2, 3);
        BlockPos assistedShaft = util.grid().at(7, 1, 1);
        BlockPos assistedFan = util.grid().at(7, 1, 2);
        BlockPos assistedCampfire = util.grid().at(7, 1, 3);
        BlockPos assistedRack = util.grid().at(7, 1, 4);
        Selection directSetup = util.select().position(directCampfire).add(util.select().position(directRack));
        Selection assistedSetup = util.select().position(assistedShaft)
                .add(util.select().position(assistedFan))
                .add(util.select().position(assistedCampfire))
                .add(util.select().position(assistedRack));
        Selection assistedDrive = util.select().position(assistedShaft)
                .add(util.select().position(assistedFan));

        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_FIRE, 75);

        scene.world().showSection(directSetup, Direction.DOWN);
        scene.overlay().showControls(util.vector().topOf(directRack), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(directCampfire))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_fire.text_1").getString());
        scene.world().modifyBlockEntity(directRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(95);

        scene.world().showSection(assistedSetup, Direction.DOWN);
        scene.world().setKineticSpeed(assistedDrive, 32);
        scene.effects().rotationDirectionIndicator(assistedShaft);
        scene.effects().indicateSuccess(assistedCampfire);
        scene.overlay().showControls(util.vector().topOf(assistedRack), Pointing.DOWN, 35).withItem(raw.copy());
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(assistedCampfire))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_fire.text_2").getString());
        scene.world().modifyBlockEntity(assistedRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(110);

        scene.world().modifyBlockEntity(directRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.world().modifyBlockEntity(assistedRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.overlay().showControls(util.vector().topOf(assistedRack), Pointing.DOWN, 45).withItem(cured);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(assistedRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_fire.text_3").getString());
        scene.effects().indicateSuccess(directRack);
        scene.effects().indicateSuccess(assistedRack);
        scene.idle(115);
    }

    public static void curingFlue(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_curing_flue", 9, 0.90f, 270);
        BlockPos directRack = util.grid().at(2, 1, 3);
        BlockPos firebox = util.grid().at(4, 1, 3);
        BlockPos assistedShaft = util.grid().at(7, 1, 1);
        BlockPos assistedFan = util.grid().at(7, 1, 2);
        BlockPos assistedLava = util.grid().at(7, 1, 3);
        BlockPos assistedRack = util.grid().at(7, 1, 5);
        Selection flueHouse = util.select().fromTo(1, 1, 1, 5, 4, 5);
        Selection assistedSetup = util.select().position(assistedShaft)
                .add(util.select().position(assistedFan))
                .add(util.select().position(assistedLava))
                .add(util.select().position(assistedRack));
        Selection assistedDrive = util.select().position(assistedShaft)
                .add(util.select().position(assistedFan));

        scene.world().showSection(flueHouse, Direction.DOWN);
        scene.world().setBlock(firebox, ModBlocks.FLUE_FIREBOX.get().defaultBlockState()
                .setValue(BlockStateProperties.LIT, true)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), false);

        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_FLUE, 75);

        scene.overlay().showControls(util.vector().topOf(directRack), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(firebox))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_flue.text_1").getString());
        scene.world().modifyBlockEntity(directRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(105);

        scene.world().showSection(assistedSetup, Direction.DOWN);
        scene.world().setKineticSpeed(assistedDrive, 32);
        scene.effects().rotationDirectionIndicator(assistedShaft);
        scene.effects().indicateSuccess(assistedLava);
        scene.world().modifyBlockEntity(assistedRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.overlay().showControls(util.vector().topOf(assistedRack), Pointing.DOWN, 35).withItem(raw.copy());
        scene.overlay().showText(115)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(assistedLava))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_curing_flue.text_2").getString());
        scene.idle(120);

        scene.world().modifyBlockEntity(directRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.world().modifyBlockEntity(assistedRack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.overlay().showControls(util.vector().topOf(assistedRack), Pointing.DOWN, 45).withItem(cured);
        scene.effects().indicateSuccess(directRack);
        scene.effects().indicateSuccess(assistedRack);
        scene.idle(110);
    }

    public static void woodenAutomationHoppers(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_wooden_automation_hoppers", 5, 1.0f, 90);
        BlockPos inputBarrel = util.grid().at(1, 3, 1);
        BlockPos inputHopper = util.grid().at(1, 2, 1);
        BlockPos rack = util.grid().at(2, 2, 1);
        BlockPos outputHopper = util.grid().at(2, 1, 1);
        BlockPos outputBarrel = util.grid().at(3, 1, 1);
        BlockPos displayLink = util.grid().at(3, 2, 1);
        BlockPos leftNixie = util.grid().at(2, 4, 1);
        BlockPos rightNixie = util.grid().at(3, 4, 1);
        Selection inputSide = util.select().position(inputBarrel)
                .add(util.select().position(inputHopper))
                .add(util.select().position(rack));
        Selection monitoring = util.select().position(displayLink)
                .add(util.select().position(leftNixie))
                .add(util.select().position(rightNixie))
                .add(util.select().position(util.grid().at(2, 5, 1)))
                .add(util.select().position(util.grid().at(3, 5, 1)));
        Selection outputSide = util.select().position(outputHopper).add(util.select().position(outputBarrel));

        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_AIR, 75);

        scene.world().showSection(inputSide, Direction.DOWN);
        scene.overlay().showControls(util.vector().topOf(inputBarrel), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(inputHopper))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_wooden_automation_hoppers.text_1").getString());
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(100);

        scene.world().showSection(monitoring, Direction.DOWN);
        scene.effects().indicateSuccess(displayLink);
        scene.effects().indicateSuccess(leftNixie);
        scene.effects().indicateSuccess(rightNixie);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(displayLink))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_wooden_automation_hoppers.text_2").getString());
        scene.idle(110);

        scene.world().showSection(outputSide, Direction.DOWN);
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.overlay().showControls(util.vector().centerOf(outputHopper), Pointing.DOWN, 40).withItem(cured);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(outputHopper))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_wooden_automation_hoppers.text_3").getString());
        scene.effects().indicateSuccess(rack);
        scene.idle(115);
    }

    public static void woodenAutomationFunnels(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_wooden_automation_funnels", 7, 0.98f, 90);
        BlockPos rack = util.grid().at(2, 2, 1);
        BlockPos inputHopper = util.grid().at(2, 3, 1);
        BlockPos inputBarrel = util.grid().at(2, 4, 1);
        BlockPos inputFunnel = util.grid().at(2, 2, 2);
        BlockPos outputFunnel = util.grid().at(2, 2, 4);
        BlockPos beltOutput = util.grid().at(2, 1, 4);
        BlockPos outputBarrel = util.grid().at(2, 2, 5);
        Selection rackSide = util.select().position(rack)
                .add(util.select().position(inputHopper))
                .add(util.select().position(inputBarrel));
        Selection beltLine = util.select().position(util.grid().at(1, 1, 2))
                .add(util.select().position(util.grid().at(2, 1, 2)))
                .add(util.select().position(util.grid().at(2, 1, 3)))
                .add(util.select().position(util.grid().at(2, 1, 4)))
                .add(util.select().position(util.grid().at(2, 1, 5)))
                .add(util.select().position(inputFunnel))
                .add(util.select().position(outputFunnel))
                .add(util.select().position(outputBarrel));
        Selection beltDrive = util.select().position(util.grid().at(1, 1, 2))
                .add(util.select().position(util.grid().at(2, 1, 2)))
                .add(util.select().position(util.grid().at(2, 1, 3)))
                .add(util.select().position(util.grid().at(2, 1, 4)));

        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_AIR, 75);

        scene.world().showSection(rackSide, Direction.DOWN);
        scene.overlay().showControls(util.vector().topOf(inputBarrel), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(rack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_wooden_automation_funnels.text_1").getString());
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(110);

        scene.world().showSection(beltLine, Direction.DOWN);
        scene.world().setKineticSpeed(beltDrive, 32);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(outputFunnel))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_wooden_automation_funnels.text_2").getString());
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.world().createItemOnBeltLike(beltOutput, Direction.UP, cured);
        scene.effects().indicateSuccess(outputFunnel);
        scene.idle(115);

        scene.world().removeItemsFromBelt(beltOutput);
        scene.overlay().showText(95)
                .placeNearTarget()
                .pointAt(util.vector().centerOf(rack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_wooden_automation_funnels.text_3").getString());
        scene.idle(105);
    }

    public static void woodenAutomationArm(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_wooden_automation_arm", 6, 1.0f, 90);
        BlockPos vault = util.grid().at(1, 2, 1);
        BlockPos funnel = util.grid().at(1, 2, 2);
        BlockPos depot = util.grid().at(1, 1, 2);
        BlockPos arm = util.grid().at(3, 1, 3);
        BlockPos rack = util.grid().at(1, 1, 4);
        Selection storage = util.select().position(util.grid().at(1, 1, 1))
                .add(util.select().position(vault))
                .add(util.select().position(funnel))
                .add(util.select().position(depot));
        Selection armAndRack = util.select().position(arm).add(util.select().position(rack));

        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 8);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 8);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_AIR, 75);

        scene.world().showSection(storage, Direction.DOWN);
        scene.world().showSection(armAndRack, Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(arm), 32);
        scene.overlay().showControls(util.vector().topOf(vault), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(arm))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_wooden_automation_arm.text_1").getString());
        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(115);

        scene.world().modifyBlockEntity(rack, TobaccoDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.effects().indicateSuccess(rack);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(rack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_wooden_automation_arm.text_2").getString());
        scene.idle(60);

        scene.world().createItemOnBeltLike(depot, Direction.UP, cured);
        scene.overlay().showText(95)
                .placeNearTarget()
                .pointAt(util.vector().centerOf(depot))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_wooden_automation_arm.text_3").getString());
        scene.idle(105);
    }

    public static void industrialAirCuring(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_industrial_curing_air", 10, 0.90f, 270);
        BlockPos lowerRack = util.grid().at(1, 1, 4);
        BlockPos upperRack = util.grid().at(1, 2, 4);
        BlockPos lowerFan = util.grid().at(1, 1, 2);
        BlockPos upperFan = util.grid().at(1, 2, 2);
        BlockPos lowerShaft = util.grid().at(1, 1, 1);
        BlockPos upperShaft = util.grid().at(1, 2, 1);
        BlockPos lowerChute = util.grid().at(1, 3, 4);
        BlockPos upperChute = util.grid().at(1, 4, 4);
        BlockPos beltA = util.grid().at(1, 1, 6);
        BlockPos beltB = util.grid().at(1, 1, 7);
        BlockPos beltC = util.grid().at(1, 1, 8);
        BlockPos outputFunnel = util.grid().at(1, 2, 5);

        Selection rackAssembly = util.select().position(lowerRack)
                .add(util.select().position(upperRack))
                .add(util.select().position(lowerChute))
                .add(util.select().position(upperChute));
        Selection airflow = util.select().position(lowerFan)
                .add(util.select().position(upperFan))
                .add(util.select().position(lowerShaft))
                .add(util.select().position(upperShaft));
        Selection beltLine = util.select().fromTo(1, 1, 5, 3, 2, 9);

        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 32);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 32);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_AIR, 75);

        scene.world().showSection(rackAssembly, Direction.DOWN);
        scene.overlay().showControls(util.vector().topOf(upperChute), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(lowerRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_air.text_1").getString());
        scene.world().modifyBlockEntity(lowerRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(105);

        scene.world().showSection(airflow, Direction.SOUTH);
        scene.world().setKineticSpeed(airflow, 32);
        scene.effects().rotationDirectionIndicator(lowerShaft);
        scene.effects().rotationDirectionIndicator(upperShaft);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(upperFan))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_air.text_2").getString());
        scene.idle(115);

        scene.world().showSection(beltLine, Direction.DOWN);
        scene.world().setKineticSpeed(beltLine, 32);
        scene.world().modifyBlockEntity(lowerRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        createDirectionalBeltBatch(scene, Direction.SOUTH, cured.copyWithCount(8), beltA, beltB, beltC);
        scene.effects().indicateSuccess(outputFunnel);
        scene.effects().indicateSuccess(lowerRack);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(outputFunnel))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_air.text_3").getString());
        scene.idle(120);
    }

    public static void industrialFireCuring(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_industrial_curing_fire", 10, 0.90f, 270);
        BlockPos lowerRack = util.grid().at(1, 1, 4);
        BlockPos upperRack = util.grid().at(1, 2, 4);
        BlockPos lowerFan = util.grid().at(1, 1, 2);
        BlockPos upperFan = util.grid().at(1, 2, 2);
        BlockPos lowerShaft = util.grid().at(1, 1, 1);
        BlockPos upperShaft = util.grid().at(1, 2, 1);
        BlockPos lowerCampfire = util.grid().at(1, 1, 3);
        BlockPos upperCampfire = util.grid().at(1, 2, 3);
        BlockPos lowerChute = util.grid().at(1, 3, 4);
        BlockPos upperChute = util.grid().at(1, 4, 4);
        BlockPos beltA = util.grid().at(1, 1, 6);
        BlockPos beltB = util.grid().at(1, 1, 7);
        BlockPos beltC = util.grid().at(1, 1, 8);
        BlockPos outputFunnel = util.grid().at(1, 2, 5);

        Selection rackAssembly = util.select().position(lowerRack)
                .add(util.select().position(upperRack))
                .add(util.select().position(lowerChute))
                .add(util.select().position(upperChute));
        Selection heat = util.select().position(lowerCampfire).add(util.select().position(upperCampfire));
        Selection airflow = util.select().position(lowerFan)
                .add(util.select().position(upperFan))
                .add(util.select().position(lowerShaft))
                .add(util.select().position(upperShaft));
        Selection beltLine = util.select().fromTo(1, 1, 5, 4, 2, 9);

        ItemStack raw = new ItemStack(ModItems.BURLEY_TOBACCO_LEAF.get(), 32);
        ItemStack cured = new ItemStack(ModItems.BURLEY_TOBACCO_LEAF_DRY.get(), 32);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_FIRE, 75);

        scene.world().showSection(rackAssembly, Direction.DOWN);
        scene.overlay().showControls(util.vector().topOf(upperChute), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(lowerRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_fire.text_1").getString());
        scene.world().modifyBlockEntity(lowerRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(100);

        scene.world().showSection(heat, Direction.SOUTH);
        scene.world().showSection(airflow, Direction.SOUTH);
        scene.world().setKineticSpeed(airflow, 32);
        scene.effects().rotationDirectionIndicator(lowerShaft);
        scene.effects().rotationDirectionIndicator(upperShaft);
        scene.overlay().showText(110)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(upperCampfire))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_fire.text_2").getString());
        scene.idle(120);

        scene.world().showSection(beltLine, Direction.DOWN);
        scene.world().setKineticSpeed(beltLine, 32);
        scene.world().modifyBlockEntity(lowerRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        createDirectionalBeltBatch(scene, Direction.SOUTH, cured.copyWithCount(8), beltA, beltB, beltC);
        scene.effects().indicateSuccess(outputFunnel);
        scene.effects().indicateSuccess(lowerRack);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(outputFunnel))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_fire.text_3").getString());
        scene.idle(120);
    }

    public static void industrialFlueCuring(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_industrial_curing_flue", 10, 0.90f, 270);
        BlockPos lowerRack = util.grid().at(1, 1, 4);
        BlockPos upperRack = util.grid().at(1, 2, 4);
        BlockPos lowerFan = util.grid().at(1, 1, 2);
        BlockPos upperFan = util.grid().at(1, 2, 2);
        BlockPos lowerShaft = util.grid().at(1, 1, 1);
        BlockPos upperShaft = util.grid().at(1, 2, 1);
        BlockPos lowerLava = util.grid().at(1, 1, 3);
        BlockPos upperLava = util.grid().at(1, 2, 3);
        BlockPos lowerChute = util.grid().at(1, 3, 4);
        BlockPos upperChute = util.grid().at(1, 4, 4);
        BlockPos beltA = util.grid().at(1, 1, 6);
        BlockPos beltB = util.grid().at(1, 1, 7);
        BlockPos beltC = util.grid().at(1, 1, 8);
        BlockPos outputFunnel = util.grid().at(1, 2, 5);

        Selection rackAssembly = util.select().position(lowerRack)
                .add(util.select().position(upperRack))
                .add(util.select().position(lowerChute))
                .add(util.select().position(upperChute));
        Selection heat = util.select().position(lowerLava).add(util.select().position(upperLava));
        Selection airflow = util.select().position(lowerFan)
                .add(util.select().position(upperFan))
                .add(util.select().position(lowerShaft))
                .add(util.select().position(upperShaft));
        Selection beltLine = util.select().fromTo(1, 1, 5, 1, 2, 9);

        ItemStack raw = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF.get(), 32);
        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 32);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_FLUE, 75);

        scene.world().showSection(rackAssembly, Direction.DOWN);
        scene.overlay().showControls(util.vector().topOf(upperChute), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(lowerRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_flue.text_1").getString());
        scene.world().modifyBlockEntity(lowerRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.idle(100);

        scene.world().showSection(heat, Direction.SOUTH);
        scene.world().showSection(airflow, Direction.SOUTH);
        scene.world().setKineticSpeed(airflow, 32);
        scene.effects().rotationDirectionIndicator(lowerShaft);
        scene.effects().rotationDirectionIndicator(upperShaft);
        scene.overlay().showText(110)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(upperLava))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_flue.text_2").getString());
        scene.idle(120);

        scene.world().showSection(beltLine, Direction.DOWN);
        scene.world().setKineticSpeed(beltLine, 32);
        scene.world().modifyBlockEntity(lowerRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        createDirectionalBeltBatch(scene, Direction.SOUTH, cured.copyWithCount(8), beltA, beltB, beltC);
        scene.effects().indicateSuccess(outputFunnel);
        scene.effects().indicateSuccess(lowerRack);
        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(outputFunnel))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_flue.text_3").getString());
        scene.idle(120);
    }

    public static void industrialRackArray(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_industrial_curing_array", 7, 0.98f, 270);
        BlockPos firstRack = util.grid().at(3, 1, 3);
        BlockPos secondRack = util.grid().at(4, 1, 3);
        BlockPos thirdRack = util.grid().at(5, 1, 3);
        BlockPos firstFan = util.grid().at(3, 2, 1);
        BlockPos firstCampfire = util.grid().at(3, 2, 2);
        BlockPos beltA = util.grid().at(2, 1, 4);
        BlockPos beltB = util.grid().at(3, 1, 4);
        BlockPos beltC = util.grid().at(4, 1, 4);

        Selection firstLane = util.select().fromTo(3, 1, 1, 3, 4, 4);
        Selection secondLane = util.select().fromTo(4, 1, 1, 4, 4, 4);
        Selection thirdLane = util.select().fromTo(5, 1, 1, 5, 4, 4);
        Selection beltDrive = util.select().fromTo(1, 1, 2, 5, 1, 4);
        Selection allFans = util.select().fromTo(3, 1, 1, 5, 2, 1);

        ItemStack raw = new ItemStack(ModItems.BURLEY_TOBACCO_LEAF.get(), 32);
        ItemStack cured = new ItemStack(ModItems.BURLEY_TOBACCO_LEAF_DRY.get(), 32);
        TobaccoCuringHelper.applyCureData(cured, TobaccoCuringHelper.CURE_FIRE, 75);

        scene.world().showSection(firstLane, Direction.DOWN);
        scene.world().setKineticSpeed(allFans, 32);
        scene.overlay().showControls(util.vector().topOf(firstRack.above(3)), Pointing.DOWN, 35).withItem(raw);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(firstRack))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_array.text_1").getString());
        scene.world().modifyBlockEntity(firstRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.effects().rotationDirectionIndicator(firstFan);
        scene.idle(110);

        scene.world().showSection(secondLane, Direction.DOWN);
        scene.world().showSection(thirdLane, Direction.DOWN);
        scene.world().modifyBlockEntity(secondRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.world().modifyBlockEntity(thirdRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, raw.copy()));
        scene.overlay().showText(110)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(firstCampfire))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_array.text_2").getString());
        scene.idle(120);

        scene.world().showSection(beltDrive, Direction.DOWN);
        scene.world().setKineticSpeed(beltDrive, 32);
        scene.world().modifyBlockEntity(firstRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.world().modifyBlockEntity(secondRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        scene.world().modifyBlockEntity(thirdRack, IndustrialDryingRackBlockEntity.class, be -> be.setItem(0, cured.copy()));
        createDirectionalBeltBatch(scene, Direction.SOUTH, cured.copyWithCount(8), beltA, beltB, beltC);
        scene.effects().indicateSuccess(firstRack);
        scene.effects().indicateSuccess(secondRack);
        scene.effects().indicateSuccess(thirdRack);
        scene.overlay().showText(110)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(beltB))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_industrial_curing_array.text_3").getString());
        scene.idle(125);
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
        scene.world().createItemOnBeltLike(depotA, Direction.UP, rough);
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
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_homogenizing", 8, 0.96f, 270);

        BlockPos basin = util.grid().at(3, 2, 2);
        BlockPos mixer = util.grid().at(3, 4, 2);
        BlockPos mixerCog = util.grid().at(3, 4, 1);
        BlockPos mixerShaft = util.grid().at(3, 5, 1);
        BlockPos leftInput = util.grid().at(1, 5, 2);
        BlockPos rightInput = util.grid().at(5, 5, 2);
        BlockPos lever = util.grid().at(3, 4, 3);
        BlockPos beltStart = util.grid().at(3, 1, 3);
        BlockPos beltEnd = util.grid().at(3, 1, 6);
        BlockPos beltCog = util.grid().at(1, 1, 3);
        BlockPos beltShaft = util.grid().at(2, 1, 3);

        Selection mixerAssembly = util.select().fromTo(1, 2, 1, 5, 5, 3);
        Selection beltLine = util.select().fromTo(1, 1, 3, 3, 1, 6);
        Selection mixerKinetics = util.select().position(mixer)
                .add(util.select().position(mixerCog))
                .add(util.select().position(mixerShaft));

        ItemStack low = rawLeafWithQuality(49, 32);
        ItemStack high = rawLeafWithQuality(59, 32);
        ItemStack standardized = rawLeafWithQuality(54, 64);

        scene.world().showSection(mixerAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(mixerKinetics, 32);
        scene.effects().rotationDirectionIndicator(mixerShaft);
        scene.overlay().showControls(util.vector().topOf(leftInput), Pointing.DOWN, 45).withItem(low);
        scene.overlay().showControls(util.vector().topOf(rightInput), Pointing.DOWN, 45).withItem(high);
        scene.world().createItemOnBeltLike(basin, Direction.UP, low);
        scene.world().createItemOnBeltLike(basin, Direction.UP, high);
        scene.overlay().showText(85)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_homogenizing.text_1").getString());
        scene.idle(95);

        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(mixer))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_homogenizing.text_2").getString());
        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, MechanicalMixerBlockEntity::startProcessingBasin);
        scene.idle(70);
        scene.world().removeItemsFromBelt(basin);
        visualizeBasinResult(scene, util, basin, standardized);
        scene.effects().indicateSuccess(basin);
        scene.idle(30);

        scene.overlay().showText(105)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(lever))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_homogenizing.text_3").getString());
        scene.idle(115);

        clearBasinResult(scene, util, basin);
        scene.world().showSection(beltLine, Direction.DOWN);
        scene.world().setKineticSpeed(beltLine, 32);
        scene.effects().rotationDirectionIndicator(beltCog);
        scene.effects().rotationDirectionIndicator(beltShaft);
        scene.world().createItemOnBelt(beltStart, Direction.UP, standardized.copy());
        showMovingItem(scene, util, beltStart, standardized, 0, 0, 0.055, 45);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(beltEnd))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_homogenizing.text_4").getString());
        scene.effects().indicateSuccess(beltEnd);
        scene.idle(100);
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

    public static void plainMolasses(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_plain_molasses", 8, 0.96f, 270);

        BlockPos burner = util.grid().at(2, 1, 5);
        BlockPos basin = util.grid().at(2, 2, 5);
        BlockPos mixer = util.grid().at(2, 4, 5);
        BlockPos mixerCog = util.grid().at(2, 4, 4);
        BlockPos mixerShaft = util.grid().at(2, 5, 4);
        BlockPos ingredientBarrel = util.grid().at(1, 3, 5);
        BlockPos waterPump = util.grid().at(3, 2, 3);
        BlockPos outputPump = util.grid().at(2, 2, 6);
        BlockPos outputTank = util.grid().at(4, 2, 7);

        BlockPos[] waterTanks = {util.grid().at(3, 2, 2), util.grid().at(3, 3, 2), util.grid().at(3, 4, 2)};
        BlockPos[] outputTanks = {outputTank, util.grid().at(4, 3, 7)};

        Selection waterLine = util.select().fromTo(3, 2, 2, 4, 4, 4);
        Selection ingredientLine = util.select().fromTo(1, 2, 5, 1, 3, 5);
        Selection mixerAssembly = util.select().position(burner).add(util.select().position(basin))
                .add(util.select().position(mixer)).add(util.select().position(mixerCog)).add(util.select().position(mixerShaft));
        Selection outputLine = util.select().fromTo(2, 2, 6, 4, 3, 7);

        scene.world().setBlock(burner, AllBlocks.BLAZE_BURNER.get().defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), false);
        fillTankColumn(scene, new FluidStack(Fluids.WATER, 8000), waterTanks);
        scene.world().showSection(waterLine, Direction.DOWN);
        scene.world().showSection(ingredientLine, Direction.DOWN);
        scene.world().showSection(mixerAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(waterLine, 32);
        scene.world().setKineticSpeed(util.select().position(mixer).add(util.select().position(mixerCog)).add(util.select().position(mixerShaft)), 32);
        scene.world().propagatePipeChange(waterPump);
        scene.overlay().showControls(util.vector().topOf(ingredientBarrel), Pointing.DOWN, 45).withItem(new ItemStack(Items.SUGAR_CANE));
        scene.overlay().showControls(util.vector().topOf(waterTanks[1]), Pointing.RIGHT, 45).withItem(new ItemStack(Items.WATER_BUCKET));
        scene.world().createItemOnBeltLike(basin, Direction.UP, new ItemStack(Items.SUGAR_CANE));
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_plain_molasses.text_1").getString());
        scene.idle(100);

        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, MechanicalMixerBlockEntity::startProcessingBasin);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(burner))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_plain_molasses.text_2").getString());
        scene.idle(75);
        scene.world().removeItemsFromBelt(basin);

        scene.world().showSection(outputLine, Direction.DOWN);
        scene.world().setKineticSpeed(outputLine, 32);
        scene.world().propagatePipeChange(outputPump);
        fillTankColumn(scene, new FluidStack(ModMolassesFluids.source(BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN), 6000), outputTanks);
        scene.effects().indicateSuccess(outputTank);
        scene.overlay().showControls(util.vector().topOf(outputTank), Pointing.DOWN, 45).withItem(new ItemStack(ModItems.BOTTLED_MOLASSES_PLAIN.get()));
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(outputTank))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_plain_molasses.text_3").getString());
        scene.idle(105);
    }

    public static void aquaVitae(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_aqua_vitae", 8, 0.96f, 270);

        BlockPos burner = util.grid().at(3, 1, 4);
        BlockPos basin = util.grid().at(3, 2, 4);
        BlockPos mixer = util.grid().at(3, 4, 4);
        BlockPos mixerCog = util.grid().at(3, 4, 3);
        BlockPos mixerShaft = util.grid().at(3, 5, 3);
        BlockPos sugarBarrel = util.grid().at(1, 3, 4);
        BlockPos wheatBarrel = util.grid().at(2, 3, 3);
        BlockPos waterPump = util.grid().at(4, 2, 2);
        BlockPos outputPump = util.grid().at(3, 2, 5);
        BlockPos outputTank = util.grid().at(5, 2, 6);

        BlockPos[] waterTanks = {util.grid().at(4, 2, 1), util.grid().at(4, 3, 1), util.grid().at(4, 4, 1)};
        BlockPos[] outputTanks = {outputTank, util.grid().at(5, 3, 6)};

        Selection ingredientLine = util.select().fromTo(1, 2, 3, 2, 3, 4);
        Selection waterLine = util.select().fromTo(4, 2, 1, 4, 4, 4);
        Selection mixerAssembly = util.select().position(burner).add(util.select().position(basin))
                .add(util.select().position(mixer)).add(util.select().position(mixerCog)).add(util.select().position(mixerShaft));
        Selection outputLine = util.select().fromTo(3, 2, 5, 5, 3, 6);

        scene.world().setBlock(burner, AllBlocks.BLAZE_BURNER.get().defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), false);
        fillTankColumn(scene, new FluidStack(Fluids.WATER, 8000), waterTanks);
        scene.world().showSection(waterLine, Direction.DOWN);
        scene.world().showSection(ingredientLine, Direction.DOWN);
        scene.world().showSection(mixerAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(waterLine, 32);
        scene.world().setKineticSpeed(util.select().position(mixer).add(util.select().position(mixerCog)).add(util.select().position(mixerShaft)), 32);
        scene.world().propagatePipeChange(waterPump);
        scene.overlay().showControls(util.vector().topOf(sugarBarrel), Pointing.DOWN, 45).withItem(new ItemStack(Items.SUGAR));
        scene.overlay().showControls(util.vector().topOf(wheatBarrel), Pointing.DOWN, 45).withItem(new ItemStack(Items.WHEAT));
        scene.world().createItemOnBeltLike(basin, Direction.UP, new ItemStack(Items.SUGAR));
        scene.world().createItemOnBeltLike(basin, Direction.UP, new ItemStack(Items.WHEAT));
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_aqua_vitae.text_1").getString());
        scene.idle(100);

        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, MechanicalMixerBlockEntity::startProcessingBasin);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(burner))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_aqua_vitae.text_2").getString());
        scene.idle(75);
        scene.world().removeItemsFromBelt(basin);

        scene.world().showSection(outputLine, Direction.DOWN);
        scene.world().setKineticSpeed(outputLine, 32);
        scene.world().propagatePipeChange(outputPump);
        fillTankColumn(scene, new FluidStack(ModExtractionFluids.aquaVitae(), 6000), outputTanks);
        scene.effects().indicateSuccess(outputTank);
        scene.overlay().showControls(util.vector().topOf(outputTank), Pointing.DOWN, 45).withItem(new ItemStack(ModItems.BOTTLED_AQUA_VITAE.get()));
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(outputTank))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_aqua_vitae.text_3").getString());
        scene.idle(105);
    }

    public static void flavoredMolasses(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_flavored_molasses", 8, 0.96f, 270);

        BottledMolassesFlavors flavor = BottledMolassesFlavors.BOTTLED_MOLASSES_CHORUS_FRUIT_FLAVOR;
        BlockPos basin = util.grid().at(2, 2, 5);
        BlockPos mixer = util.grid().at(2, 4, 5);
        BlockPos mixerCog = util.grid().at(2, 4, 4);
        BlockPos mixerShaft = util.grid().at(2, 5, 4);
        BlockPos molassesPump = util.grid().at(1, 2, 3);
        BlockPos essencePump = util.grid().at(3, 2, 3);
        BlockPos outputPump = util.grid().at(2, 2, 6);
        BlockPos outputTank = util.grid().at(4, 2, 7);

        BlockPos[] molassesTanks = {util.grid().at(1, 2, 2), util.grid().at(1, 3, 2), util.grid().at(1, 4, 2)};
        BlockPos[] essenceTanks = {util.grid().at(3, 2, 2), util.grid().at(3, 3, 2), util.grid().at(3, 4, 2)};
        BlockPos[] outputTanks = {outputTank, util.grid().at(4, 3, 7)};

        Selection sourceLines = util.select().fromTo(1, 2, 2, 3, 4, 5);
        Selection mixerAssembly = util.select().position(basin).add(util.select().position(mixer))
                .add(util.select().position(mixerCog)).add(util.select().position(mixerShaft));
        Selection outputLine = util.select().fromTo(2, 2, 6, 4, 3, 7);

        fillTankColumn(scene, new FluidStack(ModMolassesFluids.source(BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN), 8000), molassesTanks);
        fillTankColumn(scene, new FluidStack(ModExtractionFluids.essence(flavor), 8000), essenceTanks);
        scene.world().showSection(sourceLines, Direction.DOWN);
        scene.world().showSection(mixerAssembly, Direction.DOWN);
        scene.world().setKineticSpeed(sourceLines, 32);
        scene.world().setKineticSpeed(util.select().position(mixer).add(util.select().position(mixerCog)).add(util.select().position(mixerShaft)), 32);
        scene.world().propagatePipeChange(molassesPump);
        scene.world().propagatePipeChange(essencePump);
        scene.overlay().showControls(util.vector().topOf(molassesTanks[1]), Pointing.LEFT, 45).withItem(new ItemStack(ModItems.BOTTLED_MOLASSES_PLAIN.get()));
        scene.overlay().showControls(util.vector().topOf(essenceTanks[1]), Pointing.RIGHT, 45).withItem(flavor.getEssenceStack());
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(basin))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_flavored_molasses.text_1").getString());
        scene.idle(100);

        scene.world().modifyBlockEntity(mixer, MechanicalMixerBlockEntity.class, MechanicalMixerBlockEntity::startProcessingBasin);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(mixer))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_flavored_molasses.text_2").getString());
        scene.idle(75);

        scene.world().showSection(outputLine, Direction.DOWN);
        scene.world().setKineticSpeed(outputLine, 32);
        scene.world().propagatePipeChange(outputPump);
        fillTankColumn(scene, new FluidStack(ModMolassesFluids.source(flavor), 6000), outputTanks);
        scene.effects().indicateSuccess(outputTank);
        scene.overlay().showControls(util.vector().topOf(outputTank), Pointing.DOWN, 45).withItem(flavor.getStack());
        scene.overlay().showText(95)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(outputTank))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_flavored_molasses.text_3").getString());
        scene.idle(110);
    }

    public static void cigaretteProduction(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_cigarette_production", 10, 0.90f, 90);

        BlockPos barrel = util.grid().at(1, 2, 1);
        BlockPos funnel = util.grid().at(2, 2, 1);
        BlockPos paperDeployer = util.grid().at(5, 3, 1);
        BlockPos press = util.grid().at(4, 3, 1);
        BlockPos cutOneDeployer = util.grid().at(6, 3, 5);
        BlockPos cutTwoDeployer = util.grid().at(6, 3, 4);
        BlockPos cutThreeDeployer = util.grid().at(6, 3, 3);
        BlockPos upperChute = util.grid().at(6, 4, 7);
        BlockPos driveShaft = util.grid().at(7, 1, 2);
        BlockPos driveCog = util.grid().at(8, 1, 2);

        BlockPos inputBelt = util.grid().at(6, 1, 7);
        BlockPos cutOneBelt = util.grid().at(6, 1, 5);
        BlockPos cutTwoBelt = util.grid().at(6, 1, 4);
        BlockPos cutThreeBelt = util.grid().at(6, 1, 3);
        BlockPos cornerBelt = util.grid().at(6, 1, 2);
        BlockPos turnBelt = util.grid().at(6, 1, 1);
        BlockPos paperBelt = util.grid().at(5, 1, 1);
        BlockPos pressBelt = util.grid().at(4, 1, 1);

        Selection structure = util.select().fromTo(1, 1, 1, 8, 4, 8);
        Selection verticalBelt = util.select().fromTo(6, 1, 2, 6, 1, 8);
        Selection horizontalBelt = util.select().fromTo(2, 1, 1, 6, 1, 1);
        Selection machineKinetics = util.select().position(cutOneDeployer)
                .add(util.select().position(cutTwoDeployer))
                .add(util.select().position(cutThreeDeployer))
                .add(util.select().position(paperDeployer))
                .add(util.select().position(press))
                .add(util.select().position(driveShaft))
                .add(util.select().position(driveCog));

        ItemStack chaveta = new ItemStack(ModItems.STONE_CHAVETA.get());
        scene.world().modifyBlockEntityNBT(util.select().position(cutOneDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", chaveta.saveOptional(scene.world().getHolderLookupProvider())));
        scene.world().modifyBlockEntityNBT(util.select().position(cutTwoDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", chaveta.saveOptional(scene.world().getHolderLookupProvider())));
        scene.world().modifyBlockEntityNBT(util.select().position(cutThreeDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", chaveta.saveOptional(scene.world().getHolderLookupProvider())));

        ItemStack paper = new ItemStack(ModItems.ROLLING_PAPER.get());
        scene.world().modifyBlockEntityNBT(util.select().position(paperDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", paper.saveOptional(scene.world().getHolderLookupProvider())));

        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 4);
        ItemStack rough = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get(), 16);
        TobaccoCuringHelper.setCutType(rough, TobaccoCuringHelper.CUT_ROUGH);
        ItemStack ribbon = rough.copy();
        TobaccoCuringHelper.setCutType(ribbon, TobaccoCuringHelper.CUT_RIBBON);
        ItemStack shag = rough.copy();
        TobaccoCuringHelper.setCutType(shag, TobaccoCuringHelper.CUT_SHAG);
        ItemStack incomplete = new ItemStack(ModItems.INCOMPLETE_CIGARETTE.get(), 4);
        ItemStack cigarette = new ItemStack(ModItems.CIGARETTE.get(), 4);

        scene.world().showSection(structure, Direction.DOWN);
        scene.world().setKineticSpeed(verticalBelt, -16);
        scene.world().setKineticSpeed(horizontalBelt, 16);
        scene.world().setKineticSpeed(machineKinetics, -32);
        scene.effects().rotationDirectionIndicator(driveShaft);
        scene.effects().rotationDirectionIndicator(driveCog);

        scene.overlay().showControls(util.vector().topOf(upperChute), Pointing.DOWN, 35).withItem(cured);
        scene.overlay().showText(55)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(upperChute))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_1").getString());
        ElementLink<BeltItemElement> item = scene.world().createItemOnBelt(inputBelt, Direction.UP, cured.copy());
        showMovingItem(scene, util, inputBelt, cured, 0, 0, -0.055, 58);

        item = runTrackedDeployerStep(scene, item, cutOneBelt, cutOneDeployer, rough);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(cutTwoDeployer))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_2").getString());
        showMovingItem(scene, util, cutOneBelt, rough, 0, 0, -0.045, 27);
        item = runTrackedDeployerStep(scene, item, cutTwoBelt, cutTwoDeployer, ribbon);
        showMovingItem(scene, util, cutTwoBelt, ribbon, 0, 0, -0.045, 27);
        item = runTrackedDeployerStep(scene, item, cutThreeBelt, cutThreeDeployer, shag);

        // Recreate the tracker at the 90-degree transfer so the item remains controllable
        // after switching to the second belt controller.
        showMovingItem(scene, util, cutThreeBelt, shag, 0, 0, -0.045, 27);
        scene.world().removeItemsFromBelt(cornerBelt);
        item = scene.world().createItemOnBelt(turnBelt, Direction.UP, shag.copy());
        showMovingItem(scene, util, turnBelt, shag, -0.045, 0, 0, 27);

        scene.overlay().showControls(util.vector().topOf(paperDeployer), Pointing.DOWN, 40).withItem(paper);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(paperDeployer))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_3").getString());
        item = runTrackedDeployerStep(scene, item, paperBelt, paperDeployer, incomplete);
        showMovingItem(scene, util, paperBelt, incomplete, -0.045, 0, 0, 27);

        scene.overlay().showText(55)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(press))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_4").getString());
        item = runTrackedPressStep(scene, item, pressBelt, press, cigarette);
        showMovingItem(scene, util, pressBelt, cigarette, -0.055, 0, 0, 52);

        scene.overlay().showText(65)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(funnel))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigarette_production.text_5").getString());
        scene.idle(70);
        scene.effects().indicateSuccess(funnel);
        scene.effects().indicateSuccess(barrel);
        scene.idle(25);
    }

    public static void cigarProduction(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = beginSized(builder, "tobacconist_cigar_production", 10, 0.90f, 90);

        BlockPos barrel = util.grid().at(1, 2, 1);
        BlockPos funnel = util.grid().at(2, 2, 1);
        BlockPos wrapperDeployer = util.grid().at(5, 3, 1);
        BlockPos press = util.grid().at(4, 3, 1);
        BlockPos cutOneDeployer = util.grid().at(6, 3, 5);
        BlockPos cutTwoDeployer = util.grid().at(6, 3, 4);
        BlockPos upperChute = util.grid().at(6, 4, 7);
        BlockPos driveShaft = util.grid().at(7, 1, 2);
        BlockPos driveCog = util.grid().at(8, 1, 2);

        BlockPos inputBelt = util.grid().at(6, 1, 7);
        BlockPos cutOneBelt = util.grid().at(6, 1, 5);
        BlockPos cutTwoBelt = util.grid().at(6, 1, 4);
        BlockPos cornerBelt = util.grid().at(6, 1, 2);
        BlockPos turnBelt = util.grid().at(6, 1, 1);
        BlockPos wrapperBelt = util.grid().at(5, 1, 1);
        BlockPos pressBelt = util.grid().at(4, 1, 1);

        Selection structure = util.select().fromTo(1, 1, 1, 8, 4, 8);
        Selection verticalBelt = util.select().fromTo(6, 1, 2, 6, 1, 8);
        Selection horizontalBelt = util.select().fromTo(2, 1, 1, 6, 1, 1);
        Selection machineKinetics = util.select().position(cutOneDeployer)
                .add(util.select().position(cutTwoDeployer))
                .add(util.select().position(wrapperDeployer))
                .add(util.select().position(press))
                .add(util.select().position(driveShaft))
                .add(util.select().position(driveCog));

        ItemStack chaveta = new ItemStack(ModItems.STONE_CHAVETA.get());
        scene.world().modifyBlockEntityNBT(util.select().position(cutOneDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", chaveta.saveOptional(scene.world().getHolderLookupProvider())));
        scene.world().modifyBlockEntityNBT(util.select().position(cutTwoDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", chaveta.saveOptional(scene.world().getHolderLookupProvider())));

        ItemStack wrapper = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        scene.world().modifyBlockEntityNBT(util.select().position(wrapperDeployer), DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", wrapper.saveOptional(scene.world().getHolderLookupProvider())));

        ItemStack cured = new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get(), 4);
        ItemStack rough = new ItemStack(ModItems.TOBACCO_LOOSE_VIRGINIA.get(), 16);
        TobaccoCuringHelper.setCutType(rough, TobaccoCuringHelper.CUT_ROUGH);
        ItemStack ribbon = rough.copy();
        TobaccoCuringHelper.setCutType(ribbon, TobaccoCuringHelper.CUT_RIBBON);
        ItemStack incomplete = new ItemStack(ModItems.INCOMPLETE_CIGAR.get(), 4);
        ItemStack cigar = new ItemStack(ModItems.CIGAR.get(), 4);

        scene.world().showSection(structure, Direction.DOWN);
        scene.world().setKineticSpeed(verticalBelt, -16);
        scene.world().setKineticSpeed(horizontalBelt, 16);
        scene.world().setKineticSpeed(machineKinetics, -32);
        scene.effects().rotationDirectionIndicator(driveShaft);
        scene.effects().rotationDirectionIndicator(driveCog);

        scene.overlay().showControls(util.vector().topOf(upperChute), Pointing.DOWN, 35).withItem(cured);
        scene.overlay().showText(55)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(upperChute))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_1").getString());
        ElementLink<BeltItemElement> item = scene.world().createItemOnBelt(inputBelt, Direction.UP, cured.copy());
        showMovingItem(scene, util, inputBelt, cured, 0, 0, -0.055, 58);

        item = runTrackedDeployerStep(scene, item, cutOneBelt, cutOneDeployer, rough);
        scene.overlay().showText(65)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(cutTwoDeployer))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_2").getString());
        showMovingItem(scene, util, cutOneBelt, rough, 0, 0, -0.045, 27);
        item = runTrackedDeployerStep(scene, item, cutTwoBelt, cutTwoDeployer, ribbon);

        // Recreate the tracker at the 90-degree transfer so the item remains controllable
        // after switching to the second belt controller.
        showMovingItem(scene, util, cutTwoBelt, ribbon, 0, 0, -0.055, 55);
        scene.world().removeItemsFromBelt(cornerBelt);
        item = scene.world().createItemOnBelt(turnBelt, Direction.UP, ribbon.copy());
        showMovingItem(scene, util, turnBelt, ribbon, -0.045, 0, 0, 27);

        scene.overlay().showControls(util.vector().topOf(wrapperDeployer), Pointing.DOWN, 45).withItem(wrapper);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(wrapperDeployer))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_3").getString());
        item = runTrackedDeployerStep(scene, item, wrapperBelt, wrapperDeployer, incomplete);
        showMovingItem(scene, util, wrapperBelt, incomplete, -0.045, 0, 0, 27);

        scene.overlay().showText(55)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(press))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_4").getString());
        item = runTrackedPressStep(scene, item, pressBelt, press, cigar);
        showMovingItem(scene, util, pressBelt, cigar, -0.055, 0, 0, 52);

        scene.overlay().showText(65)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(funnel))
                .text(Component.translatable("tobacconistmod.ponder.tobacconist_cigar_production.text_5").getString());
        scene.idle(70);
        scene.effects().indicateSuccess(funnel);
        scene.effects().indicateSuccess(barrel);
        scene.idle(25);
    }

    private static void showMovingItem(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos start,
                                       ItemStack stack, double vx, double vy, double vz, int ticks) {
        Vec3 spawn = util.vector().topOf(start).add(0, -0.12, 0);
        ElementLink<EntityElement> proxy = scene.world().createItemEntity(spawn, new Vec3(vx, vy, vz), stack.copy());
        scene.world().modifyEntity(proxy, entity -> {
            entity.setNoGravity(true);
            entity.setDeltaMovement(vx, vy, vz);
        });
        scene.idle(ticks);
        scene.world().modifyEntity(proxy, Entity::discard);
    }

    private static void createDirectionalBeltBatch(CreateSceneBuilder scene, Direction movement, ItemStack stack,
                                                   BlockPos... positions) {
        for (BlockPos pos : positions) {
            scene.world().createItemOnBelt(pos, Direction.UP, stack.copy());
        }
    }

    private static void clearBeltBatch(CreateSceneBuilder scene, BlockPos... positions) {
        for (BlockPos pos : positions) {
            scene.world().removeItemsFromBelt(pos);
        }
    }

    private static ElementLink<BeltItemElement> runTrackedDeployerStep(CreateSceneBuilder scene,
                                                                         ElementLink<BeltItemElement> item,
                                                                         BlockPos belt, BlockPos deployer,
                                                                         ItemStack result) {
        scene.world().stallBeltItem(item, true);
        scene.world().moveDeployer(deployer, 1, 30);
        scene.idle(30);
        scene.world().moveDeployer(deployer, -1, 30);
        scene.world().removeItemsFromBelt(belt);

        ElementLink<BeltItemElement> output = scene.world().createItemOnBelt(belt, Direction.UP, result.copy());
        scene.world().stallBeltItem(output, true);
        scene.effects().indicateSuccess(belt);
        scene.idle(15);
        scene.world().stallBeltItem(output, false);
        return output;
    }

    private static ElementLink<BeltItemElement> runTrackedPressStep(CreateSceneBuilder scene,
                                                                    ElementLink<BeltItemElement> item,
                                                                    BlockPos belt, BlockPos press,
                                                                    ItemStack result) {
        scene.world().stallBeltItem(item, true);
        scene.world().modifyBlockEntity(press, MechanicalPressBlockEntity.class,
                pte -> pte.getPressingBehaviour().start(Mode.BELT));
        // At 32 RPM the press reaches its full downward stroke after ~30 Ponder ticks.
        scene.idle(40);
        scene.world().removeItemsFromBelt(belt);

        ElementLink<BeltItemElement> output = scene.world().createItemOnBelt(belt, Direction.UP, result.copy());
        scene.world().stallBeltItem(output, true);
        scene.effects().indicateSuccess(belt);
        scene.idle(15);
        scene.world().stallBeltItem(output, false);
        return output;
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

    private static void fillTankColumn(CreateSceneBuilder scene, FluidStack contents, BlockPos... tanks) {
        for (BlockPos tank : tanks) {
            scene.world().modifyBlockEntity(tank, FluidTankBlockEntity.class, be -> {
                be.getTankInventory().drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
                be.getTankInventory().fill(contents.copy(), FluidAction.EXECUTE);
            });
        }
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

    private static CreateSceneBuilder beginSized(SceneBuilder builder, String id, int basePlateSize, float scale) {
        return beginSized(builder, id, basePlateSize, scale, 0);
    }

    private static CreateSceneBuilder beginSized(SceneBuilder builder, String id, int basePlateSize, float scale, int cameraRotationY) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(id, Component.translatable("tobacconistmod.ponder." + id + ".header").getString());
        scene.configureBasePlate(0, 0, basePlateSize);
        scene.scaleSceneView(scale);
        scene.setSceneOffsetY(-1);
        if (cameraRotationY != 0) {
            scene.rotateCameraY(cameraRotationY);
        }
        scene.showBasePlate();
        scene.idle(10);
        return scene;
    }

    private static CreateSceneBuilder begin(SceneBuilder builder, String id) {
        return beginSized(builder, id, 6, 0.96f);
    }

    private static void reveal(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos pos) {
        scene.world().showSection(util.select().position(pos), Direction.DOWN);
        scene.idle(5);
    }
}
