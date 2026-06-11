package dev.blachut.svelte.lang

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.javascript.testFramework.web.configureDependencies
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

enum class SvelteTestModule(
  private val location: String,
  private vararg val myPackageNames: String,
) : WebFrameworkTestModule {
  SVELTE_KIT_DEVALUE_4("devalue/4", "devalue"),
  SVELTE_KIT_ESM_ENV_1("esm-env/1", "esm-env"),
  SVELTE_KIT_KLEUR_4("kleur/4", "kleur"),
  SVELTE_KIT_MAGIC_STRING_0_30("magic-string/0.30", "magic-string"),
  SVELTE_KIT_MIME_3("mime/3", "mime"),
  SVELTE_KIT_SET_COOKIE_PARSER_2("set-cookie-parser/2", "set-cookie-parser"),
  SVELTE_KIT_SIRV_2("sirv/2", "sirv"),
  SVELTE_KIT_UNDICI_5("undici/5", "undici"),
  SVELTE_KIT_VITE_4("vite/4", "vite"),
  SVELTE_KIT_VITE_PLUGIN_SVELTE_2("sveltejs-vite-plugin-svelte/2", "@sveltejs/vite-plugin-svelte"),
  SVELTE_4("svelte/4.2.20", "svelte"),
  SVELTE_5("svelte/5", "svelte"),
  SVELTE_KIT_ADAPTER_AUTO_2("sveltejs-adapter-auto/2", "@sveltejs/adapter-auto"),
  SVELTE_KIT_1("sveltejs-kit/1", "@sveltejs/kit"),
  SVELTE_PREPROCESS_5("svelte-preprocess/5", "svelte-preprocess"),
  ;

  override val packageNames: List<String>
    get() = myPackageNames.toList().ifEmpty { listOf(location) }

  override val folder: String
    get() = "$location/node_modules"
}

internal val SVELTE_KIT_1_RUNTIME_DEPENDENCIES = arrayOf(
  SvelteTestModule.SVELTE_KIT_DEVALUE_4,
  SvelteTestModule.SVELTE_KIT_ESM_ENV_1,
  SvelteTestModule.SVELTE_KIT_KLEUR_4,
  SvelteTestModule.SVELTE_KIT_MAGIC_STRING_0_30,
  SvelteTestModule.SVELTE_KIT_MIME_3,
  SvelteTestModule.SVELTE_KIT_SET_COOKIE_PARSER_2,
  SvelteTestModule.SVELTE_KIT_SIRV_2,
  SvelteTestModule.SVELTE_KIT_UNDICI_5,
  SvelteTestModule.SVELTE_KIT_VITE_4,
  SvelteTestModule.SVELTE_KIT_VITE_PLUGIN_SVELTE_2,
)

fun CodeInsightTestFixture.configureSvelteDependencies(
  vararg modules: SvelteTestModule = arrayOf(SvelteTestModule.SVELTE_5),
  additionalDependencies: Map<String, String> = emptyMap(),
) {
  val hasSvelte4 = modules.contains(SvelteTestModule.SVELTE_4)
  val testDataRoot = getSvelteTestDataPath()
  modules.forEach { refreshDependencySource("$testDataRoot/node_modules/${it.folder}") }
  configureDependencies(
    testDataRoot = testDataRoot,
    defaultDependencies = mapOf("svelte" to if (hasSvelte4) "^4.0.5" else "*") + additionalDependencies,
    *modules,
  )
}

private fun refreshDependencySource(path: String) {
  LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
    ?.let { UsefulTestCase.refreshRecursively(it) }
}
