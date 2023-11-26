package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.manager.ItemManager;
import kor.toxicity.questadder.nms.ToastType;
import kor.toxicity.questadder.util.ComponentReader;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ActToast extends AbstractAction {

    private static final ItemStack DEFAULT_ITEM = new ItemStack(Material.STONE);

    @DataField(aliases = "m", throwIfNull = true)
    public String message;
    @DataField(aliases = "i")
    public String item;
    @DataField(aliases = "t")
    public ToastType type = ToastType.GOAL;

    private ComponentReader<QuestAdderEvent> componentReader;
    private ItemStack i;

    public ActToast(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        componentReader = new ComponentReader<>(message);
        message = null;
        if (item != null) {
            i = ItemManager.INSTANCE.getItem(item);
            item = null;
        }
        if (i == null) i = DEFAULT_ITEM;
    }

    @Override
    public @NotNull ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var create = componentReader.createComponent(event, Collections.emptyMap());
        if (create != null) {
            QuestAdderBukkit.Companion.getNms().sendAdvancementMessage(player, i, type, create);
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.FAIL;
        }
    }
}
