dependencies {
    compileOnly("org.spigotmc:spigot:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:4.0.43")
    implementation(fileTree("shade"))
}

tasks {
    shadowJar {
        exclude(".classpath")
        exclude("plugin.yml")
        relocate("eu.endercentral.crazy_advancements","kor.toxicity.questadder.nms.v1_20_R1.crazy_advancements")
    }
}
