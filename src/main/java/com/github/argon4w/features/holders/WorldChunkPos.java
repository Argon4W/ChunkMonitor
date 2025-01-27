package com.github.argon4w.features.holders;

import com.google.common.base.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class WorldChunkPos {

    private final ResourceLocation location;
    private final ChunkPos chunkPos;
    private final BlockPos blockPos;

    public WorldChunkPos(
            ServerLevel level,
            int x,
            int z
    ) {
        this.location = level.dimension().location();
        this.chunkPos = new ChunkPos(x, z);
        this.blockPos = chunkPos.getWorldPosition();
    }

    public Component getComponent() {
        return Component.translatable(
                "chunk-monitor.chat.chunks",
                chunkPos.x,
                chunkPos.z
        ).withStyle(style -> style
                .withColor(ChatFormatting.YELLOW)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute as @s in %s run tp @s %d ~ %d".formatted(location, blockPos.getX(), blockPos.getZ())))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chunk-monitor.chat.chunks.tooltip", location)))
        );
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                location,
                chunkPos
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        WorldChunkPos that = (WorldChunkPos) obj;

        if (!that.location.equals(this.location)) {
            return false;
        }

        if (!that.chunkPos.equals(this.chunkPos)) {
            return false;
        }

        return true;
    }
}
