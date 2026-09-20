package com.github.Syaaddd.progresstree.data;

import com.github.Syaaddd.progresstree.ProgressTree;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fix #2: Async player data loading with "loading" state.
 * Instead of blocking main thread with .join(), we track loading state
 * and queue actions that need data to be ready.
 */
public class ProgressRepository {

    private final ProgressTree plugin;
    private final Map<UUID, PlayerData> cache;
    private final Set<UUID> loadingPlayers;

    public ProgressRepository(ProgressTree plugin) {
        this.plugin = plugin;
        this.cache = new ConcurrentHashMap<>();
        this.loadingPlayers = ConcurrentHashMap.newKeySet();
    }

    /**
     * Load player data asynchronously. When complete, fires ProgressUpdateEvent
     * so any pending milestone checks run through the unified path.
     */
    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) return;
        if (loadingPlayers.contains(uuid)) return;

        loadingPlayers.add(uuid);
        plugin.getLog().debug("Loading data async for " + player.getName());

        plugin.getDatabaseManager().loadPlayerData(uuid).thenAccept(data -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                cache.put(uuid, data);
                loadingPlayers.remove(uuid);
                plugin.getLog().debug("Data loaded for " + player.getName());

                // Fire a synthetic progress update so milestone check runs through unified path
                var event = new com.github.Syaaddd.progresstree.event.ProgressUpdateEvent(
                    player,
                    com.github.Syaaddd.progresstree.milestone.MilestoneType.PLAYTIME,
                    0
                );
                Bukkit.getPluginManager().callEvent(event);
            });
        }).exceptionally(ex -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                loadingPlayers.remove(uuid);
                plugin.getLog().severe("Failed to load data for " + player.getName() + ": " + ex.getMessage());
            });
            return null;
        });
    }

    public boolean isLoading(UUID uuid) {
        return loadingPlayers.contains(uuid);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerData getOrCreatePlayerData(UUID uuid) {
        return cache.computeIfAbsent(uuid, k -> new PlayerData(uuid));
    }

    public void savePlayer(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            plugin.getDatabaseManager().savePlayerData(entry.getValue());
        }
    }

    public void claimMilestone(UUID uuid, String milestoneId, String choiceId) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            data.claimMilestone(milestoneId, choiceId);
            plugin.getDatabaseManager().saveClaimedMilestone(uuid, milestoneId, choiceId);
        }
    }

    /** Fix #4: persist notification flag */
    public void markNotified(UUID uuid, String milestoneId) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            data.markNotified(milestoneId);
            plugin.getDatabaseManager().saveNotifiedMilestone(uuid, milestoneId);
        }
    }

    public int getTotalCommunityPlaytime() {
        return plugin.getDatabaseManager().getTotalPlaytime();
    }
}
