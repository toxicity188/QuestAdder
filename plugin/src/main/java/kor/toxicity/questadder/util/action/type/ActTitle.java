package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.QuestAdderEvent;
import kor.toxicity.questadder.util.ComponentReader;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.BiConsumer;

public class ActTitle extends AbstractAction {

    @DataField(aliases = "t")
    public String title;
    @DataField(aliases = "st")
    public String subtitle;
    @DataField(aliases = "fi")
    public long fadeIn = 10;
    @DataField(aliases = "s")
    public long stay = 60;
    @DataField(aliases = "fo")
    public long fadeOut = 10;

    private static final Component ERROR_COMPONENT = Component.text("error!");
    private BiConsumer<Player,QuestAdderEvent> consumer;

    public ActTitle(QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();

        var times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50),
                Duration.ofMillis(fadeOut * 50),
                Duration.ofMillis(stay * 50)
        );

        if (title == null && subtitle == null) {
            throw new RuntimeException("both of title and subtitle is null.");
        }
        var titleReader = (title != null) ? new ComponentReader<QuestAdderEvent>(title) : null;
        var subTitleReader = (subtitle != null) ? new ComponentReader<QuestAdderEvent>(subtitle) : null;
        consumer = (p,e) -> {
            var comp1 = (titleReader != null) ? titleReader.createComponent(e) : Component.empty();
            var comp2 = (subTitleReader != null) ? subTitleReader.createComponent(e) : Component.empty();
            p.showTitle(Title.title(
                    (comp1 != null) ? comp1 : ERROR_COMPONENT,
                    (comp2 != null) ? comp2 : ERROR_COMPONENT,
                    times
            ));
        };
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        consumer.accept(player,event);
    }
}
