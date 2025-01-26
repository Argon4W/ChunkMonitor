package com.github.argon4w.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public final class Reporter {

    private final CommandSourceStack source;
    private final ServerPlayer player;
    private final int timeout;

    public Reporter(
            CommandSourceStack source,
            ServerPlayer player,
            int timeout
    ) {
        this.source = source;
        this.player = player;
        this.timeout = timeout;
    }

    public int sendFailure(Component component) {
        source.sendFailure(component);
        return 0;
    }

    public int sendSuccess(Supplier<Component> component) {
        source.sendSuccess(component, true);
        return 1;
    }

    public void sendMessage(Component component) {
        source.sendSystemMessage(component);
    }

    public boolean isOnline() {
        return !player.hasDisconnected();
    }

    public int getTimeout() {
        return timeout;
    }

    public Component getName() {
        return source.getDisplayName();
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        return ((Reporter) obj)
                .player
                .equals(player);
    }
}
