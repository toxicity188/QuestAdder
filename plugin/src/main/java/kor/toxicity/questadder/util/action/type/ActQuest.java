package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import kotlin.Unit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ActQuest extends AbstractAction {
    @DataField(aliases = "q",throwIfNull = true)
    public String quest;
    @DataField(aliases = "a")
    public String action = "give";

    public ActQuest(@NotNull QuestAdder adder) {
        super(adder);
    }

    private Consumer<Player> consumer;

    @Override
    public void initialize() {
        super.initialize();
        adder.addLazyTask(() -> {
            var quest1 = DialogManager.INSTANCE.getQuest(quest);
            if (quest1 == null) QuestAdder.Companion.warn("not found error: the quest named \"" + quest + "\" doesn't exist.");
            else {
                switch (action) {
                    case "remove" -> consumer = quest1::remove;
                    case "complete" -> consumer = quest1::complete;
                    default -> consumer = quest1::give;
                }
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (consumer != null) consumer.accept(player);
    }
}
