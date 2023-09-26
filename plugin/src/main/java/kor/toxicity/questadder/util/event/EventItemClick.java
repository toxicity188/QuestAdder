package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.manager.ItemManager;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EventItemClick extends AbstractEvent<PlayerInteractEvent> {

    @DataField(aliases = "i", throwIfNull = true)
    public String item;
    @DataField(aliases = "a")
    public int amount = 1;
    @DataField(aliases = "c")
    public boolean consume = false;

    private ItemStack i;
    public EventItemClick(@NotNull QuestAdder adder, @NotNull AbstractAction action) {
        super(adder, action, PlayerInteractEvent.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        i = ItemManager.INSTANCE.getItem(item);
        if (i == null) throw new RuntimeException("the item named \"" + item + "\" doesn't exist.");
        amount = Math.min(Math.max(amount,1),i.getMaxStackSize());
    }

    @Override
    public void invoke(@NotNull PlayerInteractEvent event) {
        switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                return;
            }
        }
        var get = event.getItem();
        if (get == null) return;
        if (!get.isSimilar(i)) return;
        if (consume) get.setAmount(get.getAmount() - amount);
        apply(event.getPlayer());
    }
}
