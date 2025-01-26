package com.github.argon4w.commands;

import com.github.argon4w.ChunkMonitor;
import com.github.argon4w.Utils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Set;
import java.util.UUID;

public final class ReportTimeout {

    private ReportTimeout() {

    }

    public static final Object2ObjectMap<UUID, Reporter> REPORTERS = new Object2ObjectLinkedOpenHashMap<>();

    public static void report(ChunkPos chunkPos, long time, int count) {
        if (REPORTERS.isEmpty()) {
            return;
        }

        Set<UUID> offline = new ObjectOpenHashSet<>();

       for (UUID uuid : REPORTERS.keySet()) {
           Reporter reporter = REPORTERS.get(uuid);
           int timeout = reporter.getTimeout();

           if (time < timeout) {
               continue;
           }

           if (!reporter.isOnline()) {
               offline.add(uuid);
               continue;
           }

           BlockPos blockPos = chunkPos.getWorldPosition();
           Component teleportComponent = Utils.getTeleportComponent(blockPos);

           reporter.sendMessage(Component.translatable(
                   "chunk-monitor.commands.chunk-monitor.report.report",
                   chunkPos.x,
                   chunkPos.z,
                   teleportComponent,
                   time,
                   reporter.getTimeout(),
                   count
           ).withStyle(ChatFormatting.GOLD));
        }

        if (offline.isEmpty()) {
            return;
        }

        for (UUID uuid : offline) {
            Reporter reporter = REPORTERS.remove(uuid);
            ChunkMonitor.LOGGER.info("Remove {} from reporters as the player is offline.", reporter
                    .getName()
                    .getString());
        }
    }

    public static int runReportTimeoutWithArgument(CommandContext<CommandSourceStack> context) {
        return runReportTimeout(
                context.getSource(),
                IntegerArgumentType.getInteger(context, "timeout")
        );
    }

    public static int runReportTimeout(CommandContext<CommandSourceStack> context) {
        return runReportTimeout(
                context.getSource(),
                50
        );
    }

    private static int runReportTimeout(CommandSourceStack source, int timeout) {
        if (!source.isPlayer()) {
            source.sendFailure(Component
                    .translatable("chunk-monitor.commands.chunk-monitor.report.must-be-player")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(Component
                    .translatable("chunk-monitor.commands.chunk-monitor.report.internal-error")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        UUID uuid = player.getUUID();
        Reporter reporter = REPORTERS.get(uuid);
        Reporter newReporter = new Reporter(
                source,
                player,
                timeout
        );

        if (reporter == null) {
            REPORTERS.put(uuid, newReporter);
            source.sendFailure(Component.translatable(
                    "chunk-monitor.commands.chunk-monitor.report.add",
                    newReporter.getName(),
                    newReporter.getTimeout()
            ).withStyle(ChatFormatting.GREEN));

            return 1;
        }

        if (reporter.getTimeout() != timeout) {
            REPORTERS.put(uuid, newReporter);
            reporter.sendMessage(Component.translatable(
                    "chunk-monitor.commands.chunk-monitor.report.update",
                    reporter.getName(),
                    reporter.getTimeout(),
                    newReporter.getTimeout()
            ).withStyle(ChatFormatting.GREEN));

            return 1;
        }

        REPORTERS.remove(uuid);
        reporter.sendMessage(Component.translatable(
                "chunk-monitor.commands.chunk-monitor.report.remove",
                reporter.getName(),
                reporter.getTimeout()
        ));

        return 1;
    }
}
