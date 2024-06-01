package lunatic.bryzlequest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public class QuestDialogue {
    private FileConfiguration dialogueConfig;
    private Main plugin;
    private String npcNameColorPrefix = "§6"; // Gold color for NPC name
    private String dialogueColorPrefix = "§f"; // Green color for dialogue
    private String itemColorPrefix = "§b"; // Aqua color for item name
    private String playerNameColorPrefix = "§e"; // Yellow color for player name
    private String colorReset = "§r"; // Reset color

    public QuestDialogue(Main plugin) {
        this.plugin = plugin;
        this.dialogueConfig = plugin.config.getConfig("dialogue.yml").get();
    }

    public String getRandomDialogue(String type, String difficulty) {
        String key = type + "." + difficulty.toLowerCase();
        List<String> dialogues = dialogueConfig.getStringList(key);
        if (dialogues != null && !dialogues.isEmpty()) {
            return dialogues.get(new Random().nextInt(dialogues.size()));
        } else {
            return "No dialogue available for this difficulty";
        }
    }

    public String getQuestAcceptDialogue(String playerName, String difficulty, Material material, int itemAmount) {
        String materialName = itemColorPrefix + capitalizeFirstLetter(material.toString().toLowerCase().replace("_", " ")) + colorReset;
        String npcName = npcNameColorPrefix + "Bʀʏᴢʟᴇ" + colorReset; // NPC name
        String dialogue = getRandomDialogue("accept", difficulty.toLowerCase());

        // Replace placeholders and format the dialogue
        return npcName + " » " + dialogueColorPrefix + dialogue.replace("%player%", playerNameColorPrefix + playerName + colorReset)
                .replace("%amount%", itemColorPrefix + String.valueOf(itemAmount))
                .replace("%material%", materialName) + colorReset;
    }

    public String getQuestProgressDialogue(String playerName, String difficulty, Material material, int remainingItems) {
        String materialName = itemColorPrefix + capitalizeFirstLetter(material.toString().toLowerCase().replace("_", " ")) + colorReset;
        String npcName = npcNameColorPrefix + "Bʀʏᴢʟᴇ" + colorReset; // NPC name
        String dialogue = getRandomDialogue("progress", difficulty.toLowerCase());
        return npcName + " » " + dialogueColorPrefix + dialogue.replace("%player%", playerNameColorPrefix + playerName + colorReset)
                .replace("%remaining%", itemColorPrefix + String.valueOf(remainingItems) + "§f")
                .replace("%material%", materialName) + colorReset;
    }

    public String getQuestCompletionDialogue(String playerName, String difficulty, Material material) {
        String materialName = itemColorPrefix + capitalizeFirstLetter(material.toString().toLowerCase().replace("_", " ")) + colorReset;
        String npcName = npcNameColorPrefix + "Bʀʏᴢʟᴇ" + colorReset; // NPC name
        String dialogue = getRandomDialogue("completion", difficulty.toLowerCase());
        return npcName + " » " + dialogueColorPrefix + dialogue.replace("%player%", playerNameColorPrefix + playerName + colorReset)
                .replace("%material%", materialName) + colorReset;
    }


    private String capitalizeFirstLetter(String str) {
        StringBuilder result = new StringBuilder();
        String[] words = str.split(" ");
        for (String word : words) {
            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }
        return result.toString().trim();
    }
}
