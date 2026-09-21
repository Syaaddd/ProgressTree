package com.github.Syaaddd.progresstree.category;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.data.PlayerData;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.milestone.MilestoneManager;

import java.util.List;

/**
 * Per-player summary for a single category.
 * Computed on-demand from cached player data (no DB queries).
 */
public final class CategorySummary {

    private final String categoryId;
    private final int totalMilestones;
    private final int claimedCount;
    private final int claimableCount;
    private final Milestone nextMilestone; // null if all claimed
    private final double nextProgress;     // 0-100, 0 if no next

    public CategorySummary(String categoryId, int totalMilestones, int claimedCount,
                           int claimableCount, Milestone nextMilestone, double nextProgress) {
        this.categoryId = categoryId;
        this.totalMilestones = totalMilestones;
        this.claimedCount = claimedCount;
        this.claimableCount = claimableCount;
        this.nextMilestone = nextMilestone;
        this.nextProgress = nextProgress;
    }

    public String getCategoryId() { return categoryId; }
    public int getTotalMilestones() { return totalMilestones; }
    public int getClaimedCount() { return claimedCount; }
    public int getClaimableCount() { return claimableCount; }
    public Milestone getNextMilestone() { return nextMilestone; }
    public double getNextProgress() { return nextProgress; }

    public boolean isComplete() { return claimedCount >= totalMilestones && totalMilestones > 0; }
    public boolean hasClaimable() { return claimableCount > 0; }

    /**
     * Compute summary for a player and category.
     * @param plugin Main plugin instance
     * @param categoryId Category ID
     * @param milestones Sorted milestones for this category (by amount asc)
     * @param data Player data (may be null if not loaded yet)
     * @param manager Milestone manager for progress checks
     */
    public static CategorySummary compute(ProgressTree plugin, String categoryId,
                                          List<Milestone> milestones, PlayerData data,
                                          MilestoneManager manager) {
        if (milestones.isEmpty()) {
            return new CategorySummary(categoryId, 0, 0, 0, null, 0);
        }

        int claimed = 0;
        int claimable = 0;
        Milestone nextMs = null;
        double nextPct = 0;

        for (Milestone ms : milestones) {
            boolean isClaimed = data != null && data.hasClaimed(ms.getId());
            if (isClaimed) {
                claimed++;
            } else {
                boolean reached = data != null && manager.hasReached(data, ms);
                if (reached) {
                    claimable++;
                }
                // First unclaimed milestone is the "next" one
                if (nextMs == null) {
                    nextMs = ms;
                    nextPct = getProgress(data, ms);
                }
            }
        }

        return new CategorySummary(categoryId, milestones.size(), claimed, claimable, nextMs, nextPct);
    }

    private static double getProgress(PlayerData data, Milestone milestone) {
        if (data == null) return 0;
        int current = switch (milestone.getType()) {
            case PLAYTIME -> data.getPlaytimeSeconds();
            case BLOCK_BREAK -> data.getBlocksBroken();
            case BLOCK_PLACE -> data.getBlocksPlaced();
            case MOB_KILL -> data.getMobsKilled();
            case PLAYER_KILL -> data.getPlayersKilled();
            case JOIN -> data.getJoinDays();
            case COMMUNITY_PLAYTIME -> 0; // handled separately via repository
            default -> 0;
        };
        int required = milestone.getAmount();
        return Math.min(100.0, (double) current / required * 100);
    }
}