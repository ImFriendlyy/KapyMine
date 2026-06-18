package vv0ta3fa9.plugin.kapyMine.mine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import vv0ta3fa9.plugin.kapyMine.KapyMine;
import vv0ta3fa9.plugin.kapyMine.cfg.ConfigManager;
import vv0ta3fa9.plugin.kapyMine.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MineManager {
    private final KapyMine plugin;
    private final ConfigManager configManager;
    private final Random random = new Random();

    private List<MineType> mineTypes = new ArrayList<>();
    private MineRegion region;
    private MineType nextType;

    private BukkitTask task;
    private long refillIntervalTicks;
    private long lastRefillTime;

    public MineManager(KapyMine plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void load() {
        loadMineTypes();
        loadRegion();
        refillIntervalTicks = configManager.getConfig().getLong("refill-interval", 300) * 20L;
        // nextType выбирается до первого fill(), чтобы объявить его заранее
        nextType = pickRandomType();
    }

    public void start() {
        stopTask();
        if (region == null || mineTypes.isEmpty()) return;
        fill(nextType);
        scheduleNext();
    }

    public void stop() {
        stopTask();
    }

    public void forceRefill() {
        stopTask();
        if (region == null || mineTypes.isEmpty()) return;
        nextType = pickRandomType();
        fill(nextType);
        scheduleNext();
    }

    public long getMillisUntilRefill() {
        long elapsed = System.currentTimeMillis() - lastRefillTime;
        return Math.max(0, refillIntervalTicks * 50L - elapsed);
    }

    public MineType getNextType() { return nextType; }
    public List<MineType> getMineTypes() { return mineTypes; }
    public boolean hasRegion() { return region != null; }
    public boolean hasMineTypes() { return !mineTypes.isEmpty(); }

    public void setRegion(Location pos1, Location pos2) {
        region = new MineRegion(pos1, pos2);
        var cfg = configManager.getConfig();
        cfg.set("mine-region-set", true);
        cfg.set("mine-world", pos1.getWorld().getName());
        cfg.set("mine-pos1.x", pos1.getBlockX());
        cfg.set("mine-pos1.y", pos1.getBlockY());
        cfg.set("mine-pos1.z", pos1.getBlockZ());
        cfg.set("mine-pos2.x", pos2.getBlockX());
        cfg.set("mine-pos2.y", pos2.getBlockY());
        cfg.set("mine-pos2.z", pos2.getBlockZ());
        configManager.saveConfig();
    }

    private void fill(MineType type) {
        if (region == null || type == null) return;
        World world = region.getWorld();
        int[] thresholds = buildThresholds(type.getBlocks());
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    world.getBlockAt(x, y, z).setType(pickMaterial(type.getBlocks(), thresholds));
                }
            }
        }
        lastRefillTime = System.currentTimeMillis();
        Bukkit.broadcastMessage(configManager.getMessage("mine-filled")
            .replace("%type%", ColorUtil.colorize(type.getName())));
    }

    private void scheduleNext() {
        nextType = pickRandomType();
        Bukkit.broadcastMessage(configManager.getMessage("next-mine-type")
            .replace("%type%", ColorUtil.colorize(nextType.getName())));
        task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            fill(nextType);
            scheduleNext();
        }, refillIntervalTicks);
    }

    private void stopTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }

    private MineType pickRandomType() {
        if (mineTypes.isEmpty()) return null;
        int total = mineTypes.stream().mapToInt(MineType::getChance).sum();
        int roll = random.nextInt(Math.max(total, 1));
        int cumulative = 0;
        for (MineType type : mineTypes) {
            cumulative += type.getChance();
            if (roll < cumulative) return type;
        }
        return mineTypes.get(0);
    }

    /** Выбирает материал по весу; если сумма весов < 100, остаток — STONE */
    private Material pickMaterial(List<MineBlock> blocks, int[] thresholds) {
        int roll = random.nextInt(100);
        for (int i = 0; i < blocks.size(); i++) {
            if (roll < thresholds[i]) return blocks.get(i).getMaterial();
        }
        return Material.STONE;
    }

    private int[] buildThresholds(List<MineBlock> blocks) {
        int[] t = new int[blocks.size()];
        int sum = 0;
        for (int i = 0; i < blocks.size(); i++) {
            sum += blocks.get(i).getChance();
            t[i] = sum;
        }
        return t;
    }

    private void loadMineTypes() {
        mineTypes.clear();
        for (Map<?, ?> map : configManager.getConfig().getMapList("mine-types")) {
            String name = (String) map.get("name");
            Object chanceObj = map.get("chance");
            if (name == null || chanceObj == null) continue;
            int chance = chanceObj instanceof Integer i ? i : Integer.parseInt(chanceObj.toString());
            List<MineBlock> blocks = new ArrayList<>();
            if (map.get("blocks") instanceof List<?> blockList) {
                for (Object entry : blockList) {
                    MineBlock block = parseBlockEntry(entry.toString());
                    if (block != null) blocks.add(block);
                }
            }
            if (!blocks.isEmpty()) {
                mineTypes.add(new MineType(ColorUtil.colorize(name), chance, blocks));
            }
        }
    }

    /** Парсит запись вида "diamond_ore:15" */
    private MineBlock parseBlockEntry(String entry) {
        String[] parts = entry.split(":");
        if (parts.length != 2) return null;
        try {
            Material mat = Material.matchMaterial(parts[0].trim().toUpperCase());
            if (mat == null) {
                plugin.getLogger().warning("Неизвестный материал: " + parts[0]);
                return null;
            }
            return new MineBlock(mat, Integer.parseInt(parts[1].trim()));
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Некорректный шанс блока: " + entry);
            return null;
        }
    }

    private void loadRegion() {
        region = null;
        var cfg = configManager.getConfig();
        if (!cfg.getBoolean("mine-region-set", false)) return;
        String worldName = cfg.getString("mine-world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Мир '" + worldName + "' не найден. Регион шахты не загружен.");
            return;
        }
        int x1 = cfg.getInt("mine-pos1.x"), y1 = cfg.getInt("mine-pos1.y"), z1 = cfg.getInt("mine-pos1.z");
        int x2 = cfg.getInt("mine-pos2.x"), y2 = cfg.getInt("mine-pos2.y"), z2 = cfg.getInt("mine-pos2.z");
        region = new MineRegion(new Location(world, x1, y1, z1), new Location(world, x2, y2, z2));
    }
}
