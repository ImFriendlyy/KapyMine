package vv0ta3fa9.plugin.kapyMine;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import vv0ta3fa9.plugin.kapyMine.command.MineCommand;
import vv0ta3fa9.plugin.kapyMine.cfg.ConfigManager;
import vv0ta3fa9.plugin.kapyMine.listener.WandListener;
import vv0ta3fa9.plugin.kapyMine.mine.MineManager;
import vv0ta3fa9.plugin.kapyMine.placeholder.MinePlaceholder;

public final class KapyMine extends JavaPlugin {
    private ConfigManager configManager;
    private MineManager mineManager;
    private WandListener wandListener;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadAll();

        mineManager = new MineManager(this, configManager);
        mineManager.load();

        wandListener = new WandListener(this, configManager, mineManager);

        MineCommand mineCommand = new MineCommand(this, configManager, mineManager, wandListener);
        getCommand("kapymine").setExecutor(mineCommand);
        getCommand("kapymine").setTabCompleter(mineCommand);

        Bukkit.getPluginManager().registerEvents(wandListener, this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MinePlaceholder(this, configManager, mineManager).register();
            getLogger().info("PlaceholderAPI подключён.");
        }

        mineManager.start();
        getLogger().info("KapyMine запущен.");
    }

    @Override
    public void onDisable() {
        mineManager.stop();
        getLogger().info("KapyMine остановлен.");
    }

    public void reload() {
        mineManager.stop();
        configManager.loadAll();
        mineManager.load();
        mineManager.start();
    }
}
