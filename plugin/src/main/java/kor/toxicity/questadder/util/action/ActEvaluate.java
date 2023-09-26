package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.util.ComponentReader;
import kor.toxicity.questadder.util.builder.ActionBuilder;
import kor.toxicity.questadder.api.util.DataField;
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

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var comp = componentReader.createComponent(event);
        if (comp == null) {
            throwRuntimeError();
            return ActionResult.FAIL;
        }
        var build = ActionBuilder.INSTANCE.createAction(adder, LegacyComponentSerializer.legacySection().serialize(comp));
        if (build == null) {
            throwRuntimeError();
            return ActionResult.FAIL;
        }
        build.invoke(player, event);
        return ActionResult.SUCCESS;
    }
    private void throwRuntimeError() {
        QuestAdderBukkit.Companion.warn("runtime error: unable to read this parameter: " + parameter);
    }
}
