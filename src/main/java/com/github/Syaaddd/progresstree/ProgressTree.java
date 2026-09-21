package com.github.Syaaddd.progresstree;

import com.github.Syaaddd.progresstree.category.CategoryRegistry;
import com.github.Syaaddd.progresstree.command.ProgressTreeCommand;
import com.github.Syaaddd.progresstree.command.ProgressTreeTabCompleter;
import com.github.Syaaddd.progresstree.config.ConfigManager;
import com.github.Syaaddd.progresstree.data.DatabaseManager;
import com.github.Syaaddd.progresstree.data.ProgressRepository;
import com.github.Syaaddd.progresstree.data.MigrationService;
import com.github.Syaaddd.progresstree.gui.ChoiceGUI;
import com.github.Syaaddd.progresstree.gui.ProgressTreeGUI;
import com.github.Syaaddd.progresstree.gui.CategoryHubGUI;
import com.github.Syaaddd.progresstree.event.ProgressUpdateListener;
import com.github.Syaaddd.progresstree.listener.EventListeners;
import com.github.Syaaddd.progresstree.listener.PlaytimeTracker;
import com.github.Syaaddd.progresstree.milestone.MilestoneManager;
import com.github.Syaaddd.progresstree.placeholder.PlaceholderHook;
import com.github.Syaaddd.progresstree.util.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProgressTree extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ProgressRepository repository;
    private MilestoneManager milestoneManager;
    private CategoryRegistry categoryRegistry;
    private Logger logger;

    @Override
    public void onEnable() {
        // Initialize logger FIRST — ConfigManager.loadMilestones() uses it
        configManager = new ConfigManager(this);
        logger = new Logger(this, false); // temp logger with debug off; re-created after load
        configManager.load();
        // Re-create logger with actual debug flag from config
        logger = new Logger(this, configManager.isDebug());

        logger.info("ProgressTree v" + getDescription().getVersion() + " enabling...");

        // Database init + migration detection
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        MigrationService migrationService = new MigrationService(this);
        if (migrationService.needsMigration()) {
            logger.warn("Legacy MilestoneMP data detected. Running automatic migration...");
            boolean success = migrationService.migrate();
            if (!success) {
                logger.severe("Automatic migration FAILED. Use /progresstree migrate to retry manually.");
            }
        }

        // Repository & managers
        repository = new ProgressRepository(this);
        milestoneManager = new MilestoneManager(this);

        // Category registry (loads gui.categories from config, resolves milestones)
        categoryRegistry = new CategoryRegistry(this);
        categoryRegistry.load();

        // Command registration
        ProgressTreeCommand command = new ProgressTreeCommand(this);
        getCommand("progresstree").setExecutor(command);
        getCommand("progresstree").setTabCompleter(new ProgressTreeTabCompleter(this));

        // Event listeners — all triggers go through ProgressUpdateEvent (fix #1)
        getServer().getPluginManager().registerEvents(new EventListeners(this), this);
        getServer().getPluginManager().registerEvents(new ProgressUpdateListener(this), this);

        // GUI click/drag handlers
        ProgressTreeGUI gui = new ProgressTreeGUI(this);
        CategoryHubGUI hubGui = new CategoryHubGUI(this);
        ChoiceGUI choiceGUI = command.getChoiceGUI();

        String guiTitle = configManager.getGuiTitle();
        String hubTitle = com.github.Syaaddd.progresstree.util.MessageUtil.color("&8ProgressTree");
        String choiceTitlePrefix = com.github.Syaaddd.progresstree.util.MessageUtil.color("&8Choose Reward - ");

        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
                String title = event.getView().getTitle();
                if (title.equals(hubTitle)) {
                    event.setCancelled(true);
                    event.setResult(org.bukkit.event.Event.Result.DENY);
                    if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) return;
                    hubGui.handleClick(player, event.getSlot());
                } else if (title.equals(guiTitle) || title.startsWith(choiceTitlePrefix)) {
                    event.setCancelled(true);
                    event.setResult(org.bukkit.event.Event.Result.DENY);
                    if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) return;
                    if (title.startsWith(choiceTitlePrefix)) {
                        String milestoneId = title.substring(choiceTitlePrefix.length());
                        choiceGUI.handleChoice(player, milestoneId, event.getSlot());
                    } else {
                        gui.handleClick(player, event.getSlot());
                    }
                }
            }
        }, this);

        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
                String title = event.getView().getTitle();
                if (title.equals(hubTitle) || title.equals(guiTitle) || title.startsWith(choiceTitlePrefix)) {
                    event.setCancelled(true);
                    event.setResult(org.bukkit.event.Event.Result.DENY);
                }
            }
        }, this);

        // Playtime tracker
        PlaytimeTracker playtimeTracker = new PlaytimeTracker(this);
        playtimeTracker.start();

        // PlaceholderAPI
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            logger.info("PlaceholderAPI integration enabled.");
        }

        logger.info("ProgressTree enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (repository != null) {
            repository.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (logger != null) {
            logger.info("ProgressTree disabled.");
        }
    }

    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public ProgressRepository getRepository() { return repository; }
    public MilestoneManager getMilestoneManager() { return milestoneManager; }
    public CategoryRegistry getCategoryRegistry() { return categoryRegistry; }
    /** Custom leveled logger (NOT java.util.logging — use getLog() to avoid collision with JavaPlugin.getLogger()). */
    public Logger getLog() { return logger; }
}