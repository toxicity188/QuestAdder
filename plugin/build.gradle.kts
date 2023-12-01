plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:5.0.47")

    compileOnly("io.th0rgal:oraxen:1.164.0")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.2-beta-r3-b")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.TheComputerGeek2.MagicSpells:core:1507fdf")
    compileOnly("io.lumine:Mythic-Dist:5.4.1")
    compileOnly("com.bgsoftware:SuperiorSkyblockAPI:2023.3")
    compileOnly("net.Indyuce:MMOCore-API:1.12.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("com.github.BeYkeRYkt.LightAPI:lightapi-bukkit-common:5.3.0-Bukkit")
    compileOnly("io.lumine:MythicCrucible:1.6.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.github.Ssomar-Developement:SCore:4.23.10.8")
    compileOnly("de.oliver:FancyNpcs:2.0.5")

    implementation("com.ticxo.playeranimator:PlayerAnimator:R1.2.7")
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("org.zeroturnaround:zt-zip:1.16") {
        exclude("org.slf4j")
    }
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation(project(":api"))
    implementation(project(":platform"))
    implementation(project(":platform:spigot"))
    implementation(project(":platform:paper"))
    implementation(project(":scheduler"))
    implementation(project(":scheduler:standard"))
    implementation(project(":scheduler:folia"))
}
