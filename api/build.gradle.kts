import com.github.jengelman.gradle.plugins.shadow.ShadowExtension

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