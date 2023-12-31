plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
}

rootProject.name = "QuestAdder"

include("plugin")
include("api")
include("expansion")
include("nms:v1_17_R1")
include("nms:v1_18_R1")
include("nms:v1_18_R2")
include("nms:v1_19_R1")
include("nms:v1_19_R2")
include("nms:v1_19_R3")
include("nms:v1_20_R1")
include("nms:v1_20_R2")
include("nms:v1_20_R3")

include("platform:spigot")
include("platform:paper")

include("scheduler")
include("scheduler:standard")
include("scheduler:folia")
