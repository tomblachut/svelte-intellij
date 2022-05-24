import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    id("org.jetbrains.intellij") version "1.6.0"
    id("org.jetbrains.changelog") version "1.3.1"
    id("org.jetbrains.grammarkit") version "2021.2.1"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// https://plugins.jetbrains.com/plugin/11449-sass/versions/
val sassPlugin = when {
    properties("platformVersion").startsWith("212") -> "org.jetbrains.plugins.sass:212.4746.57"
    properties("platformVersion").startsWith("213") -> "org.jetbrains.plugins.sass:213.5744.269"
    properties("platformVersion").startsWith("221") -> "org.jetbrains.plugins.sass:221.5591.52"
    properties("platformVersion").startsWith("222") -> "org.jetbrains.plugins.sass:222.2270.35"
    else -> throw GradleException("Missing Sass plugin version for platformVersion = ${properties("platformVersion")}")
}

// https://plugins.jetbrains.com/plugin/227-psiviewer/versions
val psiViewerPlugin = when {
    properties("platformVersion").startsWith("212") -> "PsiViewer:212-SNAPSHOT"
    properties("platformVersion").startsWith("213") -> "PsiViewer:213-SNAPSHOT"
    properties("platformVersion").startsWith("221") -> "PsiViewer:221-SNAPSHOT"
    properties("platformVersion").startsWith("222") -> "PsiViewer:222-SNAPSHOT"

    else -> null
}

val intellijPlugins = listOfNotNull(
    "JavaScript",
    "JavaScriptDebugger",
    "JSIntentionPowerPack",
    "IntelliLang",
    "HtmlTools",
    "CSS",
    sassPlugin,
    psiViewerPlugin
)

repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDirs("src/main/java", "src/main/gen")
}

// https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html
    plugins.set(intellijPlugins)
}

// https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// https://github.com/JetBrains/gradle-grammar-kit-plugin
val generateSvelteLexer = task<GenerateLexerTask>("generateSvelteLexer") {
    source.set("src/main/java/dev/blachut/svelte/lang/parsing/html/SvelteHtmlLexer.flex")
    targetDir.set("src/main/gen/dev/blachut/svelte/lang/parsing/html")
    targetClass.set("_SvelteHtmlLexer")
    purgeOldFiles.set(true)
}

tasks {
    val javaVersion = "11"

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        dependsOn(generateSvelteLexer)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    withType<KotlinCompile> {
        dependsOn(generateSvelteLexer)
        kotlinOptions.jvmTarget = javaVersion
        kotlinOptions.languageVersion = "1.5"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=compatibility")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )
        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.run {
                getOrNull(properties("pluginVersion")) ?: getLatest()
            }.toHTML()
        })
    }

    runPluginVerifier {
        ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    // https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }

    withType<RunIdeTask> {
        // systemProperty("ide.plugins.snapshot.on.unload.fail", true)
        // uncomment if `unexpected exception ProcessCanceledException` prevents you from debugging a running IDE
        // jvmArgs("-Didea.ProcessCanceledException=disabled")
    }
}
