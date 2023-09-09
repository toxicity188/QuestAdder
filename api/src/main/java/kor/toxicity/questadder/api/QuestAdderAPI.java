package kor.toxicity.questadder.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author toxicity
 */
public final class QuestAdderAPI {
    private static QuestAdder instance;
    private QuestAdderAPI() {
        throw new RuntimeException();
    }

    public static QuestAdder getInstance() {
        return instance;
    }

    public static void setInstance(@NotNull QuestAdder instance) {
        QuestAdderAPI.instance = Objects.requireNonNull(instance);
    }

}
