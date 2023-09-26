package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.gui.IGui;
import kor.toxicity.questadder.api.util.SoundData;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DialogSender {
    @Nullable
    Entity getEntity();
    @NotNull
    SoundData getSoundData();
    @NotNull
    String getTalkerName();
    long getTypingSpeed();
    @Nullable
    IGui getGui();
}
