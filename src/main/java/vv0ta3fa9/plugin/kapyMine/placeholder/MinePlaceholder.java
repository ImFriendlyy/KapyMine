package vv0ta3fa9.plugin.kapyMine.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import vv0ta3fa9.plugin.kapyMine.KapyMine;
import vv0ta3fa9.plugin.kapyMine.cfg.ConfigManager;
import vv0ta3fa9.plugin.kapyMine.mine.MineManager;
import vv0ta3fa9.plugin.kapyMine.mine.MineType;

public class MinePlaceholder extends PlaceholderExpansion {
    private final KapyMine plugin;
    private final ConfigManager configManager;
    private final MineManager mineManager;

    public MinePlaceholder(KapyMine plugin, ConfigManager configManager, MineManager mineManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.mineManager = mineManager;
    }

    @Override public String getIdentifier() { return "kapymine"; }
    @Override public String getAuthor() { return "vv0ta3fa9"; }
    @Override public String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        // %kapymine_time% время до обновления
        if (params.equalsIgnoreCase("time")) {
            long ms = mineManager.getMillisUntilRefill();
            long totalSec = ms / 1000;
            long min = totalSec / 60;
            long sec = totalSec % 60;
            return configManager.getRawMessage("placeholder-time")
                .replace("%min%", String.valueOf(min))
                .replace("%sec%", String.valueOf(sec));
        }
        // %kapymine_next_type% следующий тип шахты
        if (params.equalsIgnoreCase("next_type")) {
            MineType next = mineManager.getNextType();
            String typeName = next != null ? next.getName() : "—";
            return configManager.getRawMessage("placeholder-next-type")
                .replace("%type%", typeName);
        }
        return null;
    }
}

