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

public class ChoiceGUI {

    private final ProgressTree plugin;

    public ChoiceGUI(ProgressTree plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String milestoneId) {
        Milestone milestone = plugin.getConfigManager().getMilestone(milestoneId);
        if (milestone == null || !milestone.hasChoices()) {
            plugin.getMilestoneManager().claimMilestone(player, milestoneId, null);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, 
            MessageUtil.color("&8Choose Reward - " + milestone.getId()));

        int[] slots = plugin.getConfigManager().getMilestoneSlots();
        
        for (int i = 0; i < milestone.getChoices().size() && i < slots.length; i++) {
            MilestoneChoice choice = milestone.getChoices().get(i);
            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            
            meta.setDisplayName(MessageUtil.color("&e" + choice.getName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Click to claim this reward"));
            lore.add(MessageUtil.color("&8Command: " + choice.getCommand()));
            meta.setLore(lore);
            
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        player.openInventory(inv);
    }

    public void handleChoice(Player player, String milestoneId, int slot) {
        Milestone milestone = plugin.getConfigManager().getMilestone(milestoneId);
        if (milestone == null || !milestone.hasChoices()) return;

        List<MilestoneChoice> choices = milestone.getChoices();
        int[] slots = plugin.getConfigManager().getMilestoneSlots();
        
        for (int i = 0; i < choices.size() && i < slots.length; i++) {
            if (slot == slots[i]) {
                MilestoneChoice choice = choices.get(i);
                player.closeInventory();
                plugin.getMilestoneManager().claimMilestone(player, milestoneId, choice.getId());
                return;
            }
        }
    }
}
