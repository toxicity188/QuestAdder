package kor.toxicity.questadder.event;

import kor.toxicity.questadder.mechanic.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuestRemoveEvent extends QuestAdderPlayerEvent implements Cancellable, QuestPlayerEvent {

    private final Quest quest;
    public QuestRemoveEvent(Quest quest, Player who) {
        super(who);
        this.quest = quest;
    }

    @Override
    public @NotNull Quest getQuest() {
        return quest;
    }

    private boolean cancelled;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
