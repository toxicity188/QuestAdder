subprojects {
    dependencies {
        compileOnly(project(":platform"))
        compileOnly(project(":plugin"))
        compileOnly(project(":api"))
    }
}
