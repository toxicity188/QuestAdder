package kor.toxicity.questadder.event;

import kor.toxicity.questadder.mechanic.quest.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuestCompleteEvent extends QuestAdderPlayerEvent implements Cancellable, QuestPlayerEvent {

    private final Quest quest;
    private final double money, exp;
    private final List<ItemStack> itemStacks;
    public QuestCompleteEvent(Quest quest, Player who, double money, double exp, List<ItemStack> itemStacks) {
        super(who);
        this.quest = quest;
        this.money = money;
        this.exp = exp;
        this.itemStacks = itemStacks;
    }

    public double getMoney() {
        return money;
    }

    public double getExp() {
        return exp;
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    @Override
    public @NotNull Quest getQuest() {
        return quest;
    }

    private boolean cancelled;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
