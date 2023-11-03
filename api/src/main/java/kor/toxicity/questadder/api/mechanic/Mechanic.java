package kor.toxicity.questadder.api.mechanic;

import org.jetbrains.annotations.NotNull;

public interface Mechanic {
    @NotNull MechanicBlueprint getOriginalBlueprint();
}
