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
import java.util.List;

/**
 * Reward selection screen. Opened from the category tree; claiming a
 * choice re-opens the SAME category + page (context travels with the
 * GuiHolder, never lost to title matching).
 */
public class ChoiceGUI {

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
        Inventory inv = Bukkit.createInventory(holder, 27,
                MessageUtil.toComponent("&8Choose Reward - " + milestone.getId()));
        holder.attach(inv);

        // Choice slots: first N layout slots (single source of truth, no manual table)
        int[] slots = plugin.getConfigManager().getMilestoneSlots();

        for (int i = 0; i < milestone.getChoices().size() && i < slots.length; i++) {
            MilestoneChoice choice = milestone.getChoices().get(i);
            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(MessageUtil.color("&e" + choice.getName()));

            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Klik untuk mengambil reward ini"));
            meta.setLore(lore);

            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        player.openInventory(inv);
    }

    /** Handle a click in a choice inventory (event already cancelled by the listener). */
    public void handleChoice(Player player, Inventory inv, int slot) {
        if (!(inv.getHolder() instanceof GuiHolder h) || h.kind() != GuiHolder.Kind.CHOICE) {
            return;
        }
        String milestoneId = h.milestoneId();
        Milestone milestone = plugin.getConfigManager().getMilestone(milestoneId);
        if (milestone == null || !milestone.hasChoices()) return;

        List<MilestoneChoice> choices = milestone.getChoices();
        int[] slots = plugin.getConfigManager().getMilestoneSlots();

        for (int i = 0; i < choices.size() && i < slots.length; i++) {
            if (slot == slots[i]) {
                MilestoneChoice choice = choices.get(i);
                plugin.getMilestoneManager().claimMilestone(player, milestoneId, choice.getId());
                // Return to the exact tree page we came from
                plugin.getTreeGui().open(player, h.categoryId(), h.page());
                return;
            }
        }
    }
}
