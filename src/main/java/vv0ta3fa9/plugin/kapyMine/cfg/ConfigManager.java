package vv0ta3fa9.plugin.kapyMine.cfg;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vv0ta3fa9.plugin.kapyMine.KapyMine;
import vv0ta3fa9.plugin.kapyMine.util.ColorUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private final KapyMine plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public ConfigManager(KapyMine plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        // Мёрдж с дефолтными значениями из jar
        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            messages.setDefaults(YamlConfiguration.loadConfiguration(
                new InputStreamReader(defStream, StandardCharsets.UTF_8)));
        }
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public void saveMessages() {
        try {
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить messages.yml: " + e.getMessage());
        }
    }

    public String getMessage(String path) {
        String prefix = ColorUtil.colorize(messages.getString("messages.prefix", "&8[&6KapyMine&8] "));
        String msg = messages.getString("messages." + path, "&cСообщение не найдено: " + path);
        return prefix + ColorUtil.colorize(msg);
    }

    public String getRawMessage(String path) {
        String msg = messages.getString("messages." + path, "&cСообщение не найдено: " + path);
        return ColorUtil.colorize(msg);
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}

