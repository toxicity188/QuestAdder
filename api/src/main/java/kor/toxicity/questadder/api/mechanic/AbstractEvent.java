package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.api.util.DataObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractEvent<T extends Event> implements DataObject {

    @DataField
    public double chance = 100.0;
    private final AbstractAction action;
    public AbstractEvent(QuestAdder adder, AbstractAction action, Class<T> clazz) {
        this.action = action;
        adder.registerEvent(this,clazz);
    }

    @Override
    public void initialize() {
        if (chance < 0) chance = 0;
    }
    public abstract void invoke(T event);

    protected final void apply(Player player, String... args) {
        action.apply(player, args);
    }
    protected final void apply(Player player, QuestAdderEvent event) {
        action.invoke(player, event);
    }
}
