package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoCrateBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/** A lossless bulk-storage crate: filled crates break back into their exact nine inputs. */
public class TobaccoCrateBlock extends BaseEntityBlock {
    private static final ThreadLocal<Set<BlockPos>> CREATIVE_BREAKS = ThreadLocal.withInitial(HashSet::new);

    public TobaccoCrateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TobaccoCrateBlock::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TobaccoCrateBlockEntity crate) {
            crate.loadFromCrateItem(stack, level.registryAccess());
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.getAbilities().instabuild) {
            CREATIVE_BREAKS.get().add(pos.immutable());
            if (level.getBlockEntity(pos) instanceof TobaccoCrateBlockEntity crate) {
                crate.suppressDrops();
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                Set<BlockPos> creative = CREATIVE_BREAKS.get();
                boolean creativeBreak = creative.remove(pos);
                try {
                    if (!creativeBreak) {
                        if (level.getBlockEntity(pos) instanceof TobaccoCrateBlockEntity crate) {
                            if (!crate.shouldSuppressDrops() && crate.hasContents()) {
                                crate.dropContents(level, pos);
                            } else if (!crate.shouldSuppressDrops()) {
                                popResource(level, pos, new ItemStack(this));
                            }
                        } else {
                            // Backward compatibility for crates placed before they gained a block entity.
                            popResource(level, pos, new ItemStack(this));
                        }
                    }
                } finally {
                    if (creative.isEmpty()) CREATIVE_BREAKS.remove();
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TobaccoCrateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        // Do not let piston movement detach the block from its exact stored batch.
        return PushReaction.BLOCK;
    }
}
