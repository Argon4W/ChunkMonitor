package com.github.argon4w.features;

import com.github.argon4w.ChunkMonitor;
import com.github.argon4w.Utils;
import com.github.argon4w.features.holders.ReportMessageReceiver;
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

public final class ReportChunkTimeout {

    private ReportChunkTimeout() {

    }

    public static final Object2ObjectMap<UUID, ReportMessageReceiver> RECEIVERS;

    static {
        RECEIVERS = new Object2ObjectLinkedOpenHashMap<>();
    }

    public static void report(
            ChunkPos chunkPos,
            long time,
            int count
    ) {
        if (RECEIVERS.isEmpty()) {
            return;
        }

        Set<UUID> offline = new ObjectOpenHashSet<>();

       for (UUID uuid : RECEIVERS.keySet()) {
           ReportMessageReceiver receiver = RECEIVERS.get(uuid);
           int timeout = receiver.getTimeout();

           if (time < timeout) {
               continue;
           }

           if (!receiver.isOnline()) {
               offline.add(uuid);
               continue;
           }

           BlockPos blockPos = chunkPos.getWorldPosition();
           Component teleportComponent = Utils.getTeleportComponent(blockPos);

           receiver.sendMessage(Component.translatable(
                   "chunk-monitor.commands.chunk-monitor.report.report",
                   chunkPos.x,
                   chunkPos.z,
                   teleportComponent,
                   time,
                   timeout,
                   count
           ).withStyle(ChatFormatting.GOLD));
        }

        if (offline.isEmpty()) {
            return;
        }

        for (UUID uuid : offline) {
            ReportMessageReceiver receiver = RECEIVERS.remove(uuid);
            ChunkMonitor.LOGGER.info("Remove {} from receivers as the player is offline.", receiver
                    .getName()
                    .getString());
        }
    }

    public static int runReportChunkTimeoutWithArgument(CommandContext<CommandSourceStack> context) {
        return runReportChunkTimeout(
                context.getSource(),
                IntegerArgumentType.getInteger(context, "timeout"),
                false
        );
    }

    public static int runReportChunkTimeout(CommandContext<CommandSourceStack> context) {
        return runReportChunkTimeout(
                context.getSource(),
                50,
                true
        );
    }

    private static int runReportChunkTimeout(
            CommandSourceStack source,
            int timeout,
            boolean remove
    ) {
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
        ReportMessageReceiver receiver = RECEIVERS.get(uuid);
        ReportMessageReceiver newReceiver = new ReportMessageReceiver(
                source,
                player,
                timeout
        );

        if (receiver == null) {
            RECEIVERS.put(uuid, newReceiver);
            source.sendFailure(Component.translatable(
                    "chunk-monitor.commands.chunk-monitor.report.add",
                    newReceiver.getName(),
                    newReceiver.getTimeout()
            ).withStyle(ChatFormatting.GREEN));

            return 1;
        }

        if (!remove) {
            RECEIVERS.put(uuid, newReceiver);
            source.sendSystemMessage(Component.translatable(
                    "chunk-monitor.commands.chunk-monitor.report.update",
                    receiver.getName(),
                    receiver.getTimeout(),
                    newReceiver.getTimeout()
            ).withStyle(ChatFormatting.GREEN));

            return 1;
        }

        RECEIVERS.remove(uuid);
        source.sendSystemMessage(Component.translatable(
                "chunk-monitor.commands.chunk-monitor.report.remove",
                receiver.getName(),
                receiver.getTimeout()
        ).withStyle(ChatFormatting.GREEN));

        return 1;
    }
}
