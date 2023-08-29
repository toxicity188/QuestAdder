package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.event.QuestAdderEvent;
import org.bukkit.entity.Player;

public interface IAction {
    boolean apply(Player player, String... args);
    void invoke(Player player, QuestAdderEvent event);
}
