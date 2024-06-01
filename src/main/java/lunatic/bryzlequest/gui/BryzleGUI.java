package lunatic.bryzlequest.gui;

import lunatic.bryzlequest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;
import org.bukkit.SkullType;
import org.bukkit.block.Skull;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class BryzleGUI implements Listener {
    private Main plugin;

    public BryzleGUI(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        // Create a new inventory with 27 slots
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Bryzle Quest Stats");

        // Add player head with stats
        inv.setItem(4, getPlayerHead(player));

        // Add quest section
        inv.setItem(10, getQuestItem(player, "EASY"));
        inv.setItem(12, getQuestItem(player, "MEDIUM"));
        inv.setItem(14, getQuestItem(player, "HARD"));
        inv.setItem(16, getQuestItem(player, "EXPERT"));

        player.openInventory(inv);
    }

    private ItemStack getPlayerHead(Player player) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.YELLOW + player.getName());

        Integer reputation = (Integer) plugin.getDbConnection().getColumnValue(player.getUniqueId().toString(), "bryzleReputation");
        Integer level = (Integer) plugin.getDbConnection().getColumnValue(player.getUniqueId().toString(), "level");
        Integer completedQuest = (Integer) plugin.getDbConnection().getColumnValue(player.getUniqueId().toString(), "completedQuest");

        // Set default values if any value is null
        reputation = (reputation != null) ? reputation : 0;
        level = (level != null) ? level : 1; // Assuming level 1 as the default level
        completedQuest = (completedQuest != null) ? completedQuest : 0;

        meta.setLore(Arrays.asList(
                ChatColor.GOLD + "Reputation: " + ChatColor.WHITE + reputation,
                ChatColor.GOLD + "Level: " + ChatColor.WHITE + level,
                ChatColor.GOLD + "Completed Quests: " + ChatColor.WHITE + completedQuest
        ));
        playerHead.setItemMeta(meta);
        return playerHead;
    }

    private ItemStack getQuestItem(Player player, String difficulty) {
        String playerUuid = player.getUniqueId().toString();
        String currentQuest = (String) plugin.getDbConnection().getColumnValue(playerUuid, "currentQuest");
        Integer itemAmount = (Integer) plugin.getDbConnection().getColumnValue(playerUuid, "itemAmount");
        String playerDifficulty = (String) plugin.getDbConnection().getColumnValue(playerUuid, "difficulty");

        Material material;
        try {
            material = Material.valueOf(currentQuest);
        } catch (IllegalArgumentException | NullPointerException e) {
            material = Material.BARRIER; // Default if no quest or invalid material
            currentQuest = "None";
            itemAmount = 0;
        }

        ItemStack questItem = new ItemStack(material);
        ItemMeta meta = questItem.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + difficulty + " Quest");

        meta.setLore(Arrays.asList(
                ChatColor.GOLD + "Current Quest: " + ChatColor.WHITE + currentQuest,
                ChatColor.GOLD + "Difficulty: " + ChatColor.WHITE + (playerDifficulty != null ? playerDifficulty : "None"),
                ChatColor.GOLD + "Item Amount: " + ChatColor.WHITE + (itemAmount != null ? itemAmount : 0)
        ));
        questItem.setItemMeta(meta);
        return questItem;
    }

    // Handle inventory click events to prevent item moving
    @org.bukkit.event.EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Bryzle Quest Stats")) {
            event.setCancelled(true);
        }
    }

    // Handle inventory close event if needed
    @org.bukkit.event.EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Bryzle Quest Stats")) {
            // Actions to perform when GUI is closed
        }
    }
}
