package com.github.argon4w.features.holders;

import org.jetbrains.annotations.NotNull;

public final class ChunkStats implements Comparable<ChunkStats> {

    private final WorldChunkPos chunkPos;

    private long time;
    private int count;

    public ChunkStats(WorldChunkPos chunkPos) {
        this.chunkPos = chunkPos;
        this.time = 0L;
        this.count = 0;
    }

    public void collect(long time) {
        this.time += time;
        this.count += 1;
    }

    public double getTimeSeconds() {
        return time / 1000.0;
    }

    public double getAverageSeconds() {
        return getTimeSeconds() / count;
    }

    public WorldChunkPos getChunkPos() {
        return chunkPos;
    }

    public long getTime() {
        return time;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(@NotNull ChunkStats o) {
        return Long.compare(time, o.time);
    }
}
