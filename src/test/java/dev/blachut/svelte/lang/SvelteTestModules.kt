package dev.blachut.svelte.lang

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.javascript.testFramework.web.configureDependencies
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

enum class SvelteTestModule(
  private val location: String,
  private vararg val myPackageNames: String,
) : WebFrameworkTestModule {
  SVELTE_5("svelte/5", "svelte"),
  SVELTE_KIT_1("sveltejs-kit/1", "@sveltejs/kit"),
  ;

  override val packageNames: List<String>
    get() = myPackageNames.toList().ifEmpty { listOf(location) }

  override val folder: String
    get() = "$location/node_modules"
}

fun CodeInsightTestFixture.configureSvelteDependencies(
  vararg modules: SvelteTestModule = arrayOf(SvelteTestModule.SVELTE_5),
  additionalDependencies: Map<String, String> = emptyMap(),
) {
  configureDependencies(
    testDataRoot = getSvelteTestDataPath(),
    defaultDependencies = mapOf("svelte" to "*") + additionalDependencies,
    *modules,
  )
}
