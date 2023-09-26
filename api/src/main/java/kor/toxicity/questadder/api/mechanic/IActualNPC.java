package kor.toxicity.questadder.api.mechanic;

import net.citizensnpcs.api.npc.NPC;
import org.jetbrains.annotations.NotNull;

public interface IActualNPC extends DialogSender {
    @NotNull NPC toCitizensNPC();
    @NotNull IQuestNPC toQuestNPC();
}
