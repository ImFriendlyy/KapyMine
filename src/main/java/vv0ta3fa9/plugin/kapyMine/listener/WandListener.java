package vv0ta3fa9.plugin.kapyMine.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vv0ta3fa9.plugin.kapyMine.KapyMine;
import vv0ta3fa9.plugin.kapyMine.cfg.ConfigManager;
import vv0ta3fa9.plugin.kapyMine.mine.MineManager;
import vv0ta3fa9.plugin.kapyMine.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WandListener implements Listener {
    private static final String WAND_TAG = "KapyMineWand";

    private final KapyMine plugin;
    private final ConfigManager configManager;
    private final MineManager mineManager;

    private final Map<UUID, Location> pos1Map = new HashMap<>();

    public WandListener(KapyMine plugin, ConfigManager configManager, MineManager mineManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.mineManager = mineManager;
    }

    public void giveWand(Player player) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize("&6KapyMine Wand"));
        meta.setLore(java.util.List.of(
            ColorUtil.colorize("&7ЛКМ &f— первая точка"),
            ColorUtil.colorize("&7ПКМ &f— вторая точка")
        ));
        meta.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, WAND_TAG),
            org.bukkit.persistence.PersistentDataType.BYTE,
            (byte) 1
        );
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(configManager.getMessage("wand-given"));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!isWand(item)) return;
        if (event.getClickedBlock() == null) return;
        event.setCancelled(true);
        Location loc = event.getClickedBlock().getLocation();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            pos1Map.put(player.getUniqueId(), loc);
            player.sendMessage(configManager.getMessage("pos1-set")
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ())));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location pos1 = pos1Map.get(player.getUniqueId());
            if (pos1 == null) {
                player.sendMessage(configManager.getMessage("pos1-not-set"));
                return;
            }
            mineManager.setRegion(pos1, loc);
            pos1Map.remove(player.getUniqueId());
            player.sendMessage(configManager.getMessage("coords-saved")
                .replace("%x1%", String.valueOf(pos1.getBlockX()))
                .replace("%y1%", String.valueOf(pos1.getBlockY()))
                .replace("%z1%", String.valueOf(pos1.getBlockZ()))
                .replace("%x2%", String.valueOf(loc.getBlockX()))
                .replace("%y2%", String.valueOf(loc.getBlockY()))
                .replace("%z2%", String.valueOf(loc.getBlockZ())));
        }
    }

    private boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) return false;
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey(plugin, WAND_TAG),
            org.bukkit.persistence.PersistentDataType.BYTE
        );
    }
}

