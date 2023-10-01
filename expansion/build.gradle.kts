import org.jetbrains.dokka.gradle.DokkaTaskPartial

dependencies {
    compileOnly("com.github.PlaceholderAPI:PlaceholderAPI:master-SNAPSHOT")
    compileOnly(project(":plugin"))
    compileOnly(project(":api"))
}

version = ""

tasks {
    jar {
        archiveBaseName.set("Expansion-questadder")
        archiveExtension.set("patch")
        destinationDirectory.set(file("$rootDir/plugin/src/main/resources"))
    }
}