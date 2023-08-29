package kor.toxicity.questadder.api.util;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface IRewardSet {
    double getExp();
    double getMoney();
    IRewardSetContent[] getItems();
}
