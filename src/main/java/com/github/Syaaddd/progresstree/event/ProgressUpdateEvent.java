package com.github.Syaaddd.progresstree.event;

import com.github.Syaaddd.progresstree.milestone.MilestoneType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Fired whenever a player's progress counter changes.
 * All activity types funnel through this single event so that
 * completion checking and notification logic is identical regardless of trigger.
 * 
 * Fixes: "Notifikasi milestone tidak muncul di beberapa jenis aktivitas" (v1.0.7 root cause).
 */
public class ProgressUpdateEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final MilestoneType type;
    private final int delta;

    public ProgressUpdateEvent(Player who, MilestoneType type, int delta) {
        super(who);
        this.type = type;
        this.delta = delta;
    }

    public MilestoneType getType() { return type; }
    public int getDelta() { return delta; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
