package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.custom.HookahHoseItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.WoodenSmokingPipeItem;
import com.diggydwarff.tobacconistmod.util.SmokingDraws;
import com.diggydwarff.tobacconistmod.vapor.VapePenItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Small native item transforms, with no animation-library or model dependency. */
@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class ClientDrawAnimations {
    private static final Map<InteractionHand, HandBlend> HANDS = new EnumMap<>(InteractionHand.class);
    private static final Map<Integer, Integer> MOUTH_DRAWS = new HashMap<>();
    private static ClientLevel currentLevel;

    private ClientDrawAnimations() {}

    @SubscribeEvent
    public static void register(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                if (TobacconistConfig.areSmokingDrawAnimationsEnabled() && entity.isUsingItem()
                        && entity.getUsedItemHand() == hand && supportsDrawPose(stack)) {
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

                if (pipe || hose) {
                    // These sprites put the mouthpiece on the right. Turn that end toward the
                    // camera and anchor it near the lips; the bowl/cord stays farther away.
                    float mouthX = hose ? 0.31F : 0.43F;
                    float mouthY = hose ? 0.31F : 0.36F;
                    if (stack.is(ModItems.EMERALD_AZTEC_SMOKING_PIPE.get())) mouthY = -0.10F;
                    if (stack.is(ModItems.KISERU_SMOKING_PIPE.get())) {
                        mouthX = 0.32F;
                        mouthY = -0.14F;
                    }
                    float yaw = hose ? -90.0F - side * 35.0F : -90.0F - side * 45.0F;
                    float pitch = -side * (hose ? 35.0F : 45.0F);
                    float size = hose ? 0.60F : 0.65F;
                    float yawRadians = yaw * Mth.DEG_TO_RAD;
                    float pitchRadians = pitch * Mth.DEG_TO_RAD;
                    float rotatedY = Mth.cos(pitchRadians) * mouthY;
                    float rotatedZ = Mth.sin(pitchRadians) * mouthY;
                    float rotatedX = Mth.cos(yawRadians) * mouthX + Mth.sin(yawRadians) * rotatedZ;
                    float depth = -Mth.sin(yawRadians) * mouthX + Mth.cos(yawRadians) * rotatedZ;
                    float targetX = side * 0.08F - rotatedX * size;
                    float targetY = -0.14F - rotatedY * size;
                    float targetZ = -0.28F - depth * size;
                    pose.translate(Mth.lerp(eased, side * 0.56F, targetX),
                            Mth.lerp(eased, -0.52F, targetY) - equip * 0.6F + breathing,
                            Mth.lerp(eased, -0.72F, targetZ));
                    pose.mulPose(Axis.YP.rotationDegrees(yaw * eased));
                    pose.mulPose(Axis.XP.rotationDegrees(pitch * eased));
                    float scale = Mth.lerp(eased, 1.0F, size);
                    pose.scale(scale, scale, scale);
                    removeDisplayTransform(pose, player, stack, arm, eased);
                } else {
                    pose.translate(side * (0.56F - 0.31F * eased), -0.52F - equip * 0.6F + 0.32F * eased + breathing,
                            -0.72F + 0.15F * eased);
                    pose.mulPose(Axis.YP.rotationDegrees(side * 45.0F * eased));
                    pose.mulPose(Axis.ZP.rotationDegrees(side * -52.0F * eased));
                    pose.mulPose(Axis.XP.rotationDegrees(-18.0F * eased));
                }
                return true;
            }
        };
        for (Item item : BuiltInRegistries.ITEM) {
            if (supportsDrawPose(item.getDefaultInstance()) && !event.isItemRegistered(item)) {
                event.registerItem(extensions, item);
            }
        }
    }

    private static boolean supportsDrawPose(ItemStack stack) {
        return SmokingDraws.isSupported(stack) || stack.getItem() instanceof VapePenItem;
    }

    public static void receiveMouthDraw(int entityId, int ticks) {
        if (ticks <= 0) MOUTH_DRAWS.remove(entityId);
        else MOUTH_DRAWS.put(entityId, Math.min(ticks, 100));
    }

    private static void removeDisplayTransform(PoseStack pose, LocalPlayer player, ItemStack stack,
                                               HumanoidArm arm, float amount) {
        // ItemRenderer will apply the baked first-person display transform after this hook.
        // Fade in its inverse so the fully raised pose is anchored in sprite coordinates,
        // rather than inheriting item/generated's extra yaw, roll, scale and translation.
        ItemDisplayContext context = arm == HumanoidArm.RIGHT
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        ItemTransform display = Minecraft.getInstance().getItemRenderer()
                .getModel(stack, player.level(), player, 0).getTransforms().getTransform(context);
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        pose.mulPose(Axis.ZP.rotationDegrees(-side * display.rightRotation.z() * amount));
        pose.mulPose(Axis.YP.rotationDegrees(-side * display.rightRotation.y() * amount));
        pose.mulPose(Axis.XP.rotationDegrees(-display.rightRotation.x() * amount));
        pose.scale(Mth.lerp(amount, 1.0F, inverseScale(display.scale.x())),
                Mth.lerp(amount, 1.0F, inverseScale(display.scale.y())),
                Mth.lerp(amount, 1.0F, inverseScale(display.scale.z())));
        pose.mulPose(Axis.ZP.rotationDegrees(-side * display.rotation.z() * amount));
        pose.mulPose(Axis.YP.rotationDegrees(-side * display.rotation.y() * amount));
        pose.mulPose(Axis.XP.rotationDegrees(-display.rotation.x() * amount));
        pose.translate(-side * display.translation.x() * amount,
                -display.translation.y() * amount, -display.translation.z() * amount);
    }

    private static float inverseScale(float scale) {
        return Math.abs(scale) < 0.00001F ? 1.0F : 1.0F / scale;
    }

    public static boolean isMouthDrawing(Player player) {
        return TobacconistConfig.areSmokingDrawAnimationsEnabled() && MOUTH_DRAWS.containsKey(player.getId());
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
            boolean drawing = minecraft.player.isUsingItem() && minecraft.player.getUseItemRemainingTicks() > 0
                    && minecraft.player.getUsedItemHand() == hand
                    && supportsDrawPose(stack);
            state.amount = Mth.clamp(state.amount + (drawing ? 0.20F : -0.20F), 0.0F, 1.0F);
        }

    }

    private static final class HandBlend {
        private Item item;
        private float previous;
        private float amount;
    }
}
