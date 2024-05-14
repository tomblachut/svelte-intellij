package dev.blachut.svelte.lang

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.javascript.BaseJSCompletionTestCase
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