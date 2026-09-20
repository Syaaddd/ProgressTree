package com.github.Syaaddd.progresstree.util;

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
}
