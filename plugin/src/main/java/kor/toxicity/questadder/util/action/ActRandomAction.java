package kor.toxicity.questadder.util.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.concurrent.LazyRunnable;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.api.util.DataField;
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
        adder.addLazyTask(LazyRunnable.emptyOf(() -> {
            var i = 0;
            for (JsonElement e : name) {
                try {
                    var str = e.getAsString();
                    var action = DialogManager.INSTANCE.getAction(str);
                    if (action != null) actionList.add(action);
                    else QuestAdderBukkit.Companion.warn("the action named \"" + str + "\" doesn't exist.");
                } catch (Exception ex) {
                    QuestAdderBukkit.Companion.warn("unable to read json element index of " + i);
                }
                i ++;
            }
            if (!actionList.isEmpty()) {
                consumer = (p,q) -> actionList.get(ThreadLocalRandom.current().nextInt(actionList.size())).invoke(p,q);
            } else {
                consumer = (p,q) -> QuestAdderBukkit.Companion.warn("runtime error: an actions are empty.");
                QuestAdderBukkit.Companion.warn("an actions are empty.");
            }
        }));
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        consumer.accept(player,event);
        return ActionResult.SUCCESS;
    }
}
