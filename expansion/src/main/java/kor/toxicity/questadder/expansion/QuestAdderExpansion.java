package kor.toxicity.questadder.expansion;

import kor.toxicity.questadder.api.event.QuestInvokeEvent;
import kor.toxicity.questadder.manager.DialogManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public final class QuestAdderExpansion extends PlaceholderExpansion {
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.startsWith("selected_quest_")) {
            var selectedQuest = DialogManager.INSTANCE.getSelectedQuest(player);
            if (selectedQuest != null) {
                var subString = params.substring("selected_quest_".length());
                if (subString.startsWith("condition_")) {
                    var conditions = selectedQuest.getConditions();
                    try {
                        var i = Integer.parseInt(subString.substring("condition_".length()));
                        var comp = conditions.size() > i ? conditions.get(i).createComponent(new QuestInvokeEvent(selectedQuest,player)) : null;
                        return comp != null ? PlainTextComponentSerializer.plainText().serialize(comp) : "";
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }

            } else return "";
        }
        else if (params.startsWith("index_")) {
            var npc = DialogManager.INSTANCE.getQuestNPC(params.substring("index_".length()));
            if (npc != null) {
                var index = npc.getIndex(player);
                if (index != null) return Integer.toString(index);
            }
        }
        else if (params.startsWith("state_")) {
            var quest = DialogManager.INSTANCE.getQuest(params.substring("state_".length()));
            if (quest != null) {
                return quest.getState(player).name();
            }
        }
        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "questadder";
    }

    @Override
    public @NotNull String getAuthor() {
        return "toxicity";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0-SNAPSHOT";
    }
}
