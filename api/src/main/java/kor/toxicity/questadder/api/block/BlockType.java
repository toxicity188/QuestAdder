package kor.toxicity.questadder.api.block;

import org.bukkit.Material;

public enum BlockType {
    NOTE_BLOCK(Material.NOTE_BLOCK),
    STRING(Material.TRIPWIRE),
    CHORUS_PLANT(Material.CHORUS_PLANT),
    RED_MUSHROOM_BLOCK(Material.RED_MUSHROOM_BLOCK),
    BROWN_MUSHROOM_BLOCK(Material.BROWN_MUSHROOM_BLOCK),
    MUSHROOM_STEM(Material.MUSHROOM_STEM),
    FIRE(Material.FIRE)
    ;
    private final Material material;
    BlockType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}
