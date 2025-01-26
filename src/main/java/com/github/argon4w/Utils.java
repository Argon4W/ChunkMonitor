package com.github.argon4w;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;

public final class Utils {

    private Utils() {

    }

    public static Component getTeleportComponent(BlockPos blockPos) {
        return ComponentUtils.wrapInSquareBrackets(Component.translatable(
                "chat.coordinates",
                blockPos.getX(),
                "~",
                blockPos.getZ())
        ).withStyle(style -> style.withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos.getX() + " " + "~" + " " + blockPos.getZ()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
        );
    }
}
