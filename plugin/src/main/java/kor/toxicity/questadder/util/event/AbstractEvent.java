package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import kor.toxicity.questadder.util.reflect.DataObject;
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

    private static final Listener LISTENER = new Listener() {
    };
    public static void unregisterAll() {
        HandlerList.unregisterAll(LISTENER);
    }
    private final AbstractAction action;
    public AbstractEvent(QuestAdder adder, AbstractAction action, Class<T> clazz) {
        this.action = action;
        Bukkit.getPluginManager().registerEvent(clazz,LISTENER, EventPriority.MONITOR,((listener, event) -> {
            if (clazz.isAssignableFrom(event.getClass()) && ThreadLocalRandom.current().nextDouble(100) <= chance) invoke(clazz.cast(event));
        }), adder);
    }

    @Override
    public void initialize() {
        if (chance < 0) chance = 0;
    }
    protected abstract void invoke(T event);

    protected final void apply(Player player, String... args) {
        action.apply(player, args);
    }
    protected final void apply(Player player, QuestAdderEvent event) {
        action.invoke(player, event);
    }
}
