package com.diggydwarff.tobacconistmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.HookahHoseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;


import static com.diggydwarff.tobacconistmod.block.ModBlocks.BLOCKS;

public class HookahBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(HookahBlock::new);
    }

    public HookahBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(COLOR, DyeColor.LIGHT_GRAY)
                .setValue(GLOWING, false));
    }

    // Match the visible model instead of behaving like a full opaque cube.
    private static final VoxelShape SHAPE =
            Block.box(4, 0, 4, 12, 16, 12);

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, COLOR, GLOWING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(LIT, false)
                .setValue(COLOR, DyeColor.LIGHT_GRAY);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    /* BLOCK ENTITY */

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.getAbilities().instabuild) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof HookahEntity hookah) {
                hookah.clearContentsForCreativeBreak();
            }
        }

        // Never remove the block manually from playerWillDestroy. The normal break
        // lifecycle must own removal; creative loot suppression is handled separately.
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        // Skip loot generation for Creative breaks.
        if (player.getAbilities().instabuild) {
            return;
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof HookahEntity) {
                ((HookahEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = handleUse(state, level, pos, player, hand, hit);
        return result.consumesAction() ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return handleUse(state, level, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    private InteractionResult handleUse(BlockState pState, Level pLevel, BlockPos pPos,
                                 Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

        ItemStack held = pPlayer.getItemInHand(pHand);

        if (held.getItem() instanceof DyeItem dyeItem) {
            DyeColor newColor = dyeItem.getDyeColor();

            if (pState.getValue(COLOR) != newColor) {
                if (!pLevel.isClientSide) {
                    pLevel.setBlock(pPos, pState.setValue(COLOR, newColor), 3);
                    if (!pPlayer.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
        }

        if (held.is(Items.GLOW_INK_SAC)) {
            if (!pState.getValue(GLOWING)) {
                if (!pLevel.isClientSide) {
                    pLevel.setBlock(pPos, pState.setValue(GLOWING, true), 3);
                    if (!pPlayer.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
        }

        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);

            if (blockEntity instanceof HookahEntity hookah && hookah.progress > 0) {
                for (ItemStack stack : pPlayer.getHandSlots()) {
                    if (stack.getItem() instanceof HookahHoseItem) {
                        if (pPlayer.getCooldowns().isOnCooldown(stack.getItem())) {
                            return InteractionResult.sidedSuccess(false);
                        }

                        ItemStack shisha = hookah.getShishaForSmoking();
                        if (shisha.isEmpty()) return InteractionResult.PASS;

                        SmokingItem.applyHookahSmokingEffects(pPlayer, (ServerLevel) pLevel, shisha);
                        hookah.applyDirtyWaterPenalty(pPlayer);
                        pPlayer.getCooldowns().addCooldown(stack.getItem(), 20);
                        return InteractionResult.sidedSuccess(false);
                    }
                }
            }

            if(entity instanceof HookahEntity) {
                ((ServerPlayer) pPlayer).openMenu((HookahEntity) entity, buf -> buf.writeBlockPos(pPos));
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HookahEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.HOOKAH.get(),
                HookahEntity::tick);
    }
}