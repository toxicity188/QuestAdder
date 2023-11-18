dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:5.0.47")

    compileOnly("com.github.oraxen:oraxen:1.163.0")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly(fileTree("libs"))
    compileOnly("io.lumine:Mythic-Dist:5.4.1")
    compileOnly("com.bgsoftware:SuperiorSkyblockAPI:2023.3")
    compileOnly("net.Indyuce:MMOCore-API:1.12-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("com.github.BeYkeRYkt.LightAPI:lightapi-bukkit-common:5.3.0-Bukkit")
    compileOnly("io.lumine:MythicCrucible:1.5.0")
    compileOnly("com.github.PlaceholderAPI:PlaceholderAPI:master-SNAPSHOT")

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
}
