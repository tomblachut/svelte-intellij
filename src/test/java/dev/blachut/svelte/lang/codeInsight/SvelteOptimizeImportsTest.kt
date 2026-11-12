package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.actions.OptimizeImportsAction
import com.intellij.ide.DataManager
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import dev.blachut.svelte.lang.SvelteTestModule
import dev.blachut.svelte.lang.configureSvelteDependencies
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Optimize Imports reaches `.svelte` files through `ES6ScriptTagImportOptimizer`, registered on
 * the HTML language. It asks `ES6UnusedImportsHelper` directly and reads no inspection profile,
 * so the tests enable no inspection. A false "unused" verdict deletes working code. That is the
 * half of WEB-57512 that users hit through reformat-on-save.
 */
@TestApplication
class SvelteOptimizeImportsTest {
  private val projectFixture = svelteProjectFixture()
  private val myFixture by svelteCodeInsightFixture(projectFixture)

  @Test
  fun testKeepsPackageComponentImport() {
    myFixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5, SvelteTestModule.SVELTE_FA_4)
    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import Fa from 'svelte-fa';
      </script>

      <Fa />
    """.trimIndent())

    optimizeImports()

    val text = runReadActionBlocking { myFixture.editor.document.text }
    assertTrue(text.contains("import Fa from"), "import used only by <Fa /> must survive Optimize Imports, got:\n$text")
  }

  @Test
  fun testRemovesTrulyUnusedImport() {
    myFixture.configureByText("Unused.svelte", "<div>unused</div>")
    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import Unused from './Unused.svelte';
      </script>

      <div>no components here</div>
    """.trimIndent())

    optimizeImports()

    assertFalse(runReadActionBlocking { myFixture.editor.document.text }.contains("import Unused"), "genuinely unused import should still be removed")
  }

  private fun optimizeImports() {
    runInEdtAndWait {
      OptimizeImportsAction.actionPerformedImpl(DataManager.getInstance().getDataContext(myFixture.editor.contentComponent))
    }
  }
}
