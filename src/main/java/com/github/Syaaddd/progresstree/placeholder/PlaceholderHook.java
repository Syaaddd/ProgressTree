package com.github.Syaaddd.progresstree.placeholder;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.data.PlayerData;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.milestone.MilestoneManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook extends PlaceholderExpansion {

    private final ProgressTree plugin;

    public PlaceholderHook(ProgressTree plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "progresstree";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Syaaddd";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return "";
        }

        PlayerData data = plugin.getRepository().getPlayerData(offlinePlayer.getUniqueId());
        MilestoneManager manager = plugin.getMilestoneManager();

        return switch (params.toLowerCase()) {
            case "current" -> {
                Milestone milestone = manager.getCurrentProgressMilestone(offlinePlayer.getPlayer());
                yield milestone != null ? milestone.getId() : "None";
            }
            case "next" -> {
                Milestone next = manager.getNextMilestone(offlinePlayer.getPlayer());
                yield next != null ? next.getId() : "None";
            }
            case "progress" -> {
                Milestone current = manager.getCurrentProgressMilestone(offlinePlayer.getPlayer());
                double percent = manager.getProgressPercentage(offlinePlayer.getPlayer(), current);
                yield String.format("%.1f", percent);
            }
            case "playtime" -> {
                if (data != null) {
                    int seconds = data.getPlaytimeSeconds();
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    yield hours + "h " + minutes + "m";
                }
                yield "0h 0m";
            }
            case "blocks_broken" -> data != null ? String.valueOf(data.getBlocksBroken()) : "0";
            case "blocks_placed" -> data != null ? String.valueOf(data.getBlocksPlaced()) : "0";
            case "mobs_killed" -> data != null ? String.valueOf(data.getMobsKilled()) : "0";
            case "players_killed" -> data != null ? String.valueOf(data.getPlayersKilled()) : "0";
            case "join_days" -> data != null ? String.valueOf(data.getJoinDays()) : "0";
            case "community_playtime" -> {
                int total = plugin.getRepository().getTotalCommunityPlaytime();
                int hours = total / 3600;
                yield hours + " jam";
            }
            case "can_claim" -> manager.canClaimAny(offlinePlayer.getUniqueId()) ? "Yes" : "No";
            default -> "";
        };
    }
}
