subprojects {
    repositories {
        mavenLocal()
    }
    dependencies {
        compileOnly(project(":plugin"))
        compileOnly(project(":api"))
    }
}
