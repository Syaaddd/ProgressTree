package com.github.Syaaddd.progresstree.config;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.milestone.MilestoneChoice;
import com.github.Syaaddd.progresstree.milestone.MilestoneType;
import com.github.Syaaddd.progresstree.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {

    private final ProgressTree plugin;
    private FileConfiguration config;

    private boolean debug;
    private String databaseType;
    private String dbHost;
    private int dbPort;
    private String dbDatabase;
    private String dbUsername;
    private String dbPassword;

    private int checkInterval;
    private boolean communityRewardBroadcast;

    private String prefix;
    private String msgMilestoneAvailable;
    private String msgMilestoneLocked;
    private String msgMilestoneClaimed;
    private String msgNoMilestone;
    private String msgPlayerNotFound;
    private String msgConfigReloaded;
    private String msgNoPermission;
    private String msgAlreadyClaimed;
    private String msgMilestoneClaimedSelf;
    private String msgDataLoading;
    private String msgMigrationSuccess;
    private String msgMigrationFailed;

    private String guiTitle;
    private String availableColor;
    private String lockedColor;
    private String claimedColor;
    private String claimButton;
    private String chooseButton;

    // New GUI layout fields
    private int[] layoutTemplate;
    private int prevPageSlot;
    private int playerInfoSlot;
    private int pageIndicatorSlot;
    private int closeSlot;
    private int nextPageSlot;
    private String branchFillerMaterial;
    private String branchFillerName;
    private int progressBarSegments;
    private String progressFilledChar;
    private String progressEmptyChar;
    private String progressFilledColor;
    private String progressEmptyColor;

    // Hub (category menu) fields
    private String hubTitle;
    private String hubFillerMaterial;
    private boolean hubHideEmpty;
    private int[] hubCategorySlots;
    private int backButtonSlot;

    private Map<String, Milestone> milestones;

    public ConfigManager(ProgressTree plugin) {
        this.plugin = plugin;
        this.milestones = new LinkedHashMap<>();
    }

    public void load() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        debug = config.getBoolean("debug", false);

        databaseType = config.getString("database.type", "sqlite");
        dbHost = config.getString("database.host", "localhost");
        dbPort = config.getInt("database.port", 3306);
        dbDatabase = config.getString("database.database", "progresstree");
        dbUsername = config.getString("database.username", "root");
        dbPassword = config.getString("database.password", "");

        checkInterval = config.getInt("settings.check-interval", 60);
        communityRewardBroadcast = config.getBoolean("settings.community-reward-broadcast", true);

        prefix = config.getString("messages.prefix", "&8[&bProgressTree&8] ");
        msgMilestoneAvailable = config.getString("messages.milestone-available", "&aMilestone available! Click to claim.");
        msgMilestoneLocked = config.getString("messages.milestone-locked", "&cThis milestone is still locked.");
        msgMilestoneClaimed = config.getString("messages.milestone-claimed", "&eReward claimed: %reward%");
        msgNoMilestone = config.getString("messages.no-milestone", "&cNo milestone available.");
        msgPlayerNotFound = config.getString("messages.player-not-found", "&cPlayer not found.");
        msgConfigReloaded = config.getString("messages.config-reloaded", "&aConfiguration reloaded successfully.");
        msgNoPermission = config.getString("messages.no-permission", "&cYou don\'t have permission.");
        msgAlreadyClaimed = config.getString("messages.already-claimed", "&cThis milestone has already been claimed.");
        msgMilestoneClaimedSelf = config.getString("messages.milestone-claimed-self", "&eYou claimed: %reward%");
        msgDataLoading = config.getString("messages.data-loading", "&7Loading your progress data...");
        msgMigrationSuccess = config.getString("messages.migration-success", "&aMigration completed successfully!");
        msgMigrationFailed = config.getString("messages.migration-failed", "&cMigration failed. Check console for details.");

        guiTitle = config.getString("gui.title", "&8ProgressTree");
        availableColor = config.getString("gui.available-color", "&a");
        lockedColor = config.getString("gui.locked-color", "&7");
        claimedColor = config.getString("gui.claimed-color", "&e");
        claimButton = config.getString("gui.claim-button", "&aClick to Claim");
        chooseButton = config.getString("gui.choose-button", "&eChoose Reward");

// Auto-migrate old config format to new GUI schema
        boolean migrated = migrateOldGuiConfig();

        // Load new layout template
        List<Integer> layoutList = config.getIntegerList("gui.layout-template");
        if (layoutList.isEmpty()) {
            layoutList = Arrays.asList(4, 11, 13, 15, 20, 22, 24, 26, 29, 31, 33, 35, 38, 40, 42);
        }
        layoutTemplate = layoutList.stream().mapToInt(Integer::intValue).toArray();

        // Navigation slots
        prevPageSlot = config.getInt("gui.navigation.prev-page-slot", 45);
        playerInfoSlot = config.getInt("gui.navigation.player-info-slot", 47);
        pageIndicatorSlot = config.getInt("gui.navigation.page-indicator-slot", 49);
        closeSlot = config.getInt("gui.navigation.close-slot", 51);
        nextPageSlot = config.getInt("gui.navigation.next-page-slot", 53);

        // Branch filler
        branchFillerMaterial = config.getString("gui.branch-filler.material", "GRAY_STAINED_GLASS_PANE");
        branchFillerName = config.getString("gui.branch-filler.name", " ");

        // Hub (category menu) settings
        hubTitle = config.getString("gui.hub.title", "&8ProgressTree");
        hubFillerMaterial = config.getString("gui.hub.filler", "BLACK_STAINED_GLASS_PANE");
        hubHideEmpty = config.getBoolean("gui.hub.hide-empty", true);
        List<Integer> hubSlotList = config.getIntegerList("gui.hub.category-slots");
        if (hubSlotList.isEmpty()) {
            hubSlotList = Arrays.asList(10, 11, 12, 13, 14, 15, 16);
        }
        hubCategorySlots = hubSlotList.stream().mapToInt(Integer::intValue).toArray();
        backButtonSlot = config.getInt("gui.navigation.back-slot", 46);

        // Progress bar settings
        progressBarSegments = config.getInt("gui.progress-bar.segments", 20);
        progressFilledChar = config.getString("gui.progress-bar.filled-char", "▰");
        progressEmptyChar = config.getString("gui.progress-bar.empty-char", "\u25B1");
        progressFilledColor = config.getString("gui.progress-bar.filled-color", "&b");
        progressEmptyColor = config.getString("gui.progress-bar.empty-color", "&8");

        // Save migrated config back to disk
        if (migrated) {
            plugin.saveConfig();
            plugin.getLog().info("[CONFIG] Auto-migrated old GUI config to new schema. Old keys preserved as comments.");
        }

        loadMilestones();
    }

    /**
     * Fix #3: Validate config schema at startup. Log clear errors for invalid milestones/choices.
     */
    private void loadMilestones() {
        milestones.clear();
        ConfigurationSection milestonesSection = config.getConfigurationSection("milestones");
        if (milestonesSection == null) {
            plugin.getLog().warn("No \'milestones\' section found in config.yml!");
            return;
        }

        int validCount = 0;
        int errorCount = 0;

        for (String key : milestonesSection.getKeys(false)) {
            ConfigurationSection ms = milestonesSection.getConfigurationSection(key);
            if (ms == null) {
                plugin.getLog().severe("[CONFIG] Milestone \'" + key + "\' is not a valid section - skipped.");
                errorCount++;
                continue;
            }

            String typeStr = ms.getString("type");
            if (typeStr == null || typeStr.isEmpty()) {
                plugin.getLog().severe("[CONFIG] Milestone \'" + key + "\' missing \'type\' field - skipped.");
                errorCount++;
                continue;
            }

            MilestoneType type;
            try {
                type = MilestoneType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLog().severe("[CONFIG] Milestone \'" + key + "\' has invalid type \'" + typeStr + "\' - skipped. Valid types: " + Arrays.toString(MilestoneType.values()));
                errorCount++;
                continue;
            }

            int amount = ms.getInt("amount", -1);
            if (amount <= 0) {
                plugin.getLog().severe("[CONFIG] Milestone \'" + key + "\' has invalid amount (" + amount + ") - skipped.");
                errorCount++;
                continue;
            }

            String icon = ms.getString("icon", "");
            String displayColor = ms.getString("color", "&6");
            String category = ms.getString("category", null);

            List<MilestoneChoice> choices = new ArrayList<>();
            List<Map<?, ?>> choicesList = ms.getMapList("choices");
            if (choicesList == null || choicesList.isEmpty()) {
                plugin.getLog().warn("[CONFIG] Milestone \'" + key + "\' has no choices defined.");
            } else {
                for (int i = 0; i < choicesList.size(); i++) {
                    Map<?, ?> choiceMap = choicesList.get(i);
                    String cid = choiceMap.get("id") != null ? choiceMap.get("id").toString() : null;
                    String cname = choiceMap.get("name") != null ? choiceMap.get("name").toString() : null;
                    String ccmd = choiceMap.get("command") != null ? choiceMap.get("command").toString() : null;

                    if (cid == null || cname == null || ccmd == null) {
                        plugin.getLog().severe("[CONFIG] Milestone \'" + key + "\' choice #" + (i+1) + " missing required fields (id/name/command) - skipped.");
                        errorCount++;
                        continue;
                    }
                    choices.add(new MilestoneChoice(cid, cname, ccmd));
                }
            }

            milestones.put(key, new Milestone(key, type, amount, choices, icon, displayColor, category));
            validCount++;
        }

        plugin.getLog().info("Loaded " + validCount + " milestones" + (errorCount > 0 ? " (" + errorCount + " errors)" : ""));
    }

    public List<Milestone> getMilestonesInOrder() {
        return new ArrayList<>(milestones.values());
    }

    public Milestone getMilestone(String id) {
        return milestones.get(id);
    }

    // === Getters ===
    public boolean isDebug() { return debug; }
    public String getDatabaseType() { return databaseType; }
    public String getDbHost() { return dbHost; }
    public int getDbPort() { return dbPort; }
    public String getDbDatabase() { return dbDatabase; }
    public String getDbUsername() { return dbUsername; }
    public String getDbPassword() { return dbPassword; }
    public int getCheckInterval() { return checkInterval; }
    public boolean isCommunityRewardBroadcast() { return communityRewardBroadcast; }
    public String getPrefix() { return MessageUtil.color(prefix); }
    public String getMsgMilestoneAvailable() { return MessageUtil.color(msgMilestoneAvailable); }
    public String getMsgMilestoneLocked() { return MessageUtil.color(msgMilestoneLocked); }
    public String getMsgMilestoneClaimed() { return MessageUtil.color(msgMilestoneClaimed); }
    public String getMsgNoMilestone() { return MessageUtil.color(msgNoMilestone); }
    public String getMsgPlayerNotFound() { return MessageUtil.color(msgPlayerNotFound); }
    public String getMsgConfigReloaded() { return MessageUtil.color(msgConfigReloaded); }
    public String getMsgNoPermission() { return MessageUtil.color(msgNoPermission); }
    public String getMsgAlreadyClaimed() { return MessageUtil.color(msgAlreadyClaimed); }
    public String getMsgMilestoneClaimedSelf() { return MessageUtil.color(msgMilestoneClaimedSelf); }
    public String getMsgDataLoading() { return MessageUtil.color(msgDataLoading); }
    public String getMsgMigrationSuccess() { return MessageUtil.color(msgMigrationSuccess); }
    public String getMsgMigrationFailed() { return MessageUtil.color(msgMigrationFailed); }
    public String getGuiTitle() { return MessageUtil.color(guiTitle); }
    public String getAvailableColor() { return MessageUtil.color(availableColor); }
    public String getLockedColor() { return MessageUtil.color(lockedColor); }
    public String getClaimedColor() { return MessageUtil.color(claimedColor); }
    public String getClaimButton() { return MessageUtil.color(claimButton); }
    public String getChooseButton() { return MessageUtil.color(chooseButton); }

    // New GUI getters
    public int[] getLayoutTemplate() { return layoutTemplate; }
    public int getPrevPageSlot() { return prevPageSlot; }
    public int getPlayerInfoSlot() { return playerInfoSlot; }
    public int getPageIndicatorSlot() { return pageIndicatorSlot; }
    public int getCloseSlot() { return closeSlot; }
    public int getNextPageSlot() { return nextPageSlot; }
    public String getBranchFillerMaterial() { return branchFillerMaterial; }

    // ===== Hub (category menu) getters =====
    public String getHubTitle() { return hubTitle; }
    public boolean isHubHideEmpty() { return hubHideEmpty; }
    public int[] getHubCategorySlots() { return hubCategorySlots; }
    public int getBackButtonSlot() { return backButtonSlot; }
    public Material getHubFillerMaterial() {
        try {
            return Material.valueOf(hubFillerMaterial.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return Material.BLACK_STAINED_GLASS_PANE;
        }
    }
    public String getBranchFillerName() { return branchFillerName; }
    public int getProgressBarSegments() { return progressBarSegments; }
    public String getProgressFilledChar() { return progressFilledChar; }
    public String getProgressEmptyChar() { return progressEmptyChar; }
    public String getProgressFilledColor() { return progressFilledColor; }
    public String getProgressEmptyColor() { return progressEmptyColor; }

/**
     * Auto-migrate old GUI config (milestone-slots) to new schema (layout-template + navigation + branch-filler + progress-bar).
     * Returns true if migration was performed and config should be saved.
     */
    private boolean migrateOldGuiConfig() {
        boolean migrated = false;

        // Check if old milestone-slots key exists and new layout-template doesn't
        List<Integer> oldSlots = config.getIntegerList("gui.milestone-slots");
        List<Integer> newLayout = config.getIntegerList("gui.layout-template");

        if (!oldSlots.isEmpty() && newLayout.isEmpty()) {
            plugin.getLog().info("[CONFIG] Detected old 'gui.milestone-slots' format. Migrating to new GUI schema...");

            // Migrate milestone-slots → layout-template
            config.set("gui.layout-template", oldSlots);
            migrated = true;

            // Set default navigation slots if not present
            if (!config.contains("gui.navigation")) {
                config.set("gui.navigation.prev-page-slot", 45);
                config.set("gui.navigation.player-info-slot", 47);
                config.set("gui.navigation.page-indicator-slot", 49);
                config.set("gui.navigation.close-slot", 51);
                config.set("gui.navigation.next-page-slot", 53);
            }

            // Set default branch filler if not present
            if (!config.contains("gui.branch-filler")) {
                config.set("gui.branch-filler.material", "GRAY_STAINED_GLASS_PANE");
                config.set("gui.branch-filler.name", " ");
            }

            // Set default progress bar settings if not present
            if (!config.contains("gui.progress-bar")) {
                config.set("gui.progress-bar.segments", 20);
                config.set("gui.progress-bar.filled-char", "\u25B0");
                config.set("gui.progress-bar.empty-char", "\u25B1");
                config.set("gui.progress-bar.filled-color", "&b");
                config.set("gui.progress-bar.empty-color", "&8");
            }

            // Keep old milestone-slots as comment for reference (Bukkit YAML doesn't support comments well,
            // so we just leave it - it won't interfere since we read layout-template first now)
            plugin.getLog().info("[CONFIG] Migration complete. Old 'gui.milestone-slots' kept for reference.");
        }

        return migrated;
    }

    /** @deprecated Use getLayoutTemplate() instead */
    @Deprecated
    public int[] getMilestoneSlots() { return layoutTemplate; }
}