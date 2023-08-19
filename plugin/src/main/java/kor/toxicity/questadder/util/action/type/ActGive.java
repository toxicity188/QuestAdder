package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.QuestAdderEvent;
import kor.toxicity.questadder.extension.PlayersKt;
import kor.toxicity.questadder.manager.ItemManager;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ActGive extends AbstractAction {

    @DataField(aliases = "i",throwIfNull = true)
    public String item;
    private ItemStack stack;
    public ActGive(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        stack = ItemManager.INSTANCE.getItem(item);
        if (stack == null) throw new RuntimeException("the item named \"" + item + "\" doesn't exist.");
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        PlayersKt.give(player,stack);
    }
}
