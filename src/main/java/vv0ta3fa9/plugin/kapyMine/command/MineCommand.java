package vv0ta3fa9.plugin.kapyMine.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import vv0ta3fa9.plugin.kapyMine.KapyMine;
import vv0ta3fa9.plugin.kapyMine.cfg.ConfigManager;
import vv0ta3fa9.plugin.kapyMine.listener.WandListener;
import vv0ta3fa9.plugin.kapyMine.mine.MineManager;

import java.util.List;

public class MineCommand implements CommandExecutor, TabCompleter {
    private final KapyMine plugin;
    private final ConfigManager configManager;
    private final MineManager mineManager;
    private final WandListener wandListener;

    public MineCommand(KapyMine plugin, ConfigManager configManager,
                       MineManager mineManager, WandListener wandListener) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.mineManager = mineManager;
        this.wandListener = wandListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("kapymine.use")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "refuse" -> handleRefuse(sender);
            case "wand" -> handleWand(sender);
            default -> sender.sendMessage(configManager.getMessage("unknown-command"));
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(configManager.getMessage("reload-success"));
    }

    private void handleRefuse(CommandSender sender) {
        if (!mineManager.hasRegion()) {
            sender.sendMessage(configManager.getMessage("no-region"));
            return;
        }
        if (!mineManager.hasMineTypes()) {
            sender.sendMessage(configManager.getMessage("no-mine-types"));
            return;
        }
        mineManager.forceRefill();
        sender.sendMessage(configManager.getMessage("refuse-success"));
    }

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("players-only"));
            return;
        }
        if (!player.hasPermission("kapymine.wand")) {
            player.sendMessage(configManager.getMessage("no-permission"));
            return;
        }
        wandListener.giveWand(player);
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§6KapyMine §7— автошахта");
        sender.sendMessage("§f/" + label + " reload §7— перезагрузить конфиг");
        sender.sendMessage("§f/" + label + " refuse §7— принудительно обновить шахту");
        sender.sendMessage("§f/" + label + " wand §7— получить палку выделения");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "refuse", "wand").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .toList();
        }
        return List.of();
    }
}

