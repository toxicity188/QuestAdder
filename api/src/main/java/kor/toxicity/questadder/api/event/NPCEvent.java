package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.mechanic.IActualNPC;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface NPCEvent {
    @NotNull
    IActualNPC getNpc();
}
