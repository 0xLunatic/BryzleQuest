package lunatic.bryzlequest.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class FileManager {
    private final JavaPlugin plugin;
    private final HashMap<String, FileManager.Config> configs = new HashMap();

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileManager.Config getConfig(String name) {
        if (!this.configs.containsKey(name)) {
            this.configs.put(name, new FileManager.Config(name));
        }

        return (FileManager.Config)this.configs.get(name);
    }

    public FileManager.Config saveConfig(String name) {
        return this.getConfig(name).save();
    }

    public FileManager.Config reloadConfig(String name) {
        return this.getConfig(name).reload();
    }

    public class Config {
        private final String name;
        private File file;
        private YamlConfiguration config;

        public Config(String name) {
            this.name = name;
        }

        public FileManager.Config save() {
            if (this.config != null && this.file != null) {
                try {
                    if (((ConfigurationSection)Objects.requireNonNull(this.config.getConfigurationSection(""))).getKeys(true).size() != 0) {
                        this.config.save(this.file);
                    }
                } catch (IOException var2) {
                    var2.printStackTrace();
                }

                return this;
            } else {
                return this;
            }
        }

        public YamlConfiguration get() {
            if (this.config == null) {
                this.reload();
            }

            return this.config;
        }

        public FileManager.Config saveDefaultConfig() {
            this.file = new File(FileManager.this.plugin.getDataFolder(), this.name);
            FileManager.this.plugin.saveResource(this.name, false);
            return this;
        }

        public FileManager.Config reload() {
            if (this.file == null) {
                this.file = new File(FileManager.this.plugin.getDataFolder(), this.name);
            }

            this.config = YamlConfiguration.loadConfiguration(this.file);

            try {
                Reader defConfigStream = new InputStreamReader((InputStream)Objects.requireNonNull(FileManager.this.plugin.getResource(this.name)), StandardCharsets.UTF_8);
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                this.config.setDefaults(defConfig);
            } catch (NullPointerException var3) {
            }

            return this;
        }

        public FileManager.Config copyDefaults(boolean force) {
            this.get().options().copyDefaults(force);
            return this;
        }

        public FileManager.Config set(String key, Object value) {
            this.get().set(key, value);
            return this;
        }

        public Object get(String key) {
            return this.get().get(key);
        }
    }
    public FileManager.Config getDataFromDirectory(String directory, String name) {
        File fileOutsideDirectory = new File(directory, name);

        if (!fileOutsideDirectory.exists()) {
            // If the file does not exist, you may want to handle this accordingly.
            // For now, let's just print a message.
            System.out.println("File not found: " + fileOutsideDirectory.getAbsolutePath());
            return null;
        }

        FileManager.Config config = new FileManager.Config(name);
        config.file = fileOutsideDirectory;
        config.config = YamlConfiguration.loadConfiguration(config.file);

        try {
            Reader defConfigStream = new InputStreamReader(Objects.requireNonNull(FileManager.this.plugin.getResource(name)), StandardCharsets.UTF_8);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            config.config.setDefaults(defConfig);
        } catch (NullPointerException var3) {
            // Handle exception if necessary
        }

        return config;
    }

}