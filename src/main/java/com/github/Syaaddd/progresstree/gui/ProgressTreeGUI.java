package com.github.Syaaddd.progresstree.gui;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.category.Category;
import com.github.Syaaddd.progresstree.category.CategoryRegistry;
import com.github.Syaaddd.progresstree.config.ConfigManager;
import com.github.Syaaddd.progresstree.data.PlayerData;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.milestone.MilestoneChoice;
import com.github.Syaaddd.progresstree.milestone.MilestoneManager;
import com.github.Syaaddd.progresstree.milestone.MilestoneType;
import com.github.Syaaddd.progresstree.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Category tree GUI (v2.1.0). ALWAYS scoped to a category - the legacy
 * flat "all milestones in one list" mode was REMOVED because clicking
 * Next Page on a category tree could fall back into the old GUI
 * (session maps were per-instance and state was lost).
 *
 * Navigation context now travels with the inventory itself via {@link GuiHolder},
 * so shared/instance state can no longer desync.
 */
public class ProgressTreeGUI {

    private final ProgressTree plugin;

    public ProgressTreeGUI(ProgressTree plugin) {
        this.plugin = plugin;
    }

    /** Entry point used by external callers (commands): always show the hub. */
    public void open(Player player) {
        plugin.getHubGui().open(player);
    }

    /**
     * Open the tree for a category at the given 0-based page.
     * Falls back to the hub if the category no longer exists.
     */
    public void open(Player player, String categoryId, int page) {
        CategoryRegistry registry = plugin.getCategoryRegistry();
        Category cat = (categoryId != null) ? registry.getCategory(categoryId) : null;
        if (cat == null) {
            plugin.getLog().warn("[GUI] Category '" + categoryId + "' not found - opening hub instead.");
            plugin.getHubGui().open(player);
            return;
        }

        ConfigManager cfg = plugin.getConfigManager();
        int[] template = cfg.getLayoutTemplate();
        List<Milestone> milestones = registry.getMilestonesForCategory(categoryId);

        int perPage = Math.max(1, template.length);
        int totalPages = Math.max(1, (int) Math.ceil((double) milestones.size() / perPage));
        page = Math.max(0, Math.min(page, totalPages - 1));

        GuiHolder holder = GuiHolder.tree(categoryId, page);
        String title = MessageUtil.color(cfg.getGuiTitle() + " \u00bb " + cat.getName());
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.attach(inv);

        fillBranchFiller(inv, cfg);

        PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
        MilestoneManager manager = plugin.getMilestoneManager();
        int startIdx = page * perPage;

        for (int i = 0; i < template.length; i++) {
            int msIndex = startIdx + i;
            if (msIndex >= milestones.size()) break;

            Milestone milestone = milestones.get(msIndex);
            boolean claimed = data != null && data.hasClaimed(milestone.getId());
            boolean available = data != null && manager.hasReached(data, milestone);

            inv.setItem(template[i], createMilestoneItem(milestone, claimed, available, data, cfg, cat));
        }

        placeNavBar(inv, player, page, totalPages, cfg, milestones, cat, data);
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

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }
    }

    /** Nav bar (row 5). All slots come from gui.navigation.* - single source of truth. */
    private void placeNavBar(Inventory inv, Player player, int currentPage, int totalPages,
                             ConfigManager cfg, List<Milestone> milestones, Category cat, PlayerData data) {
        // Prev page
        ItemStack prev = new ItemStack(currentPage > 0 ? Material.ARROW : Material.BARRIER);
        ItemMeta pm = prev.getItemMeta();
        pm.setDisplayName(MessageUtil.color(currentPage > 0 ? "&a&l\u25C0 Prev Page" : "&7&l\u25C0 Prev Page"));
        prev.setItemMeta(pm);
        inv.setItem(cfg.getPrevPageSlot(), prev);

        // Back to hub
        ItemStack back = new ItemStack(Material.OAK_DOOR);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(MessageUtil.color("&e&l\u00AB Back to Categories"));
        bm.setLore(Collections.singletonList(MessageUtil.color("&7Back to category selection")));
        back.setItemMeta(bm);
        inv.setItem(cfg.getBackButtonSlot(), back);

        // Player info
        ItemStack info = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(MessageUtil.color("&b&l" + player.getName()));
        List<String> infoLore = new ArrayList<>();
        infoLore.add(MessageUtil.color("&7Category: " + cat.getName()));
        infoLore.add(MessageUtil.color("&7Claimed: &f" + countClaimed(data, milestones) + " / " + milestones.size()));
        infoLore.add(MessageUtil.color("&7Page: &f" + (currentPage + 1) + " / " + totalPages));
        infoLore.add(MessageUtil.color("&7Playtime: &f" + formatTime(data != null ? data.getPlaytimeSeconds() : 0)));
        infoLore.add(MessageUtil.color("&7Blocks Broken: &f" + (data != null ? data.getBlocksBroken() : 0)));
        infoLore.add(MessageUtil.color("&7Blocks Placed: &f" + (data != null ? data.getBlocksPlaced() : 0)));
        infoLore.add(MessageUtil.color("&7Mobs Killed: &f" + (data != null ? data.getMobsKilled() : 0)));
        infoLore.add(MessageUtil.color("&7PvP Kills: &f" + (data != null ? data.getPlayersKilled() : 0)));
        im.setLore(infoLore);
        info.setItemMeta(im);
        inv.setItem(cfg.getPlayerInfoSlot(), info);

        // Page indicator
        ItemStack pageItem = new ItemStack(Material.BOOK);
        ItemMeta pgm = pageItem.getItemMeta();
        pgm.setDisplayName(MessageUtil.color("&e&lPage " + (currentPage + 1) + " / " + totalPages));
        pgm.setLore(Collections.singletonList(MessageUtil.color("&7Category: " + cat.getName())));
        pageItem.setItemMeta(pgm);
        inv.setItem(cfg.getPageIndicatorSlot(), pageItem);

        // Close
        ItemStack close = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(MessageUtil.color("&c&l\u2715 Close"));
        close.setItemMeta(cm);
        inv.setItem(cfg.getCloseSlot(), close);

        // Next page
        ItemStack next = new ItemStack(currentPage < totalPages - 1 ? Material.ARROW : Material.BARRIER);
        ItemMeta nm = next.getItemMeta();
        nm.setDisplayName(MessageUtil.color(currentPage < totalPages - 1 ? "&a&lNext Page \u25B6" : "&7&lNext Page \u25B6"));
        next.setItemMeta(nm);
        inv.setItem(cfg.getNextPageSlot(), next);
    }

    /**
     * Route a click inside the category tree. Navigation context comes
     * from the live inventory holder - category and page can never be lost.
     */
    public void handleClick(Player player, Inventory inv, int slot) {
        if (!(inv.getHolder() instanceof GuiHolder holder) || holder.categoryId() == null) {
            // Stale GUI or holder lost - recover by opening the hub.
            plugin.getHubGui().open(player);
            return;
        }

        String categoryId = holder.categoryId();
        int currentPage = holder.page();
        ConfigManager cfg = plugin.getConfigManager();
        int[] template = cfg.getLayoutTemplate();
        List<Milestone> milestones = plugin.getCategoryRegistry().getMilestonesForCategory(categoryId);

        int perPage = Math.max(1, template.length);
        int totalPages = Math.max(1, (int) Math.ceil((double) milestones.size() / perPage));

        // --- Navigation ---
        if (slot == cfg.getPrevPageSlot()) {
            if (currentPage > 0) open(player, categoryId, currentPage - 1);
            return;
        }
        if (slot == cfg.getNextPageSlot()) {
            if (currentPage < totalPages - 1) open(player, categoryId, currentPage + 1);
            return;
        }
        if (slot == cfg.getBackButtonSlot()) {
            plugin.getHubGui().open(player);
            return;
        }
        if (slot == cfg.getCloseSlot()) {
            player.closeInventory();
            return;
        }
        if (slot == cfg.getPlayerInfoSlot() || slot == cfg.getPageIndicatorSlot()) {
            return; // display only
        }

        // --- Milestone nodes ---
        int startIdx = currentPage * perPage;
        for (int i = 0; i < template.length; i++) {
            if (template[i] != slot) continue;

            int msIndex = startIdx + i;
            if (msIndex >= milestones.size()) return; // empty node

            Milestone milestone = milestones.get(msIndex);
            PlayerData data = plugin.getRepository().getPlayerData(player.getUniqueId());
            MilestoneManager manager = plugin.getMilestoneManager();
            boolean claimed = data != null && data.hasClaimed(milestone.getId());
            boolean available = data != null && manager.hasReached(data, milestone);

            if (!available || claimed) return;

            if (milestone.hasChoices()) {
                plugin.getChoiceGUI().open(player, milestone.getId(), categoryId, currentPage);
            } else {
                manager.claimMilestone(player, milestone.getId(), null);
                open(player, categoryId, currentPage); // stay on same category + page
            }
            return;
        }
    }

    private int countClaimed(PlayerData data, List<Milestone> milestones) {
        if (data == null) return 0;
        int count = 0;
        for (Milestone ms : milestones) {
            if (data.hasClaimed(ms.getId())) count++;
        }
        return count;
    }

    // ===================== item rendering =====================

    private ItemStack createMilestoneItem(Milestone milestone, boolean claimed, boolean available,
                                          PlayerData data, ConfigManager cfg, Category cat) {
        ItemStack item = getItemForState(milestone, claimed, available);
        ItemMeta meta = item.getItemMeta();

        String typeStr = milestone.getType().name().replace("_", " ");
        String amountStr = formatAmount(milestone.getAmount(), milestone.getType());
        String currentStr = getCurrentProgress(data, milestone);
        double progress = getProgress(data, milestone);

        String nameColor = claimed ? cfg.getClaimedColor() : milestone.getDisplayColor();
        meta.setDisplayName(MessageUtil.color(nameColor + "&l" + milestone.getId().replace('_', ' ')));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Category: ").color(NamedTextColor.GRAY)
                .append(Component.text(MessageUtil.strip(cat.getName())).color(NamedTextColor.WHITE)));
        lore.add(Component.text("Type: ").color(NamedTextColor.GRAY)
                .append(Component.text(typeStr).color(NamedTextColor.WHITE)));
        lore.add(Component.empty());
        lore.add(buildProgressBarComponent(progress, cfg));
        lore.add(Component.text(currentStr + " / " + amountStr + "  (" + String.format("%.0f", progress) + "%)")
                .color(NamedTextColor.WHITE));
        lore.add(Component.empty());

        if (claimed) {
            lore.add(Component.text("\u2713 CLAIMED").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            String choiceId = data != null ? data.getClaimedChoice(milestone.getId()) : null;
            if (choiceId != null) {
                final String finalChoiceId = choiceId;
                MilestoneChoice choice = milestone.getChoices().stream()
                        .filter(c -> c.getId().equals(finalChoiceId))
                        .findFirst()
                        .orElse(null);
                if (choice != null) {
                    lore.add(Component.text("Reward: ").color(NamedTextColor.GRAY)
                            .append(Component.text(choice.getName()).color(NamedTextColor.AQUA)));
                }
            }
        } else if (available) {
            lore.add(Component.text(MessageUtil.strip(cfg.getClaimButton())).color(parseColor(cfg.getClaimButton(), NamedTextColor.GREEN)));
            if (milestone.hasChoices()) {
                lore.add(Component.text(MessageUtil.strip(cfg.getChooseButton())).color(parseColor(cfg.getChooseButton(), NamedTextColor.YELLOW)));
            }
        } else {
            lore.add(Component.text("\uD83D\uDD12 Locked").color(parseColor(cfg.getLockedColor(), NamedTextColor.RED)));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * - Locked: BARRIER (mystery)
     * - Available: config icon + enchant glint
     * - Claimed: config icon, no glint, claimed-color name
     * Fallback NETHER_STAR when the config icon is empty/invalid/non-item.
     */
    private ItemStack getItemForState(Milestone milestone, boolean claimed, boolean available) {
        if (!available && !claimed) {
            return new ItemStack(Material.BARRIER);
        }
        ItemStack item = new ItemStack(resolveMilestoneMaterial(milestone));
        ItemMeta meta = item.getItemMeta();
        if (available && !claimed) {
            meta.setEnchantmentGlintOverride(true);
        }
        item.setItemMeta(meta);
        return item;
    }

    private Material resolveMilestoneMaterial(Milestone milestone) {
        String icon = milestone.getIcon();
        if (icon != null && !icon.isEmpty()) {
            try {
                Material mat = Material.valueOf(icon.toUpperCase());
                if (mat.isItem()) return mat;
            } catch (IllegalArgumentException ignored) {
                // fall through to default
            }
        }
        return Material.NETHER_STAR;
    }

    /** Progress bar as Adventure Component, per-segment red→yellow→green gradient. */
    private Component buildProgressBarComponent(double percentage, ConfigManager cfg) {
        int segments = cfg.getProgressBarSegments();
        int filled = (int) Math.round(percentage / 100.0 * segments);
        filled = Math.max(0, Math.min(segments, filled));

        Component bar = Component.empty();
        for (int i = 0; i < segments; i++) {
            if (i < filled) {
                double segPct = ((double) (i + 1) / segments) * percentage;
                bar = bar.append(Component.text(cfg.getProgressFilledChar()).color(getGradientColor(segPct)));
            } else {
                bar = bar.append(Component.text(cfg.getProgressEmptyChar()).color(parseColor(cfg.getProgressEmptyColor(), NamedTextColor.DARK_GRAY)));
            }
        }
        return bar;
    }

    private TextColor getGradientColor(double percentage) {
        float p = (float) Math.max(0, Math.min(100, percentage));
        int r, g;
        if (p <= 50f) {
            r = 255;
            g = (int) (255 * (p / 50f));
        } else {
            r = (int) (255 * ((100f - p) / 50f));
            g = 255;
        }
        return TextColor.color(r, g, 0);
    }

    /** Extract the last legacy color code (&x or \u00a7x) from a string as a TextColor. */
    private TextColor parseColor(String legacy, TextColor fallback) {
        if (legacy == null || legacy.isEmpty()) {
            return fallback;
        }
        int i = Math.max(legacy.lastIndexOf('\u00a7'), legacy.lastIndexOf('&'));
        if (i < 0 || i + 1 >= legacy.length()) {
            return fallback;
        }
        return switch (Character.toLowerCase(legacy.charAt(i + 1))) {
            case '0' -> NamedTextColor.BLACK;
            case '1' -> NamedTextColor.DARK_BLUE;
            case '2' -> NamedTextColor.DARK_GREEN;
            case '3' -> NamedTextColor.DARK_AQUA;
            case '4' -> NamedTextColor.DARK_RED;
            case '5' -> NamedTextColor.DARK_PURPLE;
            case '6' -> NamedTextColor.GOLD;
            case '7' -> NamedTextColor.GRAY;
            case '8' -> NamedTextColor.DARK_GRAY;
            case '9' -> NamedTextColor.BLUE;
            case 'a' -> NamedTextColor.GREEN;
            case 'b' -> NamedTextColor.AQUA;
            case 'c' -> NamedTextColor.RED;
            case 'd' -> NamedTextColor.LIGHT_PURPLE;
            case 'e' -> NamedTextColor.YELLOW;
            case 'f' -> NamedTextColor.WHITE;
            default -> fallback;
        };
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
        };
        int required = milestone.getAmount();
        if (required <= 0) return 0;
        return Math.min(100.0, (double) current / required * 100);
    }

    private String formatAmount(int amount, MilestoneType type) {
        return switch (type) {
            case PLAYTIME, COMMUNITY_PLAYTIME -> formatTime(amount);
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
