package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Makes Create's Mechanical Harvester understand Tobacconist's two-block crop lifecycle.
 *
 * <p>Create normally sees the lower tobacco half as age 3/7, so a standard-height harvester
 * never considers the mature two-block plant ready. If it reaches the upper half instead,
 * Create's generic CropBlock reset produces an invalid upper-half age 0 state. Intercepting
 * tobacco here lets the machine harvest the mature upper leaves exactly once and leave the
 * lower plant in the same regrowth state as a manual upper-leaf harvest.</p>
 */
@Mixin(value = HarvesterMovementBehaviour.class, remap = false)
public abstract class HarvesterMovementBehaviourMixin {

    @Inject(method = "visitNewPosition", at = @At("HEAD"), cancellable = true, remap = false)
    private void tobacconist$harvestTallTobacco(MovementContext context, BlockPos visitedPos, CallbackInfo ci) {
        Level level = context.world;
        if (level.isClientSide) {
            return;
        }

        BlockState visitedState = level.getBlockState(visitedPos);
        if (!(visitedState.getBlock() instanceof AbstractTallTobaccoCropBlock crop)) {
            return;
        }

        // Preserve Create/datapack opt-outs.
        if (AllBlockTags.NON_HARVESTABLE.matches(visitedState)) {
            return;
        }

        // From here on, Tobacconist owns the crop handling. This also prevents Create's
        // "harvest partially grown" option from tearing apart either half of the tall crop.
        ci.cancel();

        BlockPos basePos = visitedState.getValue(AbstractTallTobaccoCropBlock.HALF) == DoubleBlockHalf.UPPER
                ? visitedPos.below()
                : visitedPos;
        BlockPos upperPos = basePos.above();

        BlockState baseState = level.getBlockState(basePos);
        BlockState upperState = level.getBlockState(upperPos);
        if (!baseState.is(crop)
                || baseState.getValue(AbstractTallTobaccoCropBlock.HALF) != DoubleBlockHalf.LOWER
                || !upperState.is(crop)
                || upperState.getValue(AbstractTallTobaccoCropBlock.HALF) != DoubleBlockHalf.UPPER
                || crop.getEffectiveAge(level, basePos, baseState) < crop.getMaxAge()) {
            return;
        }

        List<ItemStack> drops = crop.getAutomationHarvestDrops(level, basePos);
        if (drops.isEmpty()) {
            return;
        }

        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, upperPos, Block.getId(upperState));

        if (level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            MovementBehaviour behaviour = (MovementBehaviour) (Object) this;
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    behaviour.dropItem(context, drop);
                }
            }
        }

        // Tobacco is harvested from the upper growth only. With Create's replant option on,
        // the lower age-3 plant remains rooted and naturally grows a fresh upper half later,
        // exactly like Tobacconist's manual upper-leaf harvest. If replanting is disabled,
        // remove the rooted lower half too.
        level.setBlock(upperPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (!AllConfigs.server().kinetics.harvesterReplants.get()) {
            level.setBlock(basePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
