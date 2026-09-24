package com.github.Syaaddd.progresstree.gui;

import com.github.Syaaddd.progresstree.ProgressTree;
import com.github.Syaaddd.progresstree.milestone.Milestone;
import com.github.Syaaddd.progresstree.milestone.MilestoneChoice;
import com.github.Syaaddd.progresstree.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reward selection screen. Opened from the category tree; claiming a
 * choice re-opens the SAME category + page (context travels with the
 * GuiHolder, never lost to title matching).
 *
 * Choices are centered on the middle row (max 9 per row, overflow to
 * the top row). Uses its own slot math, NOT the 54-slot tree layout:
 * the old code reused getMilestoneSlots() whose pyramid slots reach
 * 42, which is out of range for this 27-slot inventory.
 */
public class ChoiceGUI {

    private static final int SIZE = 27;
    private static final int BACK_SLOT = 22;

    private final ProgressTree plugin;

    public ChoiceGUI(ProgressTree plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String milestoneId, String categoryId, int page) {
        Milestone milestone = plugin.getConfigManager().getMilestone(milestoneId);
        if (milestone == null || !milestone.hasChoices()) {
            plugin.getMilestoneManager().claimMilestone(player, milestoneId, null);
            return;
        }

        GuiHolder holder = GuiHolder.choice(milestoneId, categoryId, page);
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                MessageUtil.toComponent("&8Choose Reward &8\u00BB &7" + milestone.getId().replace('_', ' ')));
        holder.attach(inv);

        fillBackground(inv);

        // Back button: bottom center, outside both the choice row and the overflow row
        ItemStack back = new ItemStack(Material.OAK_DOOR);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(MessageUtil.color("&e&l\u00AB Back"));
        bm.setLore(Collections.singletonList(MessageUtil.color("&7Return to the milestone tree")));
        back.setItemMeta(bm);
        inv.setItem(BACK_SLOT, back);

        List<MilestoneChoice> choices = milestone.getChoices();
        int[] slots = choiceSlots(choices.size());
        if (choices.size() > slots.length) {
            plugin.getLog().warn("[GUI] Milestone '" + milestoneId + "' has " + choices.size()
                    + " choices but only " + slots.length + " slots available - extras hidden.");
        }
        for (int i = 0; i < slots.length; i++) {
            inv.setItem(slots[i], createChoiceItem(choices.get(i)));
        }

        player.openInventory(inv);
    }

    /** Handle a click in a choice inventory (event already cancelled by the listener). */
    public void handleChoice(Player player, Inventory inv, int slot) {
        if (!(inv.getHolder() instanceof GuiHolder h) || h.kind() != GuiHolder.Kind.CHOICE) {
            return;
        }
        if (slot == BACK_SLOT) {
            plugin.getTreeGui().open(player, h.categoryId(), h.page());
            return;
        }
        String milestoneId = h.milestoneId();
        Milestone milestone = plugin.getConfigManager().getMilestone(milestoneId);
        if (milestone == null || !milestone.hasChoices()) return;

        List<MilestoneChoice> choices = milestone.getChoices();
        int[] slots = choiceSlots(choices.size());

        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i]) {
                MilestoneChoice choice = choices.get(i);
                plugin.getMilestoneManager().claimMilestone(player, milestoneId, choice.getId());
                // Return to the exact tree page we came from
                plugin.getTreeGui().open(player, h.categoryId(), h.page());
                return;
            }
        }
    }

    /**
     * Centered middle-row slots (row 1 as overflow), max 9 per row.
     * Capacity is 18 (27 slots minus borders and the back button); if a
     * milestone defines more choices, only the first 18 are shown.
     * Every returned slot is unique and inside 0..26.
     */
    private int[] choiceSlots(int count) {
        int capacity = Math.min(count, 18);
        int[] slots = new int[capacity];
        int placed = 0;
        int mainCount = Math.min(capacity, 9);
        int start = 13 - (mainCount - 1) / 2;
        for (int i = 0; i < mainCount; i++) {
            slots[placed++] = start + i;
        }
        if (placed < capacity) {
            int overCount = capacity - placed;
            int overStart = 4 - (overCount - 1) / 2;
            for (int i = 0; i < overCount; i++) {
                slots[placed++] = overStart + i;
            }
        }
        return slots;
    }

    private ItemStack createChoiceItem(MilestoneChoice choice) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.color("&e" + choice.getName()));
        meta.setLore(Collections.singletonList(MessageUtil.color("&7Click to claim this reward")));
        item.setItemMeta(meta);
        return item;
    }

    private void fillBackground(Inventory inv) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < inv.getSize(); i++) {
            if (i == BACK_SLOT) continue;
            inv.setItem(i, filler);
        }
    }
}
