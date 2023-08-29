package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.util.TextReader;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class ActCommand extends AbstractAction {

    @DataField(aliases = "c",throwIfNull = true)
    public String command;
    @DataField(aliases = "o")
    public boolean op = true;
    @DataField(aliases = "co")
    public boolean console = false;

    private BiConsumer<Player,QuestAdderEvent> consumer;

    public ActCommand(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        var reader = new TextReader<QuestAdderEvent>(command);
        consumer = (console) ? (p,e) -> {
            var text = reader.createString(e);
            if (text != null) Bukkit.dispatchCommand(Bukkit.getConsoleSender(),text);
            else throwRuntimeError();
        } : ((op) ? (p,e) -> {
            var text = reader.createString(e);
            if (text != null) {
                if (!p.isOp()) {
                    p.setOp(true);
                    Bukkit.dispatchCommand(p, text);
                    p.setOp(false);
                } else Bukkit.dispatchCommand(p, text);
            }
            else throwRuntimeError();
        } : (p,e) -> {
            var text = reader.createString(e);
            if (text != null) Bukkit.dispatchCommand(p,text);
            else throwRuntimeError();
        });
        command = null;
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        consumer.accept(player, event);
    }

    private void throwRuntimeError() {
        QuestAdder.Companion.warn("runtime error: cannot dispatch command \"" + consumer + "\"");
    }
}
