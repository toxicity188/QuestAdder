dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
}

subprojects {
    dependencies {
        compileOnly(project(":scheduler"))
    }
}
