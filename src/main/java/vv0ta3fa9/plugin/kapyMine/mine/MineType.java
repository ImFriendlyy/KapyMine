package vv0ta3fa9.plugin.kapyMine.mine;

import java.util.List;

public class MineType {
    private final String name;       // поддерживает цветовые коды (&a...)
    private final int chance;
    private final List<MineBlock> blocks;

    public MineType(String name, int chance, List<MineBlock> blocks) {
        this.name = name;
        this.chance = chance;
        this.blocks = blocks;
    }

    public String getName() { return name; }
    public int getChance() { return chance; }
    public List<MineBlock> getBlocks() { return blocks; }
}

