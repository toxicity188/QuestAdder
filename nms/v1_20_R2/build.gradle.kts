dependencies {
    compileOnly("org.spigotmc:spigot:1.20.2-R0.1-SNAPSHOT")
    implementation(fileTree("shade"))
}

tasks {
    shadowJar {
        exclude(".classpath")
        relocate("eu.endercentral.crazy_advancements","kor.toxicity.questadder.nms.v1_20_R2.crazy_advancements")
    }
}