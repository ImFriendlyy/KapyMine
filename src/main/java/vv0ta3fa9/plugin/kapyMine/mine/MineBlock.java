package vv0ta3fa9.plugin.kapyMine.mine;

import org.bukkit.Material;

public class MineBlock {
    private final Material material;
    private final int chance;

    public MineBlock(Material material, int chance) {
        this.material = material;
        this.chance = chance;
    }

    public Material getMaterial() { return material; }
    public int getChance() { return chance; }
}

