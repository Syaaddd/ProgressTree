package com.github.Syaaddd.progresstree.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Tags every ProgressTree inventory with its kind + navigation context.
 * Click/drag listeners route by holder instead of matching inventory
 * titles, so category hub / tree / choice screens can never conflict,
 * and navigation context travels WITH the inventory (no per-instance
 * state that could desync and fall back to a flat view).
 *
 * Each GUI inventory gets its OWN GuiHolder instance, created and
 * attached right after Bukkit.createInventory(...).
 */
public final class GuiHolder implements InventoryHolder {

    public enum Kind { HUB, TREE, CHOICE }

    private final Kind kind;
    private final String categoryId;
    private final int page;
    private final String milestoneId; // choice screens only
    private Inventory inventory;      // back-reference, set via attach()

    private GuiHolder(Kind kind, String categoryId, int page, String milestoneId) {
        this.kind = kind;
        this.categoryId = categoryId;
        this.page = page;
        this.milestoneId = milestoneId;
    }

    public static GuiHolder hub() { return new GuiHolder(Kind.HUB, null, 0, null); }
    public static GuiHolder tree(String categoryId, int page) { return new GuiHolder(Kind.TREE, categoryId, page, null); }
    public static GuiHolder choice(String milestoneId, String categoryId, int page) {
        return new GuiHolder(Kind.CHOICE, categoryId, page, milestoneId);
    }

    /** Must be called immediately after Bukkit.createInventory(..., this, ...). */
    public void attach(Inventory inv) {
        this.inventory = inv;
    }

    public Kind kind() { return kind; }
    public String categoryId() { return categoryId; }
    public int page() { return page; }
    public String milestoneId() { return milestoneId; }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
