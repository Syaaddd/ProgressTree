package com.github.Syaaddd.progresstree.gui;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.data.PlayerData;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.milestone.MilestoneManager;
import com.github.Syaaddd.progresstree.milestone.MilestoneType;
import com.github.Syaaddd.progresstree.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ProgressTreeGUI {

    private final ProgressTree plugin;

    public ProgressTreeGUI(ProgressTree plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, 
            plugin.getConfigManager().getGuiTitle());

        fillEmptySlots(inv);
        
        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
        List<Milestone> milestones = plugin.getConfigManager().getMilestonesInOrder();
        MilestoneManager manager = plugin.getMilestoneManager();

        int[] slots = plugin.getConfigManager().getMilestoneSlots();
        int slotIndex = 0;
        
        for (Milestone milestone : milestones) {
            if (slotIndex >= slots.length) break;
            
            int slot = slots[slotIndex];
            boolean claimed = data != null && data.hasClaimed(milestone.getId());
            boolean available = data != null && manager.hasReached(data, milestone);

            ItemStack item = createMilestoneItem(milestone, claimed, available, data);
            inv.setItem(slot, item);

            slotIndex++;
        }

        player.openInventory(inv);
    }

    private void fillEmptySlots(Inventory inv) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }
    }

    public void handleClick(Player player, int slot) {
        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
        List<Milestone> milestones = plugin.getConfigManager().getMilestonesInOrder();
        MilestoneManager manager = plugin.getMilestoneManager();
        int[] slots = plugin.getConfigManager().getMilestoneSlots();

        for (int i = 0; i < milestones.size() && i < slots.length; i++) {
            if (slots[i] == slot) {
                Milestone milestone = milestones.get(i);
                boolean claimed = data != null && data.hasClaimed(milestone.getId());
                boolean available = data != null && manager.hasReached(data, milestone);

                if (available && !claimed) {
                    if (milestone.hasChoices()) {
                        ChoiceGUI choiceGUI = new ChoiceGUI(plugin);
                        choiceGUI.open(player, milestone.getId());
                    } else {
                        plugin.getMilestoneManager().claimMilestone(player, milestone.getId(), null);
                        player.closeInventory();
                    }
                }
                break;
            }
        }
    }

    private ItemStack createMilestoneItem(Milestone milestone, boolean claimed, boolean available, PlayerData data) {
        ItemStack item = getItemForType(milestone, claimed, available);
        
        ItemMeta meta = item.getItemMeta();
        
        String customColor = milestone.getDisplayColor();
        String statusColor = claimed ? plugin.getConfigManager().getClaimedColor() :
                           (available ? plugin.getConfigManager().getAvailableColor() :
                            plugin.getConfigManager().getLockedColor());

        String typeStr = milestone.getType().name().replace("_", " ");
        String amountStr = formatAmount(milestone.getAmount(), milestone.getType());
        String currentStr = getCurrentProgress(data, milestone);
        double progress = getProgress(data, milestone);

        meta.setDisplayName(MessageUtil.color(customColor + "&l" + milestone.getId()));
        
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtil.color("&7------------------------"));
        lore.add(MessageUtil.color("&7Type: &f" + typeStr));
        lore.add(MessageUtil.color("&7Progress: &f" + currentStr + " &7/ &f" + amountStr));
        lore.add(MessageUtil.color(createProgressBar(progress)));
        lore.add(MessageUtil.color("&7------------------------"));
        
        if (claimed) {
            lore.add(MessageUtil.color("&a&l✓ CLAIMED"));
            String choiceId = data.getClaimedChoice(milestone.getId());
            if (choiceId != null) {
                final String finalChoiceId = choiceId;
                var choice = milestone.getChoices().stream()
                    .filter(c -> c.getId().equals(finalChoiceId))
                    .findFirst()
                    .orElse(null);
                if (choice != null) {
                    lore.add(MessageUtil.color("&7Reward: &b" + choice.getName()));
                }
            }
        } else if (available) {
            lore.add(MessageUtil.color(statusColor + plugin.getConfigManager().getClaimButton()));
            if (milestone.hasChoices()) {
                lore.add(MessageUtil.color("&e" + plugin.getConfigManager().getChooseButton()));
            }
        } else {
            lore.add(MessageUtil.color("&c🔒 Locked"));
        }
        
        lore.add(MessageUtil.color("&7------------------------"));
        lore.add(MessageUtil.color("&8Click to " + (available ? "claim" : "view")));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getItemForType(Milestone milestone, boolean claimed, boolean available) {
        String customIcon = milestone.getIcon();
        
        if (claimed) {
            return new ItemStack(Material.GOLD_BLOCK);
        }
        
        if (!customIcon.isEmpty()) {
            try {
                Material customMat = Material.valueOf(customIcon.toUpperCase());
                return new ItemStack(customMat);
            } catch (IllegalArgumentException e) {
                // Invalid material, use default
            }
        }
        
        MilestoneType type = milestone.getType();
        Material material;
        
        switch (type) {
            case PLAYTIME -> material = available ? Material.CLOCK : Material.GRAY_STAINED_GLASS_PANE;
            case BLOCK_BREAK -> material = available ? Material.DIAMOND_PICKAXE : Material.COBBLESTONE;
            case BLOCK_PLACE -> material = available ? Material.BRICK : Material.COBBLESTONE;
            case MOB_KILL -> material = available ? Material.ZOMBIE_HEAD : Material.RED_STAINED_GLASS_PANE;
            case PLAYER_KILL -> material = available ? Material.IRON_SWORD : Material.RED_STAINED_GLASS_PANE;
            case JOIN -> material = available ? Material.PAPER : Material.WHITE_STAINED_GLASS_PANE;
            case COMMUNITY_PLAYTIME -> material = available ? Material.NETHER_STAR : Material.PURPLE_STAINED_GLASS_PANE;
            default -> material = Material.GRAY_STAINED_GLASS_PANE;
        }
        
        return new ItemStack(material);
    }

    private String createProgressBar(double percentage) {
        int totalBars = 10;
        int filledBars = (int) (percentage / 100 * totalBars);
        StringBuilder bar = new StringBuilder("&b[");
        
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append("\u2588");
            } else {
                bar.append("\u2591");
            }
        }
        
        bar.append("] &b").append(String.format("%.0f", percentage)).append("%");
        return bar.toString();
    }

    private String getCurrentProgress(PlayerData data, Milestone milestone) {
        if (data == null) return "0";
        
        return switch (milestone.getType()) {
            case PLAYTIME -> formatTime(data.getPlaytimeSeconds());
            case BLOCK_BREAK -> String.valueOf(data.getBlocksBroken());
            case BLOCK_PLACE -> String.valueOf(data.getBlocksPlaced());
            case MOB_KILL -> String.valueOf(data.getMobsKilled());
            case PLAYER_KILL -> String.valueOf(data.getPlayersKilled());
            case JOIN -> String.valueOf(data.getJoinDays());
            case COMMUNITY_PLAYTIME -> formatTime(plugin.getRepository().getTotalCommunityPlaytime());
            default -> "0";
        };
    }

    private double getProgress(PlayerData data, Milestone milestone) {
        if (data == null) return 0;
        
        int current = switch (milestone.getType()) {
            case PLAYTIME -> data.getPlaytimeSeconds();
            case BLOCK_BREAK -> data.getBlocksBroken();
            case BLOCK_PLACE -> data.getBlocksPlaced();
            case MOB_KILL -> data.getMobsKilled();
            case PLAYER_KILL -> data.getPlayersKilled();
            case JOIN -> data.getJoinDays();
            case COMMUNITY_PLAYTIME -> plugin.getRepository().getTotalCommunityPlaytime();
            default -> 0;
        };
        
        int required = milestone.getAmount();
        return Math.min(100.0, (double) current / required * 100);
    }

    private String formatAmount(int amount, MilestoneType type) {
        return switch (type) {
            case PLAYTIME -> formatTime(amount);
            default -> String.valueOf(amount);
        };
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        if (hours > 0) {
            return hours + "h" + (minutes > 0 ? " " + minutes + "m" : "");
        }
        return minutes + "m";
    }
}
