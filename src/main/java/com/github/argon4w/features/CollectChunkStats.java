package com.github.argon4w.features;

import com.github.argon4w.Utils;
import com.github.argon4w.features.holders.ChunkStats;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

public final class CollectChunkStats {

    private CollectChunkStats() {

    }

    public static final DecimalFormat FORMAT;
    public static final Object2ObjectMap<ChunkPos, ChunkStats> STATS;
    public static boolean SAMPLE;

    static {
        FORMAT = new DecimalFormat("#.##");
        STATS = new Object2ObjectLinkedOpenHashMap<>();
        SAMPLE = false;

        FORMAT.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    public static void collect(ChunkPos chunkPos, long time) {
        if (!SAMPLE) {
            return;
        }

        ChunkStats stats = STATS.get(chunkPos);

        if (stats == null) {
            stats = new ChunkStats(chunkPos);
            STATS.put(chunkPos, stats);
        }

        stats.collect(time);
    }

    public static int runCollectChunkStatsCollect(CommandContext<CommandSourceStack> context) {
        SAMPLE = !SAMPLE;

        if (SAMPLE) {
            context.getSource().sendSystemMessage(Component
                    .translatable("chunk-monitor.commands.chunk-monitor.stats.collect.start")
                    .withStyle(ChatFormatting.GREEN));
        }

        if (!SAMPLE) {
            context.getSource().sendSystemMessage(Component
                    .translatable("chunk-monitor.commands.chunk-monitor.stats.collect.stop")
                    .withStyle(ChatFormatting.GREEN));

            STATS.clear();
        }

        return 1;
    }

    public static int runCollectChunkStatsWithArgument(CommandContext<CommandSourceStack> context) {
        return runCollectChunkStats(
                context.getSource(),
                IntegerArgumentType.getInteger(context, "count")
        );
    }

    public static int runCollectChunkStats(CommandContext<CommandSourceStack> context) {
        return runCollectChunkStats(
                context.getSource(),
                10
        );
    }

    public static int runCollectChunkStats(CommandSourceStack source, int count) {
        Iterator<ChunkStats> iter = STATS
                .values()
                .stream()
                .sorted(Comparator.reverseOrder())
                .iterator();

        source.sendSystemMessage(Component
                .translatable("chunk-monitor.commands.chunk-monitor.stats.info", count)
                .withStyle(ChatFormatting.GOLD));

        while (iter.hasNext() && -- count > 0) {
            ChunkStats stats = iter.next();
            ChunkPos chunkPos = stats.getChunkPos();
            BlockPos blockPos = chunkPos.getWorldPosition();
            Component teleportComponent = Utils.getTeleportComponent(blockPos);
            double time = stats.getTimeSeconds();
            int count1 = stats.getCount();
            double average = stats.getAverageSeconds();

            source.sendSystemMessage(Component.translatable(
                    "chunk-monitor.commands.chunk-monitor.stats.chunk",
                    chunkPos.x,
                    chunkPos.z,
                    teleportComponent,
                    FORMAT.format(time),
                    count1,
                    FORMAT.format(average)
            ).withStyle(ChatFormatting.GOLD));
        }

        return 1;
    }
}
