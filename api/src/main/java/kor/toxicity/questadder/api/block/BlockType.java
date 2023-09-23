package kor.toxicity.questadder.api.block;

import org.bukkit.Material;

public enum BlockType {
    NOTE_BLOCK(Material.NOTE_BLOCK),
    STRING(Material.TRIPWIRE)
    ;
    private final Material material;
    BlockType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}
