package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.npc.WrappedNPC;
import net.citizensnpcs.api.npc.NPC;
import org.jetbrains.annotations.NotNull;

public interface IActualNPC extends DialogSender {
    @NotNull WrappedNPC toUsedNPC();
    @NotNull IQuestNPC toQuestNPC();
}
