package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.mechanic.IQuest;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface QuestEvent {
    @NotNull IQuest getQuest();
}
