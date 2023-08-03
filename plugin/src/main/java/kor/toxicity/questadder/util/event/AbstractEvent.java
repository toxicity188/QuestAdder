package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class AbstractEvent<T extends Event> implements DataObject {
    private static final Listener LISTENER = new Listener() {
    };
    public static void unregisterAll() {
        HandlerList.unregisterAll(LISTENER);
    }
    private final AbstractAction action;
    public AbstractEvent(QuestAdder adder, AbstractAction action, Class<T> clazz) {
        this.action = action;
        Bukkit.getPluginManager().registerEvent(clazz,LISTENER, EventPriority.NORMAL,((listener, event) -> {
            if (clazz.isAssignableFrom(event.getClass())) invoke(clazz.cast(event));
        }), adder);
    }

    @Override
    public void initialize() {

    }
    protected abstract void invoke(T event);

    protected final void apply(Player player, String... args) {
        action.apply(player, args);
    }
}
