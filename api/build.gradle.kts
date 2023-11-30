dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
//    publishing {
//        publications {
//            create<MavenPublication>("shadow") {
//                project.extensions.configure<ShadowExtension> {
//                    component(this@create)
//                }
//            }
//        }
//    }
}
