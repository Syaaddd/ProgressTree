package com.github.Syaaddd.progresstree.listener;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.data.PlayerData;
import com.github.Syaaddd.progresstree.event.ProgressUpdateEvent;
import com.github.Syaaddd.progresstree.milestone.MilestoneType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;

public class PlaytimeTracker extends BukkitRunnable {

    private final ProgressTree plugin;

    public PlaytimeTracker(ProgressTree plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int interval = plugin.getConfigManager().getCheckInterval();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                Collection<Player> players = (Collection<Player>) plugin.getServer().getOnlinePlayers();
                
                for (Player player : players) {
                    UUID uuid = player.getUniqueId();
                    PlayerData data = plugin.getRepository().getPlayerData(uuid);

                    if (data != null) {
                        data.addPlaytime(interval);

                        long now = System.currentTimeMillis();
                        long lastJoin = data.getLastJoinTime();
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(lastJoin);
                        int lastDay = cal.get(Calendar.DAY_OF_YEAR);
                        
                        cal.setTimeInMillis(now);
                        int currentDay = cal.get(Calendar.DAY_OF_YEAR);
                        
                        if (currentDay > lastDay) {
                            data.setJoinDays(data.getJoinDays() + 1);
                            Bukkit.getPluginManager().callEvent(new ProgressUpdateEvent(player, MilestoneType.JOIN, 1));
                        }
                        data.setLastJoinTime(now);
                        
                        // Fire playtime update event
                        Bukkit.getPluginManager().callEvent(new ProgressUpdateEvent(player, MilestoneType.PLAYTIME, interval));
                    }
                }
            }
        }.runTask(plugin);
    }

    public void start() {
        int interval = plugin.getConfigManager().getCheckInterval() * 20;
        this.runTaskTimerAsynchronously(plugin, interval, interval);
    }
}
