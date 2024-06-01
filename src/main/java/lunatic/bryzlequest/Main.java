package lunatic.bryzlequest;

import lunatic.bryzlequest.command.QuestCommand;
import lunatic.bryzlequest.data.FileManager;
import lunatic.bryzlequest.db.DbConnection;
import lunatic.bryzlequest.gui.BryzleGUI;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private DbConnection dbConnection;
    public FileManager config;
    public BryzleGUI gui;
    private QuestDialogue questDialogue;

    @Override
    public void onEnable() {
        gui = new BryzleGUI(this);
        config = new FileManager(this);
        saveDefaultConfig(); // Save the default config.yml from resources if it doesn't exist
        config.getConfig("dialogue.yml").saveDefaultConfig();
        questDialogue = new QuestDialogue(this);

        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port");
        String database = getConfig().getString("database.database");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");

        dbConnection = new DbConnection(host, port, database, username, password);
        dbConnection.createTable();

        this.getCommand("bryzle").setExecutor(new QuestCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public QuestDialogue getQuestDialogue() {
        return questDialogue;
    }

    public DbConnection getDbConnection() {
        return dbConnection;
    }
}
