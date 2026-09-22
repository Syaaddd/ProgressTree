package com.github.Syaaddd.progresstree.command;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.data.MigrationService;
import com.github.Syaaddd.progresstree.gui.ChoiceGUI;
import com.github.Syaaddd.progresstree.gui.CategoryHubGUI;
import com.github.Syaaddd.progresstree.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProgressTreeCommand implements CommandExecutor {

    private final ProgressTree plugin;
    private final CategoryHubGUI hubGui;
    private final ChoiceGUI choiceGUI;

    public ProgressTreeCommand(ProgressTree plugin) {
        this.plugin = plugin;
        this.hubGui = new CategoryHubGUI(plugin);
        this.choiceGUI = new ChoiceGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.color("&cOnly players can use this command."));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("open")) {
            if (!player.hasPermission("progresstree.open")) {
                player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMsgNoPermission()));
                return true;
            }
            hubGui.open(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(player);

            case "claim" -> {
                if (!player.hasPermission("progresstree.claim")) {
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMsgNoPermission()));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        "&cUsage: /progresstree claim <milestone_id>"));
                    return true;
                }
                plugin.getMilestoneManager().claimMilestone(player, args[1], null);
            }

            case "reload" -> {
                if (!player.hasPermission("progresstree.admin")) {
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMsgNoPermission()));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getConfigManager().load();
                plugin.getCategoryRegistry().load();
                player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMsgConfigReloaded()));
            }

            case "migrate" -> {
                if (!player.hasPermission("progresstree.admin")) {
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMsgNoPermission()));
                    return true;
                }
                MigrationService migrationService = new MigrationService(plugin);
                if (!migrationService.needsMigration()) {
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        "&7No legacy MilestoneMP data found to migrate."));
                    return true;
                }
                player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                    "&7Starting migration..."));
                boolean success = migrationService.migrate();
                if (success) {
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMsgMigrationSuccess()));
                } else {
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMsgMigrationFailed()));
                }
            }

            case "check" -> {
                if (!player.hasPermission("progresstree.check")) {
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMsgNoPermission()));
                    return true;
                }
                var milestone = plugin.getMilestoneManager().getCurrentProgressMilestone(player);
                if (milestone != null) {
                    double percent = plugin.getMilestoneManager().getProgressPercentage(player, milestone);
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        "&7Progress: &a" + String.format("%.1f", percent) + "% &7(" + milestone.getId() + ")"));
                } else {
                    player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().getMsgNoMilestone()));
                }
            }

            default -> {
                player.sendMessage(MessageUtil.color(plugin.getConfigManager().getPrefix() +
                    "&cUsage: /progresstree [open|claim|check|help|reload|migrate]"));
            }
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(MessageUtil.color("&8&m----------------------------------------"));
        player.sendMessage(MessageUtil.color("&b&lProgressTree &7- Help"));
        player.sendMessage(MessageUtil.color("&8&m----------------------------------------"));
        player.sendMessage(MessageUtil.color("&e/progresstree &7- &fOpen category hub"));
        player.sendMessage(MessageUtil.color("&e/progresstree open &7- &fOpen category hub"));
        player.sendMessage(MessageUtil.color("&e/progresstree check &7- &fCheck your progress"));
        player.sendMessage(MessageUtil.color("&e/progresstree claim <id> &7- &fClaim specific milestone"));
        player.sendMessage(MessageUtil.color("&e/progresstree help &7- &fShow this help menu"));

        if (player.hasPermission("progresstree.admin")) {
            player.sendMessage(MessageUtil.color("&e/progresstree reload &7- &fReload configuration"));
            player.sendMessage(MessageUtil.color("&e/progresstree migrate &7- &fMigrate from MilestoneMP"));
        }

        player.sendMessage(MessageUtil.color("&8&m----------------------------------------"));
    }

    public ChoiceGUI getChoiceGUI() {
        return choiceGUI;
    }
}