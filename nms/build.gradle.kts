subprojects {
    dependencies {
        compileOnly(project(":platform"))
        compileOnly(project(":scheduler"))
        compileOnly(project(":plugin"))
        compileOnly(project(":api"))
    }
}
