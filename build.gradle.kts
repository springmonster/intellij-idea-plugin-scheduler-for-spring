import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.date

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.0.0"
}

group = "com.khch"
version = "1.0.1"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.1.4")
    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks {

    patchPluginXml {
        version.set("${project.version}")
        sinceBuild.set("193.7288.26")
        untilBuild.set("223.*")

        changeNotes.set(provider {
            changelog.renderItem(
                changelog
                    .getUnreleased()
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML
            )
        })
    }
}

changelog {
    version.set("1.0.1")
    path.set(file("CHANGELOG.md").canonicalPath)
    header.set(provider { "[${version.get()}] - ${date()}" })
    headerParserRegex.set("""(\d+\.\d+)""".toRegex())
    introduction.set(
        """
        Add new features:
        
        - Support Kotlin based Spring Boot project which uses @Scheduled annotation
        """.trimIndent()
    )
    itemPrefix.set("-")
    keepUnreleasedSection.set(false)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
    lineSeparator.set("\n")
    combinePreReleases.set(true)
}
