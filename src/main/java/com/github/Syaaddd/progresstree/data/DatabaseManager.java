package com.github.Syaaddd.progresstree.data;

import com.github.Syaaddd.progresstree.ProgressTree;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final ProgressTree plugin;
    private Connection connection;
    private boolean isSqlite;

    public DatabaseManager(ProgressTree plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String dbType = plugin.getConfigManager().getDatabaseType();
        isSqlite = dbType.equalsIgnoreCase("sqlite");

        if (isSqlite) {
            initializeSqlite();
        } else {
            initializeMySQL();
        }
    }

    private void initializeSqlite() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();
            File dbFile = new File(dataFolder, "progresstree.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
            plugin.getLog().info("SQLite database initialized.");
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to initialize SQLite: " + e.getMessage());
        }
    }

    private void initializeMySQL() {
        try {
            String host = plugin.getConfigManager().getDbHost();
            int port = plugin.getConfigManager().getDbPort();
            String database = plugin.getConfigManager().getDbDatabase();
            String username = plugin.getConfigManager().getDbUsername();
            String password = plugin.getConfigManager().getDbPassword();

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
            connection = DriverManager.getConnection(url, username, password);
            createTables();
            plugin.getLog().info("MySQL database initialized.");
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to initialize MySQL: " + e.getMessage());
        }
    }

    private void createTables() {
        String sqlPlayer = """
            CREATE TABLE IF NOT EXISTS player_data (
                uuid VARCHAR(36) PRIMARY KEY,
                playtime_seconds INTEGER DEFAULT 0,
                blocks_broken INTEGER DEFAULT 0,
                blocks_placed INTEGER DEFAULT 0,
                mobs_killed INTEGER DEFAULT 0,
                players_killed INTEGER DEFAULT 0,
                join_days INTEGER DEFAULT 0,
                last_join_time BIGINT DEFAULT 0
            )
            """;

        String sqlClaimed = """
            CREATE TABLE IF NOT EXISTS claimed_milestones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid VARCHAR(36),
                milestone_id VARCHAR(64),
                choice_id VARCHAR(64),
                UNIQUE(uuid, milestone_id)
            )
            """;

        // Fix #4: persistent notification tracking table
        String sqlNotified = """
            CREATE TABLE IF NOT EXISTS notified_milestones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid VARCHAR(36),
                milestone_id VARCHAR(64),
                UNIQUE(uuid, milestone_id)
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlPlayer);
            stmt.execute(sqlClaimed);
            stmt.execute(sqlNotified);
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to create tables: " + e.getMessage());
        }
    }

    /**
     * Fix #2: Async load with CompletableFuture. No .join() blocking main thread.
     * Callers should use thenAccept/thenApply on the returned future.
     */
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerData data = new PlayerData(uuid);

            String sql = "SELECT * FROM player_data WHERE uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    data.setPlaytimeSeconds(rs.getInt("playtime_seconds"));
                    data.setBlocksBroken(rs.getInt("blocks_broken"));
                    data.setBlocksPlaced(rs.getInt("blocks_placed"));
                    data.setMobsKilled(rs.getInt("mobs_killed"));
                    data.setPlayersKilled(rs.getInt("players_killed"));
                    data.setJoinDays(rs.getInt("join_days"));
                    data.setLastJoinTime(rs.getLong("last_join_time"));
                }
            } catch (SQLException e) {
                plugin.getLog().severe("Failed to load player data: " + e.getMessage());
            }

            loadClaimedMilestones(data);
            loadNotifiedMilestones(data);
            return data;
        });
    }

    private void loadClaimedMilestones(PlayerData data) {
        String sql = "SELECT milestone_id, choice_id FROM claimed_milestones WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.getClaimedMilestones().put(rs.getString("milestone_id"), rs.getString("choice_id"));
            }
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to load claimed milestones: " + e.getMessage());
        }
    }

    private void loadNotifiedMilestones(PlayerData data) {
        String sql = "SELECT milestone_id FROM notified_milestones WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.getNotifiedMilestones().add(rs.getString("milestone_id"));
            }
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to load notified milestones: " + e.getMessage());
        }
    }

    public void savePlayerData(PlayerData data) {
        String sql = """
            INSERT INTO player_data (uuid, playtime_seconds, blocks_broken, blocks_placed, mobs_killed, players_killed, join_days, last_join_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                playtime_seconds = excluded.playtime_seconds,
                blocks_broken = excluded.blocks_broken,
                blocks_placed = excluded.blocks_placed,
                mobs_killed = excluded.mobs_killed,
                players_killed = excluded.players_killed,
                join_days = excluded.join_days,
                last_join_time = excluded.last_join_time
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ps.setInt(2, data.getPlaytimeSeconds());
            ps.setInt(3, data.getBlocksBroken());
            ps.setInt(4, data.getBlocksPlaced());
            ps.setInt(5, data.getMobsKilled());
            ps.setInt(6, data.getPlayersKilled());
            ps.setInt(7, data.getJoinDays());
            ps.setLong(8, data.getLastJoinTime());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to save player data: " + e.getMessage());
        }
    }

    public void saveClaimedMilestone(UUID uuid, String milestoneId, String choiceId) {
        String sql = "INSERT OR IGNORE INTO claimed_milestones (uuid, milestone_id, choice_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, milestoneId);
            ps.setString(3, choiceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to save claimed milestone: " + e.getMessage());
        }
    }

    public void saveNotifiedMilestone(UUID uuid, String milestoneId) {
        String sql = "INSERT OR IGNORE INTO notified_milestones (uuid, milestone_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, milestoneId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to save notified milestone: " + e.getMessage());
        }
    }

    public int getTotalPlaytime() {
        String sql = "SELECT SUM(playtime_seconds) as total FROM player_data";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to get total playtime: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Locate the legacy MilestoneMP database. On a live server it lives in
     * plugins/MilestoneMP/milestonemp.db; admins may also have moved it into
     * plugins/ProgressTree/ manually. Check both.
     */
    public File findLegacyDatabaseFile() {
        File inNewFolder = new File(plugin.getDataFolder(), "milestonemp.db");
        if (inNewFolder.exists()) return inNewFolder;

        File pluginsFolder = plugin.getDataFolder().getParentFile();
        if (pluginsFolder != null) {
            File inOldFolder = new File(new File(pluginsFolder, "MilestoneMP"), "milestonemp.db");
            if (inOldFolder.exists()) return inOldFolder;
        }
        return null;
    }

    /** Check if legacy MilestoneMP database exists (for migration detection) */
    public boolean legacyDatabaseExists() {
        return findLegacyDatabaseFile() != null;
    }

    public Connection getConnection() { return connection; }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            plugin.getLog().severe("Failed to close database: " + e.getMessage());
        }
    }
}
