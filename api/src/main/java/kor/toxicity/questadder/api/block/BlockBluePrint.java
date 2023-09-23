package kor.toxicity.questadder.api.block;

import org.jetbrains.annotations.NotNull;

public record BlockBluePrint(int light, @NotNull BlockType blockType, boolean canBurned) {
}
