dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly(project(":plugin"))
    compileOnly(project(":api"))
}

version = ""

tasks {
    jar {
        archiveBaseName.set("Expansion-questadder")
        archiveExtension.set("patch")
    }
}
