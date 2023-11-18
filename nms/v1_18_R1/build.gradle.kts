dependencies {
    compileOnly("org.spigotmc:spigot:1.18.1-R0.1-SNAPSHOT")
    implementation(fileTree("shade"))
}

tasks {
    shadowJar {
        exclude(".classpath")
        exclude("plugin.yml")
        relocate("eu.endercentral.crazy_advancements","kor.toxicity.questadder.nms.v1_18_R1.crazy_advancements")
    }
}
