package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.util.IRewardSet;
import kor.toxicity.questadder.api.util.IRewardSetContent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GiveRewardEvent extends QuestAdderPlayerEvent implements Cancellable {

    private boolean cancelled;
    private double exp, money;
    private final List<ItemStack> itemStacks;
    public GiveRewardEvent(@NotNull Player who, IRewardSet rewardSet) {
        super(who);
        exp = rewardSet.getExp();
        money = rewardSet.getMoney();
        itemStacks = Arrays.stream(rewardSet.getItems()).filter(r -> ThreadLocalRandom.current().nextDouble(0,100) <= r.getChance()).map(IRewardSetContent::getItem).collect(Collectors.toList());
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

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
