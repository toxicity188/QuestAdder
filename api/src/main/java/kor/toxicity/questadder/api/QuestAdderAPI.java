package kor.toxicity.questadder.api;

import org.jetbrains.annotations.ApiStatus;
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

    /**
     * @return an API instance of QuestAdder
     */
    public static @NotNull QuestAdder getInstance() {
        return instance;
    }

    //Do not call this method
    @ApiStatus.Internal
    public static void setInstance(@NotNull QuestAdder instance) {
        if (QuestAdderAPI.instance != null) throw new RuntimeException();
        QuestAdderAPI.instance = Objects.requireNonNull(instance);
    }

}
