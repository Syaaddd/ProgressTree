package com.github.Syaaddd.progresstree.gui;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.category.Category;
import com.github.Syaaddd.progresstree.category.CategoryRegistry;
import com.github.Syaaddd.progresstree.category.CategorySummary;
import com.github.Syaaddd.progresstree.data.PlayerData;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Hub GUI displaying all categories with claim badges and progress summaries.
 * Layout per PRD 5.1:
 *   Row 0: filler border
 *   Row 1: category icons (slots 10-16)
 *   Row 2: filler border
 *   Row 3: Stats (slot 31), Close (slot 35)
 */
public class CategoryHubGUI {

    private final ProgressTree plugin;

    public CategoryHubGUI(ProgressTree plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        CategoryRegistry registry = plugin.getCategoryRegistry();
        List<Category> cats = registry.getSortedCategories();
        boolean hideEmpty = plugin.getConfig().getBoolean("gui.hub.hide-empty", true);

        List<Category> visible = new ArrayList<>();
        for (Category cat : cats) {
            if (!hideEmpty || registry.hasMilestones(cat.getId())) {
                visible.add(cat);
            }
        }

        String title = MessageUtil.color(plugin.getConfig().getString("gui.hub.title", "&8ProgressTree"));
        int rows = 4; // Fixed 4 rows per PRD 5.1
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        // Fill entire inventory with filler first
        Material fillerMat;
        try {
            fillerMat = Material.valueOf(plugin.getConfig()
                    .getString("gui.hub.filler", "BLACK_STAINED_GLASS_PANE").toUpperCase());
        } catch (IllegalArgumentException e) {
            fillerMat = Material.BLACK_STAINED_GLASS_PANE;
        }
        ItemStack filler = new ItemStack(fillerMat);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // Category slots: row 1, columns 1-7 (slots 10-16)
        int[] slots = {10, 11, 12, 13, 14, 15, 16};

        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());

        for (int i = 0; i < visible.size() && i < slots.length; i++) {
            Category cat = visible.get(i);
            List<Milestone> catMilestones = registry.getMilestonesForCategory(cat.getId());
            CategorySummary summary = CategorySummary.compute(plugin, cat.getId(), catMilestones, data, plugin.getMilestoneManager());
            ItemStack item = createCategoryItem(cat, summary);
            inv.setItem(slots[i], item);
        }

        // Stats item: row 3, center (slot 31)
        inv.setItem(31, createStatsItem(player, data, registry));

        // Close button: row 3, right side (slot 35)
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(MessageUtil.color("&c&l✕ Close"));
        close.setItemMeta(cm);
        inv.setItem(35, close);

        player.openInventory(inv);
    }

    /**
     * Handle click in hub inventory. Returns true if handled.
     */
    public boolean handleClick(Player player, int slot) {
        CategoryRegistry registry = plugin.getCategoryRegistry();
        List<Category> cats = registry.getSortedCategories();
        boolean hideEmpty = plugin.getConfig().getBoolean("gui.hub.hide-empty", true);

        List<Category> visible = new ArrayList<>();
        for (Category cat : cats) {
            if (!hideEmpty || registry.hasMilestones(cat.getId())) {
                visible.add(cat);
            }
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16};

        // Check if clicked slot matches a category slot
        for (int i = 0; i < visible.size() && i < slots.length; i++) {
            if (slots[i] == slot) {
                Category cat = visible.get(i);
                ProgressTreeGUI treeGui = new ProgressTreeGUI(plugin);
                treeGui.open(player, cat.getId(), 1);
                return true;
            }
        }

        // Close button at slot 35
        if (slot == 35) {
            player.closeInventory();
            return true;
        }

        // All other slots: cancel interaction (prevent taking items)
        return false;
    }

    private ItemStack createCategoryItem(Category cat, CategorySummary summary) {
        ItemStack item = new ItemStack(cat.getIcon());
        ItemMeta meta = item.getItemMeta();

        String color = cat.getColor();
        meta.setDisplayName(MessageUtil.color(color + "&l" + cat.getName()));

        // Glow if claimable
        if (summary.getClaimableCount() > 0) {
            meta.setEnchantmentGlintOverride(true);
        }

        List<Component> lore = new ArrayList<>();

        // Description lines
        for (String desc : cat.getDescription()) {
            lore.add(Component.text(ChatColor.stripColor(desc)).color(NamedTextColor.GRAY));
        }
        if (!cat.getDescription().isEmpty()) {
            lore.add(Component.empty());
        }

        // Claim summary
        lore.add(Component.text("Diklaim: ").color(NamedTextColor.GRAY)
                .append(Component.text(summary.getClaimedCount() + " / " + summary.getTotalMilestones()).color(NamedTextColor.WHITE)));

        // Next milestone progress
        if (summary.getNextMilestone() != null) {
            lore.add(Component.text("Next: ").color(NamedTextColor.GRAY)
                    .append(Component.text(summary.getNextMilestone().getId()).color(NamedTextColor.WHITE))
                    .append(Component.text(" — ").color(NamedTextColor.GRAY))
                    .append(Component.text(String.format("%.0f%%", summary.getNextProgress())).color(NamedTextColor.YELLOW)));
        }

        lore.add(Component.empty());

        // Status badge
        if (summary.getClaimableCount() > 0) {
            lore.add(Component.text("🟢 " + summary.getClaimableCount() + " reward siap diklaim!")
                    .color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        } else if (summary.getClaimedCount() >= summary.getTotalMilestones() && summary.getTotalMilestones() > 0) {
            lore.add(Component.text("✅ Semua selesai!").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        } else {
            lore.add(Component.text("🟡 Sedang berjalan").color(NamedTextColor.YELLOW));
        }

        lore.add(Component.empty());
        lore.add(Component.text("Klik untuk membuka").color(NamedTextColor.DARK_GRAY));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createStatsItem(Player player, PlayerData data, CategoryRegistry registry) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.color("&b&l" + player.getName()));

        List<String> lore = new ArrayList<>();
        if (data != null) {
            lore.add(MessageUtil.color("&7Playtime: &f" + formatTime(data.getPlaytimeSeconds())));
            lore.add(MessageUtil.color("&7Blocks Broken: &f" + data.getBlocksBroken()));
            lore.add(MessageUtil.color("&7Blocks Placed: &f" + data.getBlocksPlaced()));
            lore.add(MessageUtil.color("&7Mobs Killed: &f" + data.getMobsKilled()));
            lore.add(MessageUtil.color("&7PvP Kills: &f" + data.getPlayersKilled()));
        } else {
            lore.add(MessageUtil.color("&7Loading data..."));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
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
