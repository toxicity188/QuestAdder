package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.concurrent.LazyRunnable;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.gui.IGui;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.api.mechanic.DialogSender;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.api.util.SoundData;
import kor.toxicity.questadder.manager.DialogManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ActCustomDialog extends AbstractAction {

    @DataField(aliases = "d", throwIfNull = true)
    public String dialog;

    @DataField(aliases = "n", throwIfNull = true)
    public String name;

    @DataField(aliases = "s")
    public String sound = QuestAdderBukkit.Config.INSTANCE.getDefaultTypingSound().name();
    @DataField(aliases = "v")
    public float volume;
    @DataField(aliases = "p")
    public float pitch;
    @DataField
    public long speed;

    private Consumer<Player> consumer;

    public ActCustomDialog(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        adder.addLazyTask(LazyRunnable.emptyOf(() -> {
            var d = DialogManager.INSTANCE.getDialog(dialog);
            if (d == null) QuestAdderBukkit.Companion.warn("the dialog named \"" + dialog + "\" doesn't exist.");
            else {
                var data = new SoundData(sound, volume, pitch);
                var sender = new DialogSender() {
                    @Override
                    public @Nullable Supplier<Location> getLocationSupplier() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public IGui getGui() {
                        return null;
                    }

                    @Override
                    public long getTypingSpeed() {
                        return speed;
                    }

                    @NotNull
                    @Override
                    public SoundData getSoundData() {
                        return data;
                    }

                    @NotNull
                    @Override
                    public String getTalkerName() {
                        return name;
                    }
                };
                consumer = p -> d.start(p, sender);
            }
        }));
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (consumer != null) {
            consumer.accept(player);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }
}
