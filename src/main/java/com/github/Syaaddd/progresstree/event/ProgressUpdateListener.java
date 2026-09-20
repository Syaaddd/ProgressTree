package com.github.Syaaddd.progresstree.event;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.data.PlayerData;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.milestone.MilestoneManager;
import com.github.Syaaddd.progresstree.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * Single listener that handles ALL progress notifications.
 * Every ProgressUpdateEvent (regardless of source: block break, mob kill, playtime tick, join)
 * goes through here — guaranteeing uniform behavior.
 * 
 * Fix #1: Unified trigger path (all milestone types use same completion check).
 * Fix #4: Idempotent notification via notifiedMilestones set in PlayerData.
 */
public class ProgressUpdateListener implements Listener {

    private final ProgressTree plugin;

    public ProgressUpdateListener(ProgressTree plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProgressUpdate(ProgressUpdateEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
        if (data == null) return;

        MilestoneManager manager = plugin.getMilestoneManager();
        List<Milestone> milestones = plugin.getConfigManager().getMilestonesInOrder();

        for (Milestone milestone : milestones) {
            if (data.hasClaimed(milestone.getId())) continue;
            if (data.isNotified(milestone.getId())) continue; // Fix #4: idempotent

            if (manager.hasReached(data, milestone)) {
                // Mark as notified (in-memory + persisted) BEFORE sending to prevent race/spam
                plugin.getRepository().markNotified(data.getUuid(), milestone.getId());
                String msg = plugin.getConfigManager().getMsgMilestoneAvailable();
                player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() + msg));
                plugin.getLog().debug("Notified " + player.getName() + " about milestone: " + milestone.getId());
            }
        }
    }
}
