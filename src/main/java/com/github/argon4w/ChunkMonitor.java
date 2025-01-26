package com.github.argon4w;

import com.github.argon4w.features.ReportChunkTimeout;
import com.github.argon4w.features.CollectChunkStats;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChunkMonitor implements ModInitializer {

	public static final String MOD_ID = "chunk-monitor";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((
				dispatcher,
				context,
				selection
		) -> dispatcher.register(Commands
				.literal("chunk-monitor")
				.requires(source -> source.hasPermission(4))
				.then(Commands
						.literal("report")
						.then(Commands
								.argument("timeout", IntegerArgumentType.integer())
								.executes(ReportChunkTimeout::runReportChunkTimeoutWithArgument))
						.executes(ReportChunkTimeout::runReportChunkTimeout))
				.then(Commands.literal("stats")
						.then(Commands
								.literal("collect")
								.executes(CollectChunkStats::runCollectChunkStatsCollect))
						.then(Commands
								.argument("count", IntegerArgumentType.integer())
								.executes(CollectChunkStats::runCollectChunkStatsWithArgument))
						.executes(CollectChunkStats::runCollectChunkStats))
		));
	}
}