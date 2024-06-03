package dev.blachut.svelte.lang

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.javascript.BaseJSCompletionTestCase
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy

private const val SVELTE_TEST_DATA_PATH = "/svelte/src/test/resources"

// todo sync with solutions from Vue plugin
internal val svelteMinimalPackageJson = """
  {
    "devDependencies": {
      "svelte": "*"
    },
    "type": "module"
  }
""".trimIndent()

internal val svelteKitPackageJson = """
  {
    "name": "svelte-test",
    "version": "0.0.1",
    "private": true,
    "devDependencies": {
      "@sveltejs/adapter-auto": "^2.0.0",
      "@sveltejs/kit": "^1.20.4",
      "svelte": "^4.0.5",
      "svelte-check": "^3.4.3",
      "tslib": "^2.4.1",
      "typescript": "^5.0.0",
      "vite": "^4.4.2"
    },
    "type": "module"
  }
""".trimIndent()

fun getRelativeSvelteTestDataPath(): String {
  return "/plugins$SVELTE_TEST_DATA_PATH"
}

fun getSvelteTestDataPath(): String {
  return "${IdeaTestExecutionPolicy.getHomePathWithPolicy()}/plugins$SVELTE_TEST_DATA_PATH"
}

internal fun CodeInsightTestFixture.copyBundledNpmPackage(packageName: String) {
  copyDirectoryToProject("dev/blachut/svelte/lang/_npm/$packageName", "node_modules/$packageName")
}

internal fun CodeInsightTestFixture.configureBundledSvelte() {
  copyBundledNpmPackage("svelte")
  configureByText("package.json", svelteMinimalPackageJson)
}

internal fun CodeInsightTestFixture.copyBundledSvelteKit() {
  copyBundledNpmPackage("@sveltejs/kit")
}

internal fun CodeInsightTestFixture.checkCompletionContains(vararg variants: String) {
  val elements = completeBasic()
  BaseJSCompletionTestCase.checkWeHaveInCompletion(elements, *variants)
}

internal fun Array<LookupElement>.checkCompletionContains(vararg variants: String) {
  BaseJSCompletionTestCase.checkWeHaveInCompletion(this, *variants)
}

internal fun Array<LookupElement>.checkCompletionExcludes(vararg variants: String) {
  BaseJSCompletionTestCase.checkNoCompletion(this, *variants)
}

enum class SvelteTestScriptLang(val langExt: String, val langWarning: String) {
  JS("js", "weak_warning"),
  TS("ts", "error")
}

internal fun interface SvelteTestScenario {
  fun SvelteTestHelperContext.perform(langExt: String, langWarning: String)

  fun perform(lang: SvelteTestScriptLang) {
    SvelteTestHelperContext.perform(lang.langExt, lang.langWarning)
  }

  fun perform(testName: String) {
    val lang = getScriptLangFromTestNameSuffix(testName)
    perform(lang)
  }
}

internal fun UsefulTestCase.doTestWithLangFromTestNameSuffix(testScenarioFactory: SvelteTestScenario) {
  val testName = UsefulTestCase.getTestName(name, false)
  testScenarioFactory.perform(testName)
}

private fun getScriptLangFromTestNameSuffix(testName: String): SvelteTestScriptLang {
  val value = SvelteTestScriptLang.entries.find { testName.endsWith(it.name) }
  if (value == null) {
    throw IllegalArgumentException("Test name doesn't end with one of ${SvelteTestScriptLang.entries.joinToString(", ")}")
  }
  return value
}

@Suppress("ConstPropertyName")
internal object SvelteTestHelperContext {
  const val props = "\$props" // to trick Kotlin
  const val bindable = "\$bindable" // to trick Kotlin
  const val state = "\$state" // to trick Kotlin
  const val derived = "\$derived" // to trick Kotlin
  const val effect = "\$effect" // to trick Kotlin
  const val host = "\$host" // to trick Kotlin
}