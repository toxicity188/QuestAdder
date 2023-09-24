package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.api.util.DataObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractEvent<T extends Event> implements DataObject {

    @DataField
    public double chance = 100.0;
    private final AbstractAction action;
    public AbstractEvent(@NotNull QuestAdder adder, @NotNull AbstractAction action, @NotNull Class<T> clazz) {
        this.action = action;
        adder.registerEvent(this,clazz);
    }

    @Override
    public void initialize() {
        if (chance < 0) chance = 0;
    }
    public abstract void invoke(@NotNull T event);

    protected final void apply(@NotNull Player player, @NotNull String... args) {
        action.apply(player, args);
    }
    protected final void apply(@NotNull Player player, @NotNull QuestAdderEvent event) {
        action.invoke(player, event);
    }
}
