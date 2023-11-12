plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm") version("1.9.10")
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("maven-publish")
    id("org.jetbrains.dokka") version("1.9.0")
}

val questAdderGroup = "kor.toxicity.questadder"
val questAdderVersion = "1.1.7"

val adventureVersion = "4.14.0"
val platformVersion = "4.3.1"

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


        compileOnly("net.kyori:adventure-api:${adventureVersion}")
        compileOnly("net.kyori:adventure-text-serializer-legacy:${adventureVersion}")
        compileOnly("net.kyori:adventure-text-serializer-gson:${adventureVersion}")
        compileOnly("net.kyori:adventure-text-serializer-plain:${adventureVersion}")
        compileOnly("net.kyori:adventure-platform-bukkit:${platformVersion}")

        compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")
        compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
        compileOnly("net.byteflux:libby-bukkit:1.3.0")
        compileOnly("net.citizensnpcs:citizens-main:2.0.33-SNAPSHOT")
        compileOnly("com.mojang:brigadier:1.1.8")
        compileOnly("com.mojang:datafixerupper:6.0.8")
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
            "version" to version,
            "adventureVersion" to adventureVersion,
            "platformVersion" to platformVersion
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

val sourcesJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.classes)
    fun getProjectSource(project: Project): Array<File> {
        return if (project.subprojects.isEmpty()) project.sourceSets.main.get().allSource.srcDirs.toTypedArray() else ArrayList<File>().apply {
            project.subprojects.forEach {
                addAll(getProjectSource(it))
            }
        }.toTypedArray()
    }
    archiveClassifier.set("sources")
    from(*getProjectSource(project))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
val dokkaJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.dokkaHtmlMultiModule)
    archiveClassifier.set("dokka")
    from(file("${layout.buildDirectory.get()}/dokka"))
}

artifacts {
    archives(dokkaJar)
    archives(sourcesJar)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

kotlin {
    jvmToolchain(targetJavaVersion)
}
