package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.event.QuestAdderEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IAction {
    boolean apply(@NotNull Player player, @NotNull String... args);
    void invoke(@NotNull Player player, @NotNull QuestAdderEvent event);
}
