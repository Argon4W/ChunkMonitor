package com.github.argon4w.mixin;

import com.github.argon4w.features.CollectChunkStats;
import com.github.argon4w.features.ReportChunkTimeout;
import com.github.argon4w.features.holders.WorldChunkPos;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BooleanSupplier;

@Mixin(value = ServerChunkCache.class, priority = Integer.MIN_VALUE)
public class ServerChunkCacheMixin {

    @Shadow @Final ServerLevel level;

    @WrapOperation(
            method = {"getChunkFuture", "getChunk"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache$MainThreadExecutor;managedBlock(Ljava/util/function/BooleanSupplier;)V")
    )
    public void wrapExecutorManagedBlock(
            ServerChunkCache.MainThreadExecutor instance,
            BooleanSupplier booleanSupplier,
            Operation<Void> original,
            @Local(ordinal = 0, argsOnly = true) int x,
            @Local(ordinal = 1, argsOnly = true) int z,
            @Local(ordinal = 0, argsOnly = true) ChunkStatus chunkStatus
    ) {
        if (chunkStatus != ChunkStatus.FULL) {
            return;
        }

        long time = System.currentTimeMillis();
        int count = instance.blockingCount;

        original.call(instance, booleanSupplier);

        long endTime = System.currentTimeMillis();
        long delta = endTime - time;
        WorldChunkPos chunkPos = new WorldChunkPos(level, x, z);

        CollectChunkStats.collect(chunkPos, delta);
        ReportChunkTimeout.report(chunkPos, delta, count);
    }
}
