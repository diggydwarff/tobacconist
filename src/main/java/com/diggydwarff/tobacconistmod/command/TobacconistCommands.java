package com.diggydwarff.tobacconistmod.command;

import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.util.TobaccoCropDebugHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoTestItemFactory;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class TobacconistCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tobacconist")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("debug")
                                .executes(ctx -> runDebug(ctx.getSource())))
                        .then(Commands.literal("give")
                                .executes(ctx -> showGiveHelp(ctx.getSource()))
                                .then(Commands.literal("help")
                                        .executes(ctx -> showGiveHelp(ctx.getSource())))
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                new String[] {
                                                        "raw", "leaf", "loose", "blend",
                                                        "cigarette", "cigar", "shisha"
                                                },
                                                builder
                                        ))
                                        .executes(ctx -> giveTestItem(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "type"),
                                                ""
                                        ))
                                        .then(Commands.argument("options", StringArgumentType.greedyString())
                                                .executes(ctx -> giveTestItem(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "type"),
                                                        StringArgumentType.getString(ctx, "options")
                                                )))))
                        .then(Commands.literal("barrel")
                                .then(Commands.literal("ferment")
                                        .executes(ctx -> forceBarrelFerment(ctx.getSource())))
                                .then(Commands.literal("age")
                                        .then(Commands.argument("days", IntegerArgumentType.integer(1))
                                                .executes(ctx -> forceBarrelAge(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "days")
                                                ))))
                                .then(Commands.literal("ruin")
                                        .executes(ctx -> forceBarrelRuin(ctx.getSource()))))
                        .then(Commands.literal("rack")
                                .then(Commands.literal("finish")
                                        .executes(ctx -> forceRackFinish(ctx.getSource())))
                                .then(Commands.literal("addtime")
                                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1))
                                                .executes(ctx -> forceRackAddTime(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "ticks")
                                                ))))
                                .then(Commands.literal("status")
                                        .executes(ctx -> rackStatus(ctx.getSource()))))
        );
    }

    private static int showGiveHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(TobaccoTestItemFactory.helpText()), false);
        source.sendSuccess(() -> Component.literal(
                "Example: /tobacconist give loose variety=virginia quality=90 cure=flue cut=shag flavor=berry fermented=true age=365"
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Blend example: /tobacconist give blend components=virginia:95:flue:none,burley:90:air:berry,shade:88:sun:none cut=shag"
        ), false);
        return 1;
    }

    private static int giveTestItem(CommandSourceStack source, String type, String options) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        final ItemStack stack;
        try {
            stack = TobaccoTestItemFactory.create(type, options);
        } catch (IllegalArgumentException ex) {
            source.sendFailure(Component.literal(ex.getMessage()));
            return 0;
        }

        ItemStack given = stack.copy();
        if (!player.addItem(given)) {
            player.drop(given, false);
        }

        source.sendSuccess(() -> Component.literal(
                "Gave " + stack.getCount() + "x " + stack.getHoverName().getString()
        ), false);
        return stack.getCount();
    }

    private static int runDebug(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) {
            source.sendFailure(Component.literal("No block in sight."));
            return 0;
        }

        BlockPos pos = blockHit.getBlockPos();
        Level level = player.level();
        BlockEntity be = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);

        if (be instanceof TobaccoBarrelBlockEntity barrel) {
            for (Component line : barrel.getFullDebugLines()) {
                source.sendSuccess(() -> line, false);
            }
            return 1;
        }

        if (be instanceof TobaccoDryingRackBlockEntity rack) {
            for (Component line : rack.getFullDebugLines()) {
                source.sendSuccess(() -> line, false);
            }
            return 1;
        }

        if (state.getBlock() instanceof AbstractTallTobaccoCropBlock) {
            for (Component line : TobaccoCropDebugHelper.getFullDebugLines(level, pos, state)) {
                source.sendSuccess(() -> line, false);
            }
            return 1;
        }

        source.sendFailure(Component.literal("Not a tobacconist block."));
        return 0;
    }

    private static int forceBarrelFerment(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TobaccoBarrelBlockEntity barrel = getLookedAtBarrel(player);

        if (barrel == null) {
            source.sendFailure(Component.literal("No tobacco barrel in sight."));
            return 0;
        }

        barrel.forceFinishFermentation();
        source.sendSuccess(() -> Component.literal("Forced barrel fermentation."), false);
        return 1;
    }

    private static int forceBarrelAge(CommandSourceStack source, int days) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TobaccoBarrelBlockEntity barrel = getLookedAtBarrel(player);

        if (barrel == null) {
            source.sendFailure(Component.literal("No tobacco barrel in sight."));
            return 0;
        }

        barrel.addAgedDays(days);
        source.sendSuccess(() -> Component.literal("Added " + days + " aging days to barrel."), false);
        return 1;
    }

    private static int forceBarrelRuin(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TobaccoBarrelBlockEntity barrel = getLookedAtBarrel(player);

        if (barrel == null) {
            source.sendFailure(Component.literal("No tobacco barrel in sight."));
            return 0;
        }

        barrel.forceRuin();
        source.sendSuccess(() -> Component.literal("Ruined barrel contents."), false);
        return 1;
    }

    private static int forceRackFinish(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TobaccoDryingRackBlockEntity rack = getLookedAtRack(player);

        if (rack == null) {
            source.sendFailure(Component.literal("No tobacco drying rack in sight."));
            return 0;
        }

        if (!rack.hasLeaves()) {
            source.sendFailure(Component.literal("That drying rack is empty."));
            return 0;
        }

        if (rack.isFinished()) {
            source.sendFailure(Component.literal("That drying rack is already finished."));
            return 0;
        }

        rack.debugFinishNow();
        source.sendSuccess(() -> Component.literal("Forced rack curing to finish."), false);
        return 1;
    }

    private static int forceRackAddTime(CommandSourceStack source, int ticks) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TobaccoDryingRackBlockEntity rack = getLookedAtRack(player);

        if (rack == null) {
            source.sendFailure(Component.literal("No tobacco drying rack in sight."));
            return 0;
        }

        if (!rack.hasLeaves()) {
            source.sendFailure(Component.literal("That drying rack is empty."));
            return 0;
        }

        if (rack.isFinished()) {
            source.sendFailure(Component.literal("That drying rack is already finished."));
            return 0;
        }

        int before = rack.getDryProgressPercent();
        rack.debugAddTime(ticks);
        int after = rack.getDryProgressPercent();

        source.sendSuccess(() -> Component.literal(
                "Added " + ticks + " ticks to drying rack (" + before + "% -> " + after + "%)."
        ), false);
        return 1;
    }

    private static int rackStatus(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TobaccoDryingRackBlockEntity rack = getLookedAtRack(player);

        if (rack == null) {
            source.sendFailure(Component.literal("No tobacco drying rack in sight."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Rack: " + rack.getRackStatusText()), false);
        source.sendSuccess(() -> Component.literal("Progress: " + rack.getDryProgressPercent() + "%"), false);
        return 1;
    }

    @Nullable
    private static TobaccoBarrelBlockEntity getLookedAtBarrel(ServerPlayer player) {
        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) {
            return null;
        }

        BlockEntity be = player.level().getBlockEntity(blockHit.getBlockPos());
        return be instanceof TobaccoBarrelBlockEntity barrel ? barrel : null;
    }

    @Nullable
    private static TobaccoDryingRackBlockEntity getLookedAtRack(ServerPlayer player) {
        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) {
            return null;
        }

        BlockEntity be = player.level().getBlockEntity(blockHit.getBlockPos());
        return be instanceof TobaccoDryingRackBlockEntity rack ? rack : null;
    }
}