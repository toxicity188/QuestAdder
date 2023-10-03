plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm") version("1.9.10")
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("maven-publish")
    id("org.jetbrains.dokka") version("1.9.0")
}

val questAdderGroup = "kor.toxicity.questadder"
val questAdderVersion = "1.1.3"

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "maven-publish")

    group = questAdderGroup
    version = questAdderVersion

    repositories {
        mavenCentral()
        maven {
            name = "papermc-repo"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven(url = "https://repo.dmulloy2.net/repository/public/")
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }
        maven {
            name = "citizens-repo"
            url = uri("https://maven.citizensnpcs.co/repo")
        }
        maven(url = "https://jitpack.io")
        maven {
            name = "alessiodpRepo"
            url = uri("https://repo.alessiodp.com/releases")
        }
        maven {
            name = "minecraft-repo"
            url = uri("https://libraries.minecraft.net/")
        }
        maven(url = "https://maven.enginehub.org/repo/")
        maven(url = "https://mvn.lumine.io/repository/maven/")
        maven(url = "https://repo.bg-software.com/repository/api/")
        maven(url = "https://nexus.phoenixdevt.fr/repository/maven-public/")
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
        testImplementation("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

        compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
        compileOnly("net.byteflux:libby-bukkit:1.1.5")
        compileOnly("com.github.oraxen:oraxen:1.158.0")
        compileOnly("com.github.LoneDev6:api-itemsadder:3.5.0b")
        compileOnly("net.citizensnpcs:citizens-main:2.0.32-SNAPSHOT")
        compileOnly("com.github.MilkBowl:VaultAPI:1.7")
        compileOnly("com.mojang:brigadier:1.1.8")
        compileOnly("com.mojang:datafixerupper:6.0.8")
        compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
        compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")
        compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
        compileOnly(fileTree("libs"))
        compileOnly("io.lumine:Mythic-Dist:5.3.5")
        compileOnly("com.bgsoftware:SuperiorSkyblockAPI:2023.2")
        compileOnly("net.Indyuce:MMOCore-API:1.12-SNAPSHOT")
        compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
        compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
        compileOnly("com.github.BeYkeRYkt.LightAPI:lightapi-bukkit-common:5.3.0-Bukkit")
    }
}
subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

dependencies {
    runtimeOnly(project(":expansion"))
    implementation(project(path = ":nms:v1_17_R1", configuration = "shadow"))
    implementation(project(path = ":nms:v1_18_R1", configuration = "shadow"))
    implementation(project(path = ":nms:v1_18_R2", configuration = "shadow"))
    implementation(project(path = ":nms:v1_19_R1", configuration = "shadow"))
    implementation(project(path = ":nms:v1_19_R2", configuration = "shadow"))
    implementation(project(path = ":nms:v1_19_R3", configuration = "shadow"))
    implementation(project(path = ":nms:v1_20_R1", configuration = "shadow"))
    implementation(project(path = ":nms:v1_20_R2", configuration = "shadow"))
    implementation(project(path = ":plugin", configuration = "shadow"))
}

val targetJavaVersion = 17

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(targetJavaVersion)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
            "version" to version
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    test {
        useJUnitPlatform()

        maxHeapSize = "1G"

        testLogging {
            events("passed")
        }
    }
    jar {
        finalizedBy(shadowJar)
        archiveFileName.set("QuestAdder.jar")
    }
    shadowJar {
        finalizedBy(dokkaHtmlMultiModule)
        archiveFileName.set("QuestAdder.jar")
        relocate("kotlin","kor.toxicity.questadder.shaded.kotlin")
        relocate("com.ticxo.playeranimator","kor.toxicity.questadder.shaded.com.ticxo.playeranimator")
        relocate("org.apache.commons.io","kor.toxicity.questadder.shaded.org.apache.commons.io")
        relocate("net.objecthunter","kor.toxicity.questadder.shaded.net.objecthunter")
        relocate("org.zeroturnaround","kor.toxicity.questadder.shaded.org.zeroturnaround")
        relocate("org.bstats","kor.toxicity.questadder.shaded.org.bstats")
        dependencies {
            exclude(dependency("org.jetbrains:annotations:13.0"))
        }
    }
    dokkaHtmlMultiModule {
        outputDirectory.set(file("${layout.buildDirectory.get()}/dokka"))
    }
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

kotlin {
    jvmToolchain(targetJavaVersion)
}
