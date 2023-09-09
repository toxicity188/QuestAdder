package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.api.util.DataField;
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
            if (quest1 == null) QuestAdderBukkit.Companion.warn("not found error: the quest named \"" + quest + "\" doesn't exist.");
            else {
                switch (action) {
                    case "remove" -> consumer = quest1::remove;
                    case "complete" -> consumer = quest1::complete;
                    default -> consumer = quest1::give;
                }
            }
        });
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (consumer != null) consumer.accept(player);
    }
}
