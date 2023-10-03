package kor.toxicity.questadder.nms

enum class NMSVersion(val version: Int, val subVersion: Int, val mcmetaVersion: Int) {
    V1_17_R1(17,1, 7),
    V1_18_R1(18,1, 8),
    V1_18_R2(18,2, 8),
    V1_19_R1(19,1, 9),
    V1_19_R2(19,2, 12),
    V1_19_R3(19,3, 13),
    V1_20_R1(20,1, 15),
    V1_20_R2(20,2, 18)
}
