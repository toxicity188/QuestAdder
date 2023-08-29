package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.util.ComponentReader;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.builder.ActionBuilder;
import kor.toxicity.questadder.util.reflect.DataField;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActEvaluate extends AbstractAction {
    @DataField(aliases = "p", throwIfNull = true)
    public String parameter;

    private ComponentReader<QuestAdderEvent> componentReader;

    public ActEvaluate(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        componentReader = new ComponentReader<>(parameter);
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var comp = componentReader.createComponent(event);
        if (comp == null) {
            throwRuntimeError();
            return;
        }
        var build = ActionBuilder.INSTANCE.createAction(adder, LegacyComponentSerializer.legacySection().serialize(comp));
        if (build == null) {
            throwRuntimeError();
            return;
        }
        build.invoke(player, event);
    }
    private void throwRuntimeError() {
        QuestAdder.Companion.warn("runtime error: unable to read this parameter: " + parameter);
    }
}
