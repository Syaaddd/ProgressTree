package com.github.Syaaddd.progresstree.data;

import com.github.Syaaddd.progresstree.ProgressTree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

/**
 * Handles automatic and manual migration from MilestoneMP to ProgressTree.
 * 
 * PRD Section 6 requirements:
 * 1. Auto-detect legacy data at startup
 * 2. Backup before migrating
 * 3. Manual fallback command (/progresstree migrate)
 * 4. Summary report after migration
 */
public class MigrationService {

    private final ProgressTree plugin;

    public MigrationService(ProgressTree plugin) {
        this.plugin = plugin;
    }

    public boolean needsMigration() {
        return plugin.getDatabaseManager().legacyDatabaseExists();
    }

    public boolean migrate() {
        File legacyDb = plugin.getDatabaseManager().findLegacyDatabaseFile();
        if (legacyDb == null || !legacyDb.exists()) {
            plugin.getLog().warn("No legacy database found to migrate.");
            return false;
        }

        // Step 1: Backup
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) backupDir.mkdirs();
        File backupFile = new File(backupDir, "milestonemp_backup_" + System.currentTimeMillis() + ".db");
        try {
            Files.copy(legacyDb.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLog().info("Backup created: " + backupFile.getName());
        } catch (IOException e) {
            plugin.getLog().severe("Failed to create backup: " + e.getMessage());
            return false;
        }

        // Step 2: Migrate data
        int playersMigrated = 0;
        int claimsMigrated = 0;

        try (Connection legacyConn = DriverManager.getConnection("jdbc:sqlite:" + legacyDb.getAbsolutePath())) {
            Connection newConn = plugin.getDatabaseManager().getConnection();

            // Migrate player_data
            try (Statement stmt = legacyConn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM player_data")) {
                String insertSql = """
                    INSERT OR IGNORE INTO player_data (uuid, playtime_seconds, blocks_broken, blocks_placed, 
                        mobs_killed, players_killed, join_days, last_join_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                try (PreparedStatement ps = newConn.prepareStatement(insertSql)) {
                    while (rs.next()) {
                        ps.setString(1, rs.getString("uuid"));
                        ps.setInt(2, rs.getInt("playtime_seconds"));
                        ps.setInt(3, rs.getInt("blocks_broken"));
                        ps.setInt(4, rs.getInt("blocks_placed"));
                        ps.setInt(5, rs.getInt("mobs_killed"));
                        ps.setInt(6, rs.getInt("players_killed"));
                        ps.setInt(7, rs.getInt("join_days"));
                        ps.setLong(8, rs.getLong("last_join_time"));
                        ps.executeUpdate();
                        playersMigrated++;
                    }
                }
            }

            // Migrate claimed_milestones
            try (Statement stmt = legacyConn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT uuid, milestone_id, choice_id FROM claimed_milestones")) {
                String insertSql = "INSERT OR IGNORE INTO claimed_milestones (uuid, milestone_id, choice_id) VALUES (?, ?, ?)";
                try (PreparedStatement ps = newConn.prepareStatement(insertSql)) {
                    while (rs.next()) {
                        ps.setString(1, rs.getString("uuid"));
                        ps.setString(2, rs.getString("milestone_id"));
                        ps.setString(3, rs.getString("choice_id"));
                        ps.executeUpdate();
                        claimsMigrated++;
                    }
                }
            }

            // Migrate notified_milestones if table exists in legacy (may not exist in old versions)
            try (Statement stmt = legacyConn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT uuid, milestone_id FROM notified_milestones")) {
                String insertSql = "INSERT OR IGNORE INTO notified_milestones (uuid, milestone_id) VALUES (?, ?)";
                try (PreparedStatement ps = newConn.prepareStatement(insertSql)) {
                    while (rs.next()) {
                        ps.setString(1, rs.getString("uuid"));
                        ps.setString(2, rs.getString("milestone_id"));
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException ignored) {
                // Table may not exist in legacy - that's fine
                plugin.getLog().debug("No notified_milestones table in legacy DB (expected for pre-2.0).");
            }

        } catch (SQLException e) {
            plugin.getLog().severe("Migration failed: " + e.getMessage());
            return false;
        }

        // Step 3: Rename legacy DB so it won't trigger again
        File migratedMarker = new File(plugin.getDataFolder(), "milestonemp.db.migrated");
        try {
            Files.move(legacyDb.toPath(), migratedMarker.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLog().warn("Could not rename legacy DB (migration succeeded but legacy file remains): " + e.getMessage());
        }

        // Step 4: Summary
        plugin.getLog().info("=== Migration Complete ===");
        plugin.getLog().info("Players migrated: " + playersMigrated);
        plugin.getLog().info("Claims migrated: " + claimsMigrated);
        plugin.getLog().info("Backup: " + backupFile.getName());
        plugin.getLog().info("==========================");

        return true;
    }
}
