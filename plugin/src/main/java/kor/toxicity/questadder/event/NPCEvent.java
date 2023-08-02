package kor.toxicity.questadder.event;

import kor.toxicity.questadder.mechanic.npc.ActualNPC;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface NPCEvent {
    @NotNull ActualNPC getNpc();
    static HandlerList getHandlerList() {
        return QuestAdderEvent.HANDLER_LIST;
    }
}
