package dev.blachut.svelte.lang

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.javascript.BaseJSCompletionTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy

private const val SVELTE_TEST_DATA_PATH = "/svelte/src/test/resources"

fun getRelativeSvelteTestDataPath(): String {
  return "/plugins$SVELTE_TEST_DATA_PATH"
}

fun getSvelteTestDataPath(): String {
  return "${IdeaTestExecutionPolicy.getHomePathWithPolicy()}/plugins$SVELTE_TEST_DATA_PATH"
}

internal fun CodeInsightTestFixture.copyBundledNpmPackage(packageName: String) {
  copyDirectoryToProject("dev/blachut/svelte/lang/_npm/$packageName", "node_modules/$packageName")
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