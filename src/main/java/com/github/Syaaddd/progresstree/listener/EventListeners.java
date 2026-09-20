package com.github.Syaaddd.progresstree.listener;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.data.PlayerData;
import com.github.Syaaddd.progresstree.event.ProgressUpdateEvent;
import com.github.Syaaddd.progresstree.milestone.MilestoneType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListeners implements Listener {

    private final ProgressTree plugin;

    public EventListeners(ProgressTree plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getRepository().loadPlayer(player);
        // Note: milestone check happens via ProgressUpdateEvent fired after async load completes
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getRepository().savePlayer(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
        
        if (data != null) {
            data.addBlockBreak(1);
            Bukkit.getPluginManager().callEvent(new ProgressUpdateEvent(player, MilestoneType.BLOCK_BREAK, 1));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
        
        if (data != null) {
            data.addBlockPlace(1);
            Bukkit.getPluginManager().callEvent(new ProgressUpdateEvent(player, MilestoneType.BLOCK_PLACE, 1));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        
        Player player = event.getEntity().getKiller();
        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
        
        if (data != null) {
            if (event.getEntity() instanceof org.bukkit.entity.Player) {
                data.addPlayerKill(1);
                Bukkit.getPluginManager().callEvent(new ProgressUpdateEvent(player, MilestoneType.PLAYER_KILL, 1));
            } else {
                data.addMobKill(1);
                Bukkit.getPluginManager().callEvent(new ProgressUpdateEvent(player, MilestoneType.MOB_KILL, 1));
            }
        }
    }
}
