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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Hub GUI displaying all categories with claim badges and progress summaries.
 * Layout per PRD 5.1 (4 rows):
 *   Row 0: filler border
 *   Row 1: category icons (gui.hub.category-slots, default 10-16)
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
        List<Category> visible = visibleCategories(registry);

        GuiHolder holder = GuiHolder.hub();
        String title = MessageUtil.color(plugin.getConfigManager().getHubTitle());
        Inventory inv = Bukkit.createInventory(holder, 36, title); // fixed 4 rows per PRD
        holder.attach(inv);

        // Fill everything with hub filler
        ItemStack filler = new ItemStack(plugin.getConfigManager().getHubFillerMaterial());
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        int[] slots = plugin.getConfigManager().getHubCategorySlots();
        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());

        if (visible.size() > slots.length) {
            plugin.getLog().warn("[HUB] " + visible.size() + " categories but only " + slots.length
                    + " slots configured (gui.hub.category-slots) - " + (visible.size() - slots.length) + " hidden.");
        }

        for (int i = 0; i < visible.size() && i < slots.length; i++) {
            Category cat = visible.get(i);
            List<Milestone> catMilestones = registry.getMilestonesForCategory(cat.getId());
            CategorySummary summary = CategorySummary.compute(plugin, cat.getId(), catMilestones, data, plugin.getMilestoneManager());
            inv.setItem(slots[i], createCategoryItem(cat, summary));
        }

        // Stats item: row 3 center (slot 31)
        inv.setItem(31, createStatsItem(player, data));

        // Close button: row 3 right (slot 35)
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(MessageUtil.color("&c&l\u2715 Close"));
        close.setItemMeta(cm);
        inv.setItem(35, close);

        player.openInventory(inv);
    }

    /** Categories visible in the hub, respecting hide-empty. Shared by open() and handleClick(). */
    private List<Category> visibleCategories(CategoryRegistry registry) {
        boolean hideEmpty = plugin.getConfigManager().isHubHideEmpty();
        List<Category> visible = new ArrayList<>();
        for (Category cat : registry.getSortedCategories()) {
            if (!hideEmpty || registry.hasMilestones(cat.getId())) {
                visible.add(cat);
            }
        }
        return visible;
    }

    /**
     * Handle a click in the hub inventory. The event must already be
     * cancelled by the listener - this only routes the action.
     */
    public void handleClick(Player player, int slot) {
        CategoryRegistry registry = plugin.getCategoryRegistry();
        List<Category> visible = visibleCategories(registry);
        int[] slots = plugin.getConfigManager().getHubCategorySlots();

        for (int i = 0; i < visible.size() && i < slots.length; i++) {
            if (slots[i] == slot) {
                // Page 0 = first page of the category tree (old code used 1 - off-by-one)
                plugin.getTreeGui().open(player, visible.get(i).getId(), 0);
                return;
            }
        }

        if (slot == 35) { // Close
            player.closeInventory();
            return;
        }

        // Slot 31 (stats) and everything else: display only, no action.
    }

    private ItemStack createCategoryItem(Category cat, CategorySummary summary) {
        ItemStack item = new ItemStack(cat.getIcon());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(MessageUtil.color(cat.getColor() + "&l" + cat.getName()));

        // Glow if claimable
        if (summary.getClaimableCount() > 0) {
            meta.setEnchantmentGlintOverride(true);
        }

        List<Component> lore = new ArrayList<>();

        for (String desc : cat.getDescription()) {
            lore.add(Component.text(MessageUtil.strip(desc)).color(NamedTextColor.GRAY));
        }
        if (!cat.getDescription().isEmpty()) {
            lore.add(Component.empty());
        }

        lore.add(Component.text("Claimed: ").color(NamedTextColor.GRAY)
                .append(Component.text(summary.getClaimedCount() + " / " + summary.getTotalMilestones()).color(NamedTextColor.WHITE)));

        if (summary.getNextMilestone() != null) {
            lore.add(Component.text("Next: ").color(NamedTextColor.GRAY)
                    .append(Component.text(summary.getNextMilestone().getId()).color(NamedTextColor.WHITE))
                    .append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text(String.format("%.0f%%", summary.getNextProgress())).color(NamedTextColor.YELLOW)));
        }

        lore.add(Component.empty());

        if (summary.getClaimableCount() > 0) {
            lore.add(Component.text("\uD83D\uDFE2 " + summary.getClaimableCount() + " rewards ready to claim!")
                    .color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        } else if (summary.isComplete()) {
            lore.add(Component.text("\u2705 All done!").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        } else {
            lore.add(Component.text("\uD83D\uDFE1 In progress").color(NamedTextColor.YELLOW));
        }

        lore.add(Component.empty());
        lore.add(Component.text("Click to open").color(NamedTextColor.DARK_GRAY));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createStatsItem(Player player, PlayerData data) {
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
