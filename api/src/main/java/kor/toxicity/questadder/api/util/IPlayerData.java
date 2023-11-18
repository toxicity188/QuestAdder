package kor.toxicity.questadder.api.util;

import kor.toxicity.questadder.api.mechanic.IQuestData;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IPlayerData {
    @NotNull ConfigurationSection serialize();
    @NotNull Map<@NotNull String, ? extends @NotNull IQuestData> getQuestDataMap();
}
