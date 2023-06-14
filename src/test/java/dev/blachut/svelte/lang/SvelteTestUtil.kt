package dev.blachut.svelte.lang

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import java.io.File

private const val SVELTE_TEST_DATA_PATH = "/svelte/src/test/resources"

fun getRelativeSvelteTestDataPath(): String {
  return "/plugins$SVELTE_TEST_DATA_PATH"
}

fun getSvelteTestDataPath(): String {
  return "${IdeaTestExecutionPolicy.getHomePathWithPolicy()}/plugins$SVELTE_TEST_DATA_PATH"
}