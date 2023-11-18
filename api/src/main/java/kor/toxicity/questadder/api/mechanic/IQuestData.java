package kor.toxicity.questadder.api.mechanic;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public interface IQuestData {
    @NotNull LocalDateTime getGivenTime();
    @NotNull Map<@NotNull String, @NotNull Long> getLocalVariableMap();
    @NotNull QuestRecord getRecord();
}
