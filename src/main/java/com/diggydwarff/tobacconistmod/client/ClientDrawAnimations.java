package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.custom.HookahHoseItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.WoodenSmokingPipeItem;
import com.diggydwarff.tobacconistmod.util.SmokingDraws;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Small native item transforms, with no animation-library or model dependency. */
@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class ClientDrawAnimations {
    private static final Map<InteractionHand, HandBlend> HANDS = new EnumMap<>(InteractionHand.class);
    private static final Map<Integer, Integer> MOUTH_DRAWS = new HashMap<>();
    private static ClientLevel currentLevel;
    private static final DustParticleOptions EMBER = new DustParticleOptions(new Vector3f(1.0F, 0.26F, 0.035F), 0.28F);

    private ClientDrawAnimations() {}

    @SubscribeEvent
    public static void register(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                if (TobacconistConfig.areSmokingDrawAnimationsEnabled() && entity.isUsingItem()
                        && entity.getUsedItemHand() == hand && SmokingDraws.isSupported(stack)) {
                    // Vanilla already has a one-handed pose which places a held item near the lips.
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }
                return null;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack pose, LocalPlayer player, HumanoidArm arm,
                                                  ItemStack stack, float partialTick, float equip, float swing) {
                if (!TobacconistConfig.areSmokingDrawAnimationsEnabled()) return false;
                InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                HandBlend state = HANDS.get(hand);
                if (state == null || state.item != stack.getItem()) return false;
                float amount = Mth.lerp(partialTick, state.previous, state.amount);
                if (amount <= 0.001F) return false;
                float eased = amount * amount * (3.0F - 2.0F * amount);
                int side = arm == HumanoidArm.RIGHT ? 1 : -1;
                boolean pipe = stack.getItem() instanceof WoodenSmokingPipeItem;
                boolean hose = stack.getItem() instanceof HookahHoseItem;
                float breathing = player.isUsingItem()
                        ? Mth.sin((player.tickCount + partialTick) * 0.22F) * 0.005F * eased : 0.0F;

                // Start from vanilla's idle position and bring the mouthpiece inward/upward.
                pose.translate(side * (0.56F - 0.31F * eased), -0.52F - equip * 0.6F + 0.32F * eased + breathing,
                        -0.72F + 0.15F * eased);
                pose.mulPose(Axis.YP.rotationDegrees(side * (pipe ? 30.0F : 45.0F) * eased));
                pose.mulPose(Axis.ZP.rotationDegrees(side * (pipe ? -28.0F : hose ? -38.0F : -52.0F) * eased));
                pose.mulPose(Axis.XP.rotationDegrees((pipe ? -12.0F : -18.0F) * eased));
                return true;
            }
        };
        for (Item item : BuiltInRegistries.ITEM) {
            if (SmokingDraws.isSupported(item.getDefaultInstance()) && !event.isItemRegistered(item)) {
                event.registerItem(extensions, item);
            }
        }
    }

    public static void receiveMouthDraw(int entityId, int ticks) {
        if (ticks <= 0) MOUTH_DRAWS.remove(entityId);
        else MOUTH_DRAWS.put(entityId, Math.min(ticks, 100));
    }

    public static boolean isMouthDrawing(Player player) {
        return TobacconistConfig.areSmokingDrawAnimationsEnabled() && MOUTH_DRAWS.containsKey(player.getId());
    }

    public static void ember(Player player, ItemStack stack) {
        if (stack.getItem() instanceof HookahHoseItem || player.level().getGameTime() % 4 != 0) return;
        Vec3 facing = Vec3.directionFromRotation(0.0F, player.getYHeadRot());
        Vec3 right = new Vec3(facing.z, 0.0D, -facing.x);
        boolean pipe = stack.getItem() instanceof WoodenSmokingPipeItem;
        Vec3 position = player.getEyePosition().add(0.0D, pipe ? -0.28D : -0.16D, 0.0D)
                .add(facing.scale(pipe ? 0.27D : 0.34D)).add(right.scale(pipe ? 0.17D : 0.10D));
        player.level().addParticle(EMBER, position.x, position.y, position.z, 0.0D, 0.002D, 0.0D);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != currentLevel) {
            HANDS.clear();
            MOUTH_DRAWS.clear();
            currentLevel = minecraft.level;
        }
        if (minecraft.player == null || minecraft.level == null || minecraft.isPaused()) return;
        if (!TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            HANDS.clear();
            MOUTH_DRAWS.clear();
            return;
        }
        MOUTH_DRAWS.replaceAll((id, remaining) -> remaining - 1);
        MOUTH_DRAWS.values().removeIf(remaining -> remaining <= 0);
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = minecraft.player.getItemInHand(hand);
            HandBlend state = HANDS.computeIfAbsent(hand, ignored -> new HandBlend());
            if (state.item != stack.getItem()) {
                state.item = stack.getItem();
                state.amount = 0.0F;
            }
            state.previous = state.amount;
            boolean drawing = minecraft.player.isUsingItem() && minecraft.player.getUsedItemHand() == hand
                    && SmokingDraws.isSupported(stack);
            state.amount = Mth.clamp(state.amount + (drawing ? 0.20F : -0.20F), 0.0F, 1.0F);
        }
        for (Player player : minecraft.level.players()) {
            if (player.isUsingItem() && SmokingDraws.isSupported(player.getUseItem()) && player.getTicksUsingItem() > 5) {
                ember(player, player.getUseItem());
            }
        }
    }

    private static final class HandBlend {
        private Item item;
        private float previous;
        private float amount;
    }
}
