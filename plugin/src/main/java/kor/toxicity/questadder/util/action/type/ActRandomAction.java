package kor.toxicity.questadder.util.action.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import kotlin.Unit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public class ActRandomAction extends AbstractAction {
    @DataField(aliases = "n", throwIfNull = true)
    public JsonArray name;

    private BiConsumer<Player,QuestAdderEvent> consumer;
    public ActRandomAction(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        var actionList = new ArrayList<AbstractAction>();
        adder.addLazyTask(() -> {
            var i = 0;
            for (JsonElement e : name) {
                try {
                    var str = e.getAsString();
                    var action = DialogManager.INSTANCE.getAction(str);
                    if (action != null) actionList.add(action);
                    else QuestAdder.Companion.warn("the action named \"" + str + "\" doesn't exist.");
                } catch (Exception ex) {
                    QuestAdder.Companion.warn("unable to read json element index of " + i);
                }
                i ++;
            }
            if (!actionList.isEmpty()) {
                consumer = (p,q) -> actionList.get(ThreadLocalRandom.current().nextInt(actionList.size())).invoke(p,q);
            } else {
                consumer = (p,q) -> QuestAdder.Companion.warn("runtime error: an actions are empty.");
                QuestAdder.Companion.warn("an actions are empty.");
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    public void invoke(Player player, QuestAdderEvent event) {
        consumer.accept(player,event);
    }
}
