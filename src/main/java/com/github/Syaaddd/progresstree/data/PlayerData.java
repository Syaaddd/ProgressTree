package com.github.Syaaddd.progresstree.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int playtimeSeconds;
    private int blocksBroken;
    private int blocksPlaced;
    private int mobsKilled;
    private int playersKilled;
    private int joinDays;
    private long lastJoinTime;
    private Map<String, String> claimedMilestones;
    /** Fix #4: tracks which milestones have already been notified to prevent spam */
    private Set<String> notifiedMilestones;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.playtimeSeconds = 0;
        this.blocksBroken = 0;
        this.blocksPlaced = 0;
        this.mobsKilled = 0;
        this.playersKilled = 0;
        this.joinDays = 0;
        this.lastJoinTime = System.currentTimeMillis();
        this.claimedMilestones = new HashMap<>();
        this.notifiedMilestones = new HashSet<>();
    }

    public UUID getUuid() { return uuid; }
    public int getPlaytimeSeconds() { return playtimeSeconds; }
    public void setPlaytimeSeconds(int v) { this.playtimeSeconds = v; }
    public void addPlaytime(int seconds) { this.playtimeSeconds += seconds; }

    public int getBlocksBroken() { return blocksBroken; }
    public void setBlocksBroken(int v) { this.blocksBroken = v; }
    public void addBlockBreak(int amount) { this.blocksBroken += amount; }

    public int getBlocksPlaced() { return blocksPlaced; }
    public void setBlocksPlaced(int v) { this.blocksPlaced = v; }
    public void addBlockPlace(int amount) { this.blocksPlaced += amount; }

    public int getMobsKilled() { return mobsKilled; }
    public void setMobsKilled(int v) { this.mobsKilled = v; }
    public void addMobKill(int amount) { this.mobsKilled += amount; }

    public int getPlayersKilled() { return playersKilled; }
    public void setPlayersKilled(int v) { this.playersKilled = v; }
    public void addPlayerKill(int amount) { this.playersKilled += amount; }

    public int getJoinDays() { return joinDays; }
    public void setJoinDays(int v) { this.joinDays = v; }

    public long getLastJoinTime() { return lastJoinTime; }
    public void setLastJoinTime(long v) { this.lastJoinTime = v; }

    public Map<String, String> getClaimedMilestones() { return claimedMilestones; }
    public void setClaimedMilestones(Map<String, String> m) { this.claimedMilestones = m; }
    public void claimMilestone(String milestoneId, String choiceId) { this.claimedMilestones.put(milestoneId, choiceId); }
    public boolean hasClaimed(String milestoneId) { return claimedMilestones.containsKey(milestoneId); }
    public String getClaimedChoice(String milestoneId) { return claimedMilestones.get(milestoneId); }

    // === Notification tracking (Fix #4) ===
    public Set<String> getNotifiedMilestones() { return notifiedMilestones; }
    public void setNotifiedMilestones(Set<String> s) { this.notifiedMilestones = s; }
    public boolean isNotified(String milestoneId) { return notifiedMilestones.contains(milestoneId); }
    public void markNotified(String milestoneId) { notifiedMilestones.add(milestoneId); }
}
