package kor.toxicity.questadder.util.event.mythiclib;

import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EventMMOCastSkill extends AbstractEvent<PlayerCastSkillEvent> {

    @DataField(aliases = "t")
    public EntityType type;
    @DataField(aliases = "n")
    public String name;

    public EventMMOCastSkill(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerCastSkillEvent.class);
    }

    @Override
    public void invoke(@NotNull PlayerCastSkillEvent event) {
        var meta = event.getMetadata();
        if (meta.hasTargetEntity()) {
            var target = meta.getTargetEntity();
            if (type != null && target.getType() != type) return;
            if (name != null) {
                var n = target.getCustomName();
                if (n != null && !n.equals(name)) return;
            }
        }
        apply(event.getPlayer());
    }
}
