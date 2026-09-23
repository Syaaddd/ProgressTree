package com.github.Syaaddd.progresstree.category;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.milestone.MilestoneType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Registry for milestone categories. Loads from config, resolves milestones to categories,
 * and provides sorted category list for hub GUI.
 */
public class CategoryRegistry {

    private final ProgressTree plugin;
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private final Map<String, String> milestoneToCategory = new HashMap<>();

    public CategoryRegistry(ProgressTree plugin) {
        this.plugin = plugin;
    }

    /**
     * Load categories from config. If gui.categories section is missing, generate defaults.
     * Also resolves all milestones to their categories.
     */
    public void load() {
        categories.clear();
        milestoneToCategory.clear();

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection catSection = config.getConfigurationSection("gui.categories");

        if (catSection == null || catSection.getKeys(false).isEmpty()) {
            generateDefaultCategories(config);
            catSection = config.getConfigurationSection("gui.categories");
        }

        com.github.Syaaddd.progresstree.util.Logger log = plugin.getLog();

        // Load each category
        for (String id : catSection.getKeys(false)) {
            ConfigurationSection cs = catSection.getConfigurationSection(id);
            if (cs == null) continue;

            String name = cs.getString("name", "&7" + id);
            String iconStr = cs.getString("icon", "CHEST");
            String color = cs.getString("color", "&7");
            int order = cs.getInt("order", 99);
            List<String> desc = cs.getStringList("description");

            Material icon;
            try {
                icon = Material.valueOf(iconStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[CATEGORY] Invalid icon '" + iconStr + "' for category '" + id + "', falling back to CHEST");
                icon = Material.CHEST;
            }

            Set<MilestoneType> types = EnumSet.noneOf(MilestoneType.class);
            List<String> typeStrs = cs.getStringList("types");
            for (String t : typeStrs) {
                try {
                    types.add(MilestoneType.valueOf(t.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("[CATEGORY] Invalid type '" + t + "' in category '" + id + "' - skipped");
                }
            }

            categories.put(id, new Category(id, name, icon, color, order, desc, types));
        }

        // Resolve milestones to categories
        List<Milestone> allMilestones = plugin.getConfigManager().getMilestonesInOrder();
        for (Milestone ms : allMilestones) {
            String resolved = resolveCategory(ms);
            milestoneToCategory.put(ms.getId(), resolved);
        }

        log.info("[CATEGORY] Loaded " + categories.size() + " categories, mapped " + milestoneToCategory.size() + " milestones");
    }

    /**
     * Generate default 7 categories (Opsi 1 from PRD) and write to config.
     */
    private void generateDefaultCategories(FileConfiguration config) {
        config.set("gui.hub.title", "&8ProgressTree");
        config.set("gui.hub.rows", 4);
        config.set("gui.hub.hide-empty", true);
        config.set("gui.hub.filler", "BLACK_STAINED_GLASS_PANE");
        config.set("gui.hub.category-slots", Arrays.asList(10, 11, 12, 13, 14, 15, 16));

        setDefaultCategory(config, "playtime", "&ePlaytime", "CLOCK", "&e", 1,
                Arrays.asList("&7Bermain lebih lama, dapat lebih banyak."), Arrays.asList("PLAYTIME"));
        setDefaultCategory(config, "mining", "&bMining", "DIAMOND_PICKAXE", "&b", 2,
                Arrays.asList("&7Hancurkan blok, kumpulkan hadiah."), Arrays.asList("BLOCK_BREAK"));
        setDefaultCategory(config, "building", "&6Building", "BRICKS", "&6", 3,
                Arrays.asList("&7Bangun dunia, raih penghargaan."), Arrays.asList("BLOCK_PLACE"));
        setDefaultCategory(config, "hunting", "&aHunting", "ZOMBIE_HEAD", "&a", 4,
                Arrays.asList("&7Buru mob, kumpulkan trofi."), Arrays.asList("MOB_KILL"));
        setDefaultCategory(config, "combat", "&cCombat", "IRON_SWORD", "&c", 5,
                Arrays.asList("&7Kalahkan pemain lain."), Arrays.asList("PLAYER_KILL"));
        setDefaultCategory(config, "loyalty", "&dLoyalty", "CAMPFIRE", "&d", 6,
                Arrays.asList("&7Setia bermain setiap hari."), Arrays.asList("JOIN"));
        setDefaultCategory(config, "community", "&5Community", "BEACON", "&5", 7,
                Arrays.asList("&7Kontribusi untuk server."), Arrays.asList("COMMUNITY_PLAYTIME"));

        plugin.saveConfig();
        plugin.getLog().info("[CATEGORY] Generated default categories (config v2.0.0 migration)");
    }

    private void setDefaultCategory(FileConfiguration config, String id, String name, String icon,
                                     String color, int order, List<String> desc, List<String> types) {
        String path = "gui.categories." + id;
        config.set(path + ".name", name);
        config.set(path + ".icon", icon);
        config.set(path + ".color", color);
        config.set(path + ".order", order);
        config.set(path + ".description", desc);
        config.set(path + ".types", types);
    }

    /**
     * Resolve which category a milestone belongs to.
     * Priority: explicit category field > type-based match > fallback "other"
     */
    private String resolveCategory(Milestone ms) {
        // Check explicit category override first
        String explicit = ms.getCategory();
        if (explicit != null && !explicit.isEmpty()) {
            if (categories.containsKey(explicit)) {
                return explicit;
            }
            plugin.getLog().warn("[CATEGORY] Milestone '" + ms.getId() + "' has category='" + explicit + "' but it's not defined in gui.categories - falling back to type-based resolution");
        }

        // Type-based resolution
        for (Map.Entry<String, Category> entry : categories.entrySet()) {
            if (entry.getValue().matchesType(ms.getType())) {
                return entry.getKey();
            }
        }

        // Fallback: no matching category found
        plugin.getLog().warn("[CATEGORY] Milestone '" + ms.getId() + "' (type=" + ms.getType() + ") has no matching category!");
        return null;
    }

    /** Get all categories sorted by order */
    public List<Category> getSortedCategories() {
        return categories.values().stream()
                .sorted(Comparator.comparingInt(Category::getOrder))
                .toList();
    }

    /** Get category by ID */
    public Category getCategory(String id) {
        return categories.get(id);
    }

    /** Get category ID for a milestone */
    public String getCategoryForMilestone(String milestoneId) {
        return milestoneToCategory.get(milestoneId);
    }

    /** Get all milestones belonging to a category, sorted by amount ascending */
    public List<Milestone> getMilestonesForCategory(String categoryId) {
        Category cat = categories.get(categoryId);
        if (cat == null) return Collections.emptyList();

        return plugin.getConfigManager().getMilestonesInOrder().stream()
                .filter(ms -> cat.matchesType(ms.getType()))
                .sorted(Comparator.comparingInt(Milestone::getAmount))
                .toList();
    }

    /** Check if a category has any milestones */
    public boolean hasMilestones(String categoryId) {
        return !getMilestonesForCategory(categoryId).isEmpty();
    }
}