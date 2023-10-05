import com.github.jengelman.gradle.plugins.shadow.ShadowExtension

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("shadow") {
                project.extensions.configure<ShadowExtension> {
                    component(this@create)
                }
            }
        }
    }
}
