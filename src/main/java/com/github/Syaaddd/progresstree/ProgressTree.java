package com.github.Syaaddd.progresstree;

import com.github.Syaaddd.progresstree.category.CategoryRegistry;
import com.github.Syaaddd.progresstree.command.ProgressTreeCommand;
import com.github.Syaaddd.progresstree.command.ProgressTreeTabCompleter;
import com.github.Syaaddd.progresstree.config.ConfigManager;
import com.github.Syaaddd.progresstree.data.DatabaseManager;
import com.github.Syaaddd.progresstree.data.ProgressRepository;
import com.github.Syaaddd.progresstree.data.MigrationService;
import com.github.Syaaddd.progresstree.gui.ChoiceGUI;
import com.github.Syaaddd.progresstree.gui.GuiHolder;
import com.github.Syaaddd.progresstree.gui.ProgressTreeGUI;
import com.github.Syaaddd.progresstree.gui.CategoryHubGUI;
import com.github.Syaaddd.progresstree.event.ProgressUpdateListener;
import com.github.Syaaddd.progresstree.listener.EventListeners;
import com.github.Syaaddd.progresstree.listener.PlaytimeTracker;
import com.github.Syaaddd.progresstree.milestone.MilestoneManager;
import com.github.Syaaddd.progresstree.placeholder.PlaceholderHook;
import com.github.Syaaddd.progresstree.util.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProgressTree extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ProgressRepository repository;
    private MilestoneManager milestoneManager;
    private CategoryRegistry categoryRegistry;
    private Logger logger;

    // Shared GUI instances — single source of truth for routing
    private ProgressTreeGUI treeGui;
    private CategoryHubGUI hubGui;
    private ChoiceGUI choiceGUI;

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

        // Shared GUI instances
        treeGui = new ProgressTreeGUI(this);
        hubGui = new CategoryHubGUI(this);
        choiceGUI = new ChoiceGUI(this);

        // Command registration
        getCommand("progresstree").setExecutor(new ProgressTreeCommand(this));
        getCommand("progresstree").setTabCompleter(new ProgressTreeTabCompleter(this));

        // Event listeners — all triggers go through ProgressUpdateEvent (fix #1)
        getServer().getPluginManager().registerEvents(new EventListeners(this), this);
        getServer().getPluginManager().registerEvents(new ProgressUpdateListener(this), this);

        // GUI click/drag handlers — route by InventoryHolder, NEVER by title
        getServer().getPluginManager().registerEvents(new GuiListener(), this);

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

    /**
     * Routes every inventory interaction through the GuiHolder attached to
     * the top inventory. All clicks/drags in ProgressTree GUIs are cancelled
     * first (items can never be taken), then dispatched to the owning GUI.
     */
    private final class GuiListener implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            GuiHolder holder = topHolder(event.getInventory());
            if (holder == null) return;

            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);

            if (!(event.getWhoClicked() instanceof Player player)) return;
            // Only react to clicks in the GUI (top) inventory, not the player's own
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;

            int slot = event.getSlot();
            switch (holder.kind()) {
                case HUB -> hubGui.handleClick(player, slot);
                case TREE -> treeGui.handleClick(player, event.getInventory(), slot);
                case CHOICE -> choiceGUI.handleChoice(player, event.getInventory(), slot);
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            GuiHolder holder = topHolder(event.getInventory());
            if (holder == null) return;
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
        }

        private GuiHolder topHolder(Inventory top) {
            if (top != null && top.getHolder() instanceof GuiHolder h) {
                return h;
            }
            return null;
        }
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
    public ProgressTreeGUI getTreeGui() { return treeGui; }
    public CategoryHubGUI getHubGui() { return hubGui; }
    public ChoiceGUI getChoiceGUI() { return choiceGUI; }
    /** Custom leveled logger (NOT java.util.logging — use getLog() to avoid collision with JavaPlugin.getLogger()). */
    public Logger getLog() { return logger; }
}
