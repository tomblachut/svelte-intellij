package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.actions.OptimizeImportsAction
import com.intellij.ide.DataManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.SvelteTestModule
import dev.blachut.svelte.lang.configureSvelteDependencies
import dev.blachut.svelte.lang.getSvelteTestDataPath

/**
 * Optimize Imports reaches `.svelte` files through `ES6ScriptTagImportOptimizer`, registered on
 * the HTML language. It asks `ES6UnusedImportsHelper` directly and reads no inspection profile,
 * so the tests enable no inspection. A false "unused" verdict deletes working code. That is the
 * half of WEB-57512 that users hit through reformat-on-save.
 */
class SvelteOptimizeImportsTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getSvelteTestDataPath()

  fun testKeepsPackageComponentImport() {
    myFixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5, SvelteTestModule.SVELTE_FA_4)
    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import Fa from 'svelte-fa';
      </script>

      <Fa />
    """.trimIndent())

    optimizeImports()

    val text = myFixture.editor.document.text
    assertTrue("import used only by <Fa /> must survive Optimize Imports, got:\n$text", text.contains("import Fa from"))
  }

  fun testRemovesTrulyUnusedImport() {
    myFixture.configureByText("Unused.svelte", "<div>unused</div>")
    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import Unused from './Unused.svelte';
      </script>

      <div>no components here</div>
    """.trimIndent())

    optimizeImports()

    assertFalse("genuinely unused import should still be removed", myFixture.editor.document.text.contains("import Unused"))
  }

  private fun optimizeImports() {
    OptimizeImportsAction.actionPerformedImpl(DataManager.getInstance().getDataContext(myFixture.editor.contentComponent))
  }
}
