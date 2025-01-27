package com.github.argon4w.features.holders;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ReportMessageReceiver {

    private final CommandSourceStack source;
    private final ServerPlayer player;
    private final int timeout;

    public ReportMessageReceiver(
            CommandSourceStack source,
            ServerPlayer player,
            int timeout
    ) {
        this.source = source;
        this.player = player;
        this.timeout = timeout;
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
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        return ((ReportMessageReceiver) obj)
                .player
                .equals(player);
    }
}
