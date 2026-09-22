package com.github.Syaaddd.progresstree.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class MessageUtil {

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String[] color(String[] messages) {
        String[] colored = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            colored[i] = color(messages[i]);
        }
        return colored;
    }

    /** Strip legacy color codes from a string. */
    public static String strip(String message) {
        return message == null ? "" : ChatColor.stripColor(message);
    }

    /** Convert a legacy (&/§ coded) string to an Adventure Component for inventory titles. */
    public static Component toComponent(String legacy) {
        return LegacyComponentSerializer.legacySection().deserialize(color(legacy));
    }
}
