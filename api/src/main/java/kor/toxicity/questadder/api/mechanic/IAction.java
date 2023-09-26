package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.event.QuestAdderEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IAction {
    @NotNull ActionResult apply(@NotNull Player player, @NotNull String... args);
    @NotNull ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event);
}
