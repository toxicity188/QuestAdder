subprojects {
    repositories {
        mavenLocal()
    }
    dependencies {
        compileOnly(project(":platform"))
        compileOnly(project(":plugin"))
        compileOnly(project(":api"))
    }
}
