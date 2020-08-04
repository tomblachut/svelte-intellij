import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.intellij.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    // https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.4.21"
    // https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.grammarkit") version "2020.2.1"
    // https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "0.4.0"
    // https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
val pluginName: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project

val platformType: String by project
val platformVersion: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

// https://plugins.jetbrains.com/plugin/11449-sass/versions/
val sassPlugin = when (platformVersion) {
    "2020.1.2" -> "org.jetbrains.plugins.sass:201.7846.80"
    "2020.2" -> "org.jetbrains.plugins.sass:202.6397.47"
    else -> throw IllegalArgumentException("Mussing Sass plugin version for platformVersion = $platformVersion")
}

// https://plugins.jetbrains.com/plugin/227-psiviewer/versions
val psiViewerPlugin = when {
    platformVersion.startsWith("2020.1") -> "PsiViewer:201.6251.22-EAP-SNAPSHOT.3"
    platformVersion.startsWith("2020.2") -> "PsiViewer:202-SNAPSHOT.3"
    else -> null
}

val intellijPlugins = listOfNotNull("JavaScriptLanguage", "CSS", sassPlugin, psiViewerPlugin)

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

sourceSets.main {
    java.srcDirs("src/main/java", "src/main/gen")
}

// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = pluginName
    version = platformVersion
    type = platformType
    downloadSources = platformDownloadSources.toBoolean()
    updateSinceUntilBuild = true

    //  https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html
    setPlugins(*intellijPlugins.toTypedArray())
}

ktlint {
    disabledRules.set(setOf("no-wildcard-imports", "import-ordering"))
}

val generateLexer = task<GenerateLexer>("generateLexer") {
    source = "src/main/java/dev/blachut/svelte/lang/parsing/html/SvelteHtmlLexer.flex"
    targetDir = "src/main/gen/dev/blachut/svelte/lang/parsing/html"
    targetClass = "_SvelteHtmlLexer"
    purgeOldFiles = true
}

tasks {
    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    listOf("compileKotlin", "compileTestKotlin").forEach {
        getByName<KotlinCompile>(it) {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    withType<KotlinCompile> {
        dependsOn(generateLexer)
    }

    patchPluginXml {
        version(pluginVersion)
        sinceBuild(pluginSinceBuild)
        untilBuild(pluginUntilBuild)

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription(closure {
            File("./README.md").readText().lines().run {
                subList(indexOf("<!-- Plugin description -->") + 1, indexOf("<!-- Plugin description end -->"))
            }.joinToString("\n").run { markdownToHTML(this) }
        })

        // Get the latest available change notes from the changelog file
        changeNotes(closure {
            changelog.getLatest().toHTML()
        })
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token(System.getenv("PUBLISH_TOKEN"))
        channels(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
    }

    withType<RunIdeTask> {
        // Disable auto plugin reloading. See `com.intellij.ide.plugins.DynamicPluginVfsListener`
        // jvmArgs("-Didea.auto.reload.plugins=false")
        // uncomment if `unexpected exception ProcessCanceledException` prevents you from debugging a running IDE
        // jvmArgs("-Didea.ProcessCanceledException=disabled")
    }
}
