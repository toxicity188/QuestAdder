package kor.toxicity.questadder.api.mechanic;

import net.citizensnpcs.api.npc.NPC;

public interface IActualNPC {
    NPC toCitizensNPC();
    IQuestNPC toQuestNPC();
}
