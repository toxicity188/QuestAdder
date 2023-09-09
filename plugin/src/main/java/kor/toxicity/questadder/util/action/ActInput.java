package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.CallbackManager;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.util.ComponentReader;
import kor.toxicity.questadder.api.util.DataField;
import kotlin.Unit;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActInput extends AbstractAction {
    @DataField(aliases = "m", throwIfNull = true)
    public String message;
    @DataField(aliases = "v", throwIfNull = true)
    public String variable;
    @DataField(aliases = "c", throwIfNull = true)
    public String callback;
    @DataField(aliases = "f", throwIfNull = true)
    public String failure;

    private ComponentReader<QuestAdderEvent> componentReader;
    private AbstractAction cal, fa;

    public ActInput(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        componentReader = new ComponentReader<>(message);
        ((QuestAdderBukkit) adder.getPlugin()).addLazyTask(() -> {
            cal = DialogManager.INSTANCE.getAction(callback);
            if (cal == null) QuestAdderBukkit.Companion.warn("the action \"" + callback + "\" doesn't exist.");
            fa = DialogManager.INSTANCE.getAction(failure);
            if (fa == null) QuestAdderBukkit.Companion.warn("the action \"" + failure + "\" doesn't exist.");
            return Unit.INSTANCE;
        });
    }

    @Override
    public void invoke(Player player, QuestAdderEvent event) {
        if (cal != null && fa != null) {
            var message = componentReader.createComponent(event);
            if (message != null) CallbackManager.INSTANCE.openSign(player, List.of(
                    Component.empty(),
                    message,
                    Component.empty(),
                    Component.empty()
            ),s -> {
                if (!s[0].isEmpty()) {
                    var data = QuestAdderBukkit.Companion.getPlayerData(player);
                    if (data != null) data.set(variable, s[0]);
                    cal.invoke(player, event);
                } else {
                    fa.invoke(player, event);
                }
                return Unit.INSTANCE;
            });
        }
    }
}
