package lunatic.bryzlequest.command;

import lunatic.bryzlequest.Main;
import lunatic.bryzlequest.db.DbConnection;
import lunatic.bryzlequest.QuestDialogue;
import lunatic.bryzlequest.gui.BryzleGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.stream.Collectors;

public class QuestCommand implements CommandExecutor {
    private Main plugin;

    public QuestCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length >= 1) {
                String subCommand = args[0].toLowerCase();
                if (subCommand.equals("gui")){
                    plugin.gui.openGUI(player);
                }
                if (subCommand.equals("accept")) {
                    String arg = args[1].toUpperCase();
                    if (isValidDifficulty(arg)) {
                        createOrUpdateTable();
                        if (plugin.getDbConnection().getColumnValue(player.getUniqueId().toString(), "currentQuest") == null) {
                            acceptQuest(plugin.getDbConnection(), player, arg);
                        } else {
                            progressQuest(plugin.getDbConnection(), player);
                        }
                    } else {
                        player.sendMessage("Invalid difficulty specified. Available difficulties: EASY, MEDIUM, HARD, EXPERT");
                    }
                } else if (subCommand.equals("reload")) {
                    plugin.reloadConfig();
                    plugin.config.getConfig("dialogue.yml").reload();
                    player.sendMessage("Dialogue config reloaded.");
                } else {
                    player.sendMessage("Invalid subcommand. Use /bryzle <accept|deny|abandon|turnin> <material>");
                }
            } else {
                player.sendMessage("Usage: /bryzle <accept|deny|abandon|turnin> <material>");
            }
        } else {
            sender.sendMessage("This command can only be executed by a player.");
        }
        return true;
    }

    private boolean isValidDifficulty(String difficulty) {
        return difficulty.equalsIgnoreCase("EASY") ||
                difficulty.equalsIgnoreCase("MEDIUM") ||
                difficulty.equalsIgnoreCase("HARD") ||
                difficulty.equalsIgnoreCase("EXPERT");
    }

    private void createOrUpdateTable() {
        DbConnection db = plugin.getDbConnection();
        db.createTable();
    }

    private Material getRandomQuestMaterial(String difficulty) {
        Material[] materials = Material.values();

        List<Material> obtainableMaterials = Arrays.stream(materials)
                .filter(Material::isItem)
                .filter(material -> material.isItem() && !material.isAir() && material.getMaxStackSize() > 1 && material.isBlock() || material.isEdible() || material.isFuel()) // Filtering obtainable materials
                .collect(Collectors.toList());

        Random random = new Random();
        Material randomMaterial = obtainableMaterials.get(random.nextInt(obtainableMaterials.size()));

        return randomMaterial;
    }


    private int getRandomItemAmount(String difficulty) {
        Map<String, ItemRange> difficultyItemRanges = new HashMap<>();
        difficultyItemRanges.put("EASY", new ItemRange(300, 400));
        difficultyItemRanges.put("MEDIUM", new ItemRange(450, 600));
        difficultyItemRanges.put("HARD", new ItemRange(700, 1000));
        difficultyItemRanges.put("EXPERT", new ItemRange(1200, 1500));

        ItemRange itemRange = difficultyItemRanges.get(difficulty.toUpperCase());
        if (itemRange != null) {
            return itemRange.getRandomAmount();
        } else {
            // Default item amount if difficulty is not recognized
            return 0;
        }
    }

    private static class ItemRange {
        private final int min;
        private final int max;

        public ItemRange(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getRandomAmount() {
            return new Random().nextInt((max - min) + 1) + min;
        }
    }

    private void acceptQuest(DbConnection db, Player player, String difficulty) {
        Material questMaterial = getRandomQuestMaterial(difficulty);
        int itemAmount = getRandomItemAmount(difficulty);

        // Register the player in the database
        db.insertPlayer(player.getUniqueId().toString(), player.getName(), difficulty, questMaterial, itemAmount, 1, 0, 0);

        // Create an instance of QuestDialogue
        QuestDialogue questDialogue = new QuestDialogue(plugin);

        // Call the non-static method on the instance
        String acceptDialogue = questDialogue.getQuestAcceptDialogue(player.getName(), difficulty, questMaterial, itemAmount);
        player.sendMessage(acceptDialogue);
    }

    private void progressQuest(DbConnection db, Player player) {
        // Retrieve player's quest information from the database
        String playerUuid = player.getUniqueId().toString();
        String currentQuest = (String) db.getColumnValue(playerUuid, "currentQuest");
        Integer itemAmount = (Integer) db.getColumnValue(playerUuid, "itemAmount");
        String difficulty = (String) db.getColumnValue(playerUuid, "difficulty");

        // Check if the player has an active quest and the required values are not null
        if (currentQuest != null && itemAmount != null && itemAmount > 0 && difficulty != null) {
            Material questMaterial;
            try {
                questMaterial = Material.valueOf(currentQuest);
            } catch (IllegalArgumentException e) {
                player.sendMessage("The quest material specified in the database is invalid.");
                return;
            }

            int playerItemCount = countItems(player.getInventory(), questMaterial);

            // Check if the player has enough items to progress the quest
            if (playerItemCount >= 1) {
                // Calculate the new item amount needed after subtracting player's items
                int newItemAmount = Math.max(itemAmount - playerItemCount, 0);

                // Subtract the items from the player's inventory
                removeItems(player.getInventory(), questMaterial, Math.min(playerItemCount, itemAmount));

                // Update the item amount in the database
                db.setColumnValue(playerUuid, "itemAmount", newItemAmount);

                // Create an instance of QuestDialogue
                QuestDialogue questDialogue = new QuestDialogue(plugin);

                // Call the non-static method on the instance to get the progression dialogue
                if (newItemAmount != 0) {
                    String progressionDialogue = questDialogue.getQuestProgressDialogue(player.getName(), difficulty, questMaterial, newItemAmount);
                    player.sendMessage(progressionDialogue);
                }else{
                    completeQuest(db, player);
                }
            } else {
                QuestDialogue questDialogue = new QuestDialogue(plugin);
                String progressionDialogue = questDialogue.getQuestProgressDialogue(player.getName(), difficulty, questMaterial, itemAmount);
                player.sendMessage(progressionDialogue);
            }
        } else {
            player.sendMessage("You don't have an active quest or your quest is completed.");
        }
    }

    private void completeQuest(DbConnection db, Player player) {
        String playerUuid = player.getUniqueId().toString();
        String currentQuest = (String) db.getColumnValue(playerUuid, "currentQuest");
        String difficulty = (String) db.getColumnValue(playerUuid, "difficulty");

        if (currentQuest != null && difficulty != null) {
            Material questMaterial;
            try {
                questMaterial = Material.valueOf(currentQuest);
            } catch (IllegalArgumentException e) {
                player.sendMessage("The quest material specified in the database is invalid.");
                return;
            }

            // Increase reputation and completed quest count based on difficulty
            int reputationIncrease;
            String command;
            switch (difficulty.toUpperCase()) {
                case "EASY":
                    reputationIncrease = 20;
                    command = "heal";
                    break;
                case "MEDIUM":
                    reputationIncrease = 30;
                    command = "fix";
                    break;
                case "HARD":
                    reputationIncrease = 50;
                    command = "he";
                    break;
                case "EXPERT":
                    reputationIncrease = 100;
                    command = "damn";
                    break;
                default:
                    player.sendMessage("Invalid quest difficulty.");
                    return;
            }

            db.addBryzleReputation(playerUuid, reputationIncrease);

            // Execute the command
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command + " " + player.getName());

            // Create an instance of QuestDialogue
            QuestDialogue questDialogue = new QuestDialogue(plugin);

            // Send the completion dialogue to the player
            String completionDialogue = questDialogue.getQuestCompletionDialogue(player.getName(), difficulty, questMaterial);
            player.sendMessage(completionDialogue);

            // Reset quest information
            db.setColumnValue(playerUuid, "currentQuest", null);
            db.setColumnValue(playerUuid, "itemAmount", 0);
            db.setColumnValue(playerUuid, "difficulty", null);
        } else {
            player.sendMessage("You don't have an active quest or your quest is completed.");
        }
    }


    private int countItems(PlayerInventory inventory, Material material) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItems(PlayerInventory inventory, Material material, int amount) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= amount) {
                    inventory.removeItem(item);
                    amount -= itemAmount;
                } else {
                    item.setAmount(itemAmount - amount);
                    break;
                }
            }
        }
    }
}