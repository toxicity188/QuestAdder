package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.mechanic.IQuest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuestSurrenderFailEvent extends QuestAdderPlayerEvent implements Cancellable, QuestPlayerEvent {

    private final IQuest quest;
    public QuestSurrenderFailEvent(IQuest quest, Player who) {
        super(who);
        this.quest = quest;
    }

    @Override
    public @NotNull IQuest getQuest() {
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
