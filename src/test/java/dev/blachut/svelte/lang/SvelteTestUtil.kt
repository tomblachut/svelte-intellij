package dev.blachut.svelte.lang

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import java.io.File

private const val SVELTE_TEST_DATA_PATH = "/svelte/src/test/resources"

fun getSvelteTestDataPath(): String =
    getPluginsPath() + SVELTE_TEST_DATA_PATH

private fun getPluginsPath(): String {
  return IdeaTestExecutionPolicy.getHomePathWithPolicy() + "/plugins"
}