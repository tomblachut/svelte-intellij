import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.intellij.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    // https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.6.3"
    // https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.grammarkit") version "2020.2.1"
    // https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "0.6.2"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
@Suppress("PropertyName")
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

// https://plugins.jetbrains.com/plugin/11449-sass/versions/
val sassPlugin = when {
    platformVersion.startsWith("203") -> "org.jetbrains.plugins.sass:203.5981.98"
    else -> throw GradleException("Missing Sass plugin version for platformVersion = $platformVersion")
}

// https://plugins.jetbrains.com/plugin/227-psiviewer/versions
val psiViewerPlugin = when {
    platformVersion.startsWith("203") -> "PsiViewer:203-SNAPSHOT"
    else -> null
}

val intellijPlugins = listOfNotNull("JavaScriptLanguage", "CSS", "JavaScriptDebugger", sassPlugin, psiViewerPlugin)

repositories {
    mavenCentral()
    jcenter()
}

sourceSets.main {
    java.srcDirs("src/main/java", "src/main/gen")
}

// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = pluginName_
    version = platformVersion
    type = platformType
    downloadSources = platformDownloadSources.toBoolean()
    updateSinceUntilBuild = true

    //  https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html
    setPlugins(*intellijPlugins.toTypedArray())
}

changelog {
    groups = listOf("")
}

val generateLexer = task<GenerateLexer>("generateLexer") {
    source = "src/main/java/dev/blachut/svelte/lang/parsing/html/SvelteHtmlLexer.flex"
    targetDir = "src/main/gen/dev/blachut/svelte/lang/parsing/html"
    targetClass = "_SvelteHtmlLexer"
    purgeOldFiles = true
}

tasks {
    // Set the compatibility versions to 11
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    listOf("compileKotlin", "compileTestKotlin").forEach {
        getByName<KotlinCompile>(it) {
            kotlinOptions.jvmTarget = "11"
        }
    }

    withType<KotlinCompile> {
        dependsOn(generateLexer)
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=compatibility")
    }

    patchPluginXml {
        version(pluginVersion)
        sinceBuild(pluginSinceBuild)
        untilBuild(pluginUntilBuild)

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription(
            closure {
                File("./README.md").readText().lines().run {
                    val start = "<!-- Plugin description -->"
                    val end = "<!-- Plugin description end -->"

                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end))
                }.joinToString("\n").run { markdownToHTML(this) }
            }
        )
        // Get the latest available change notes from the changelog file
        changeNotes(
            closure {
                changelog.getLatest().toHTML()
            }
        )
    }

    runPluginVerifier {
        ideVersions(pluginVerifierIdeVersions)
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
        channels(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
    }

    withType<RunIdeTask> {
        // autoReloadPlugins = true
        // Disable auto plugin reloading. See `com.intellij.ide.plugins.DynamicPluginVfsListener`
        // jvmArgs("-Didea.auto.reload.plugins=false")
        // systemProperty("ide.plugins.snapshot.on.unload.fail", true)
        // uncomment if `unexpected exception ProcessCanceledException` prevents you from debugging a running IDE
        // jvmArgs("-Didea.ProcessCanceledException=disabled")
    }
}
