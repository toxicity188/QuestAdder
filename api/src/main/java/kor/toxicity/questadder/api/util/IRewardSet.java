package kor.toxicity.questadder.api.util;

import org.jetbrains.annotations.NotNull;

public interface IRewardSet {
    double getExp();
    double getMoney();
    @NotNull IRewardSetContent[] getItems();
}
