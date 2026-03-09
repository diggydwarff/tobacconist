package com.diggydwarff.tobacconistmod.block.custom;

import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.datagen.items.custom.HookahHoseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.diggydwarff.tobacconistmod.block.ModBlocks.BLOCKS;

public class HookahBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    public HookahBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(COLOR, DyeColor.LIGHT_GRAY)
                .setValue(GLOWING, false));
    }

    private static final VoxelShape SHAPE =
            Block.box(0, 0, 0, 16, 10, 16);

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
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
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos,
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

            try{

                boolean isSmoking = ((HookahEntity) blockEntity).progress > 0;
                if(isSmoking){
                    for(ItemStack stack : pPlayer.getHandSlots()){
                        if(stack.getItem() instanceof HookahHoseItem){

                            Vec3 look = pPlayer.getLookAngle();
                            Vec3 eyePos = new Vec3(pPlayer.getX(), pPlayer.getY()+1.4, pPlayer.getZ());

                            look.multiply(0.3D, 0.3D, 0.3D);
                            eyePos.add(look);
                            look.multiply(0.066D, 0.066D, 0.066D);

                            Random rand = new Random();
                            for (int i = 0; i < 5; ++i)
                            {
                                Vec3 newVec = (new Vec3(rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D));
                                newVec.multiply(0.01D, 0.01D, 0.01D);
                                Vec3 mergeVec = look.add(newVec);
                                ServerLevel sLevel = (ServerLevel) pLevel;
                                sLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pPlayer.getX()+mergeVec.x, pPlayer.getY()+1.4+mergeVec.y, pPlayer.getZ()+mergeVec.z, 1, 0, 0, 0, 0);
                            }

                            return InteractionResult.sidedSuccess(pLevel.isClientSide());
                        }
                    }
                }
            }
            catch(Exception ex){

            }

            if(entity instanceof HookahEntity) {
                NetworkHooks.openScreen(((ServerPlayer)pPlayer), (HookahEntity)entity, pPos);
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