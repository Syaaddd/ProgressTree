package com.github.Syaaddd.progresstree.gui;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.config.ConfigManager;
import com.github.Syaaddd.progresstree.data.PlayerData;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.milestone.MilestoneManager;
import com.github.Syaaddd.progresstree.milestone.MilestoneType;
import com.github.Syaaddd.progresstree.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ProgressTreeGUI {

    private final ProgressTree plugin;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public ProgressTreeGUI(ProgressTree plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        ConfigManager cfg = plugin.getConfigManager();
        int[] template = cfg.getLayoutTemplate();
        List<Milestone> allMilestones = cfg.getMilestonesInOrder();

        int perPage = template.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) allMilestones.size() / perPage));
        page = Math.max(0, Math.min(page, totalPages - 1));
        playerPages.put(player.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, 54, cfg.getGuiTitle());

        // Fill entire GUI with consistent branch filler
        fillBranchFiller(inv, cfg);

        // Place milestone nodes for this page
        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
        MilestoneManager manager = plugin.getMilestoneManager();
        int startIdx = page * perPage;

        for (int i = 0; i < template.length; i++) {
            int msIndex = startIdx + i;
            if (msIndex >= allMilestones.size()) break;

            Milestone milestone = allMilestones.get(msIndex);
            boolean claimed = data != null && data.hasClaimed(milestone.getId());
            boolean available = data != null && manager.hasReached(data, milestone);

            ItemStack item = createMilestoneItem(milestone, claimed, available, data, cfg);
            inv.setItem(template[i], item);
        }

        // Navigation bar (bottom row, reserved exclusively)
        placeNavBar(inv, player, page, totalPages, cfg);

        player.openInventory(inv);
    }

    private void fillBranchFiller(Inventory inv, ConfigManager cfg) {
        Material mat;
        try {
            mat = Material.valueOf(cfg.getBranchFillerMaterial().toUpperCase());
        } catch (IllegalArgumentException e) {
            mat = Material.GRAY_STAINED_GLASS_PANE;
        }
        ItemStack filler = new ItemStack(mat);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(MessageUtil.color(cfg.getBranchFillerName()));
        filler.setItemMeta(meta);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, filler);
        }
    }

    private void placeNavBar(Inventory inv, Player player, int currentPage, int totalPages, ConfigManager cfg) {
        // Previous page button
        ItemStack prevItem;
        if (currentPage > 0) {
            prevItem = new ItemStack(Material.ARROW);
            ItemMeta pm = prevItem.getItemMeta();
            pm.setDisplayName(MessageUtil.color("&a&l◀ Previous Page"));
            prevItem.setItemMeta(pm);
        } else {
            prevItem = new ItemStack(Material.BARRIER);
            ItemMeta pm = prevItem.getItemMeta();
            pm.setDisplayName(MessageUtil.color("&7&l◀ First Page"));
            prevItem.setItemMeta(pm);
        }
        inv.setItem(cfg.getPrevPageSlot(), prevItem);

        // Player info
        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta im = infoItem.getItemMeta();
        im.setDisplayName(MessageUtil.color("&b&l" + player.getName()));
        List<String> infoLore = new ArrayList<>();
        infoLore.add(MessageUtil.color("&7Playtime: &f" + formatTime(data != null ? data.getPlaytimeSeconds() : 0)));
        infoLore.add(MessageUtil.color("&7Blocks Broken: &f" + (data != null ? data.getBlocksBroken() : 0)));
        infoLore.add(MessageUtil.color("&7Blocks Placed: &f" + (data != null ? data.getBlocksPlaced() : 0)));
        infoLore.add(MessageUtil.color("&7Mobs Killed: &f" + (data != null ? data.getMobsKilled() : 0)));
        infoLore.add(MessageUtil.color("&7PvP Kills: &f" + (data != null ? data.getPlayersKilled() : 0)));
        im.setLore(infoLore);
        infoItem.setItemMeta(im);
        inv.setItem(cfg.getPlayerInfoSlot(), infoItem);

        // Page indicator
        ItemStack pageItem = new ItemStack(Material.BOOK);
        ItemMeta pgm = pageItem.getItemMeta();
        pgm.setDisplayName(MessageUtil.color("&e&lPage " + (currentPage + 1) + " / " + totalPages));
        pageItem.setItemMeta(pgm);
        inv.setItem(cfg.getPageIndicatorSlot(), pageItem);

        // Close button
        ItemStack closeItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cm = closeItem.getItemMeta();
        cm.setDisplayName(MessageUtil.color("&c&l✕ Close"));
        closeItem.setItemMeta(cm);
        inv.setItem(cfg.getCloseSlot(), closeItem);

        // Next page button
        ItemStack nextItem;
        if (currentPage < totalPages - 1) {
            nextItem = new ItemStack(Material.ARROW);
            ItemMeta nm = nextItem.getItemMeta();
            nm.setDisplayName(MessageUtil.color("&a&lNext Page ▶"));
            nextItem.setItemMeta(nm);
        } else {
            nextItem = new ItemStack(Material.BARRIER);
            ItemMeta nm = nextItem.getItemMeta();
            nm.setDisplayName(MessageUtil.color("&7&lLast Page ▶"));
            nextItem.setItemMeta(nm);
        }
        inv.setItem(cfg.getNextPageSlot(), nextItem);
    }

    public void handleClick(Player player, int slot) {
        ConfigManager cfg = plugin.getConfigManager();
        UUID uuid = player.getUniqueId();
        int currentPage = playerPages.getOrDefault(uuid, 0);
        int[] template = cfg.getLayoutTemplate();
        List<Milestone> allMilestones = cfg.getMilestonesInOrder();
        int perPage = template.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) allMilestones.size() / perPage));

        // Navigation clicks
        if (slot == cfg.getPrevPageSlot()) {
            if (currentPage > 0) {
                open(player, currentPage - 1);
            }
            return;
        }
        if (slot == cfg.getNextPageSlot()) {
            if (currentPage < totalPages - 1) {
                open(player, currentPage + 1);
            }
            return;
        }
        if (slot == cfg.getCloseSlot()) {
            player.closeInventory();
            return;
        }
        // Info and page indicator slots — no action
        if (slot == cfg.getPlayerInfoSlot() || slot == cfg.getPageIndicatorSlot()) {
            return;
        }

        // Milestone node clicks
        int startIdx = currentPage * perPage;
        for (int i = 0; i < template.length; i++) {
            if (template[i] == slot) {
                int msIndex = startIdx + i;
                if (msIndex >= allMilestones.size()) return;

                Milestone milestone = allMilestones.get(msIndex);
                PlayerData data = plugin.getRepository().getPlayerData(uuid);
                MilestoneManager manager = plugin.getMilestoneManager();
                boolean claimed = data != null && data.hasClaimed(milestone.getId());
                boolean available = data != null && manager.hasReached(data, milestone);

                if (available && !claimed) {
                    if (milestone.hasChoices()) {
                        ChoiceGUI choiceGUI = new ChoiceGUI(plugin);
                        choiceGUI.open(player, milestone.getId());
                    } else {
                        manager.claimMilestone(player, milestone.getId(), null);
                        // Re-open same page after claim (preserve pagination)
                        open(player, currentPage);
                    }
                }
                return;
            }
        }
    }

    public int getCurrentPage(UUID uuid) {
        return playerPages.getOrDefault(uuid, 0);
    }

    private ItemStack createMilestoneItem(Milestone milestone, boolean claimed, boolean available, PlayerData data, ConfigManager cfg) {
        ItemStack item = getItemForState(milestone, claimed, available);
        ItemMeta meta = item.getItemMeta();

        String customColor = milestone.getDisplayColor();
        String typeStr = milestone.getType().name().replace("_", " ");
        String amountStr = formatAmount(milestone.getAmount(), milestone.getType());
        String currentStr = getCurrentProgress(data, milestone);
        double progress = getProgress(data, milestone);

        meta.setDisplayName(MessageUtil.color(customColor + "&l" + milestone.getId()));

        List<String> lore = new ArrayList<>();
        lore.add(MessageUtil.color("&7------------------------"));
        lore.add(MessageUtil.color("&7Type: &f" + typeStr));
        lore.add(MessageUtil.color("&7Progress: &f" + currentStr + " &7/ &f" + amountStr));
        lore.add(createProgressBarComponent(progress, cfg, currentStr, amountStr));
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
            lore.add(MessageUtil.color(cfg.getAvailableColor() + cfg.getClaimButton()));
            if (milestone.hasChoices()) {
                lore.add(MessageUtil.color("&e" + cfg.getChooseButton()));
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

    /**
     * State-based icon selection:
     * - Claimed: EMERALD with enchant glow (distinct from available)
     * - Available: NETHER_STAR with enchant glow (bright, active) or custom icon from config + glow
     * - Locked: BARRIER (clearly blocked, not clickable)
     */
    private ItemStack getItemForState(Milestone milestone, boolean claimed, boolean available) {
        if (claimed) {
            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            return item;
        }

        if (available) {
            String customIcon = milestone.getIcon();
            Material mat = Material.NETHER_STAR;
            if (!customIcon.isEmpty()) {
                try {
                    mat = Material.valueOf(customIcon.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    // Fall through to default NETHER_STAR
                }
            }
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            return item;
        }

        // Locked state — always BARRIER
        return new ItemStack(Material.BARRIER);
    }

    /**
     * RGB gradient progress bar using Adventure Component.
     * 20 segments, ▰/▱ chars, color interpolated green→yellow→red based on percentage.
     * Format: "▰▰▰▰▱▱▱▱▱▱▱▱▱▱▱▱▱▱ 1.234 / 5.000 — 24%"
     */
    private String createProgressBarComponent(double percentage, ConfigManager cfg, String currentStr, String amountStr) {
        int segments = cfg.getProgressBarSegments();
        int filled = (int) (percentage / 100.0 * segments);

        StringBuilder bar = new StringBuilder();

        // Filled segments with gradient color
        TextColor fillColor = getGradientColor(percentage);
        String fillHex = String.format("#%02X%02X%02X", fillColor.red(), fillColor.green(), fillColor.blue());
        bar.append("&").append(fillHex);
        for (int i = 0; i < filled; i++) {
            bar.append(cfg.getProgressFilledChar());
        }

        // Empty segments
        bar.append(cfg.getProgressEmptyColor());
        for (int i = filled; i < segments; i++) {
            bar.append(cfg.getProgressEmptyChar());
        }

        // Raw numbers + percentage
        bar.append(" &f").append(currentStr).append(" &7/ &f").append(amountStr);
        bar.append(" &7— &f").append(String.format("%.0f", percentage)).append("%");

        return MessageUtil.color(bar.toString());
    }

    /**
     * Interpolate color: 0% = red, 50% = yellow, 100% = green
     */
    private TextColor getGradientColor(double percentage) {
        float p = (float) Math.max(0, Math.min(100, percentage));
        int r, g;
        if (p <= 50f) {
            // Red → Yellow (0-50%)
            r = 255;
            g = (int) (255 * (p / 50f));
        } else {
            // Yellow → Green (50-100%)
            r = (int) (255 * ((100f - p) / 50f));
            g = 255;
        }
        return TextColor.color(r, g, 0);
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

</content>