package com.github.Syaaddd.progresstree.util;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.milestone.MilestoneChoice;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RewardExecutor {

    private final ProgressTree plugin;

    public RewardExecutor(ProgressTree plugin) {
        this.plugin = plugin;
    }

    public void executeReward(Player player, MilestoneChoice choice) {
        String rawCommand = choice.formatCommand(player.getName());
        final String command = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;
        final String playerName = player.getName();
        final String rewardName = choice.getName();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            plugin.getLog().debug("Executed reward command: " + command + " for " + playerName);
        });

        String msg = plugin.getConfigManager().getMsgMilestoneClaimed()
            .replace("%reward%", rewardName);
        player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() + msg));

        if (plugin.getConfigManager().isCommunityRewardBroadcast()) {
            Bukkit.broadcastMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                "&7" + playerName + " " + msg));
        }
    }
}
