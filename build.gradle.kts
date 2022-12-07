plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.0"
}

group = "com.khch"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.1.4")
//    type.set("IC") // Target IDE Platform

//    plugins.set(listOf(/* Plugin Dependencies */))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks {

    patchPluginXml {
        version.set("${project.version}")
        sinceBuild.set("221")
        untilBuild.set("223.*")
    }
}
