import org.jetbrains.dokka.gradle.DokkaTaskPartial

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.github.PlaceholderAPI:PlaceholderAPI:master-SNAPSHOT")
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
