package dev.blachut.svelte.lang

import com.intellij.psi.PsiManager
import com.intellij.psi.stubs.StubUpdatingIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

/**
 * Tests for [SvelteLangMode] detection and persistence.
 */
class SvelteLangModeTest : BasePlatformTestCase() {

  fun testLangModeDetectionJavaScript() {
    val file = myFixture.configureByText("Test.svelte", """
      <script>
        let count = 0;
      </script>

      {#if count > 0}
        <p>Yes</p>
      {/if}
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.NO_TS, svelteFile.langMode)
  }

  fun testLangModeDetectionTypeScript() {
    val file = myFixture.configureByText("Test.svelte", """
      <script lang="ts">
        let count: number = 0;
      </script>

      {#if count satisfies number}
        <p>Yes</p>
      {/if}
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.HAS_TS, svelteFile.langMode)
  }

  fun testLangModeDetectionTypeScriptLong() {
    val file = myFixture.configureByText("Test.svelte", """
      <script lang="typescript">
        let count: number = 0;
      </script>
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.HAS_TS, svelteFile.langMode)
  }

  fun testLangModeDetectionJavaScriptExplicit() {
    val file = myFixture.configureByText("Test.svelte", """
      <script lang="js">
        let count = 0;
      </script>
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.NO_TS, svelteFile.langMode)
  }

  fun testLangModeStubPersistence() {
    // Create and parse a TypeScript file
    val virtualFile = myFixture.addFileToProject("TypeScriptFile.svelte", """
      <script lang="ts">
        let count: number = 0;
      </script>

      {#if count > 0}
        <p>Yes</p>
      {/if}
    """.trimIndent()).virtualFile

    // Ensure stubs are built
    FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null)

    // Get file via PsiManager (should use stub if available)
    val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as SvelteHtmlFile

    // Verify lang mode is correctly retrieved (either from stub or AST)
    assertEquals(SvelteLangMode.HAS_TS, psiFile.langMode)
  }

  fun testLangModeStubPersistenceJavaScript() {
    val virtualFile = myFixture.addFileToProject("JavaScriptFile.svelte", """
      <script>
        let count = 0;
      </script>
    """.trimIndent()).virtualFile

    FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null)

    val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as SvelteHtmlFile
    assertEquals(SvelteLangMode.NO_TS, psiFile.langMode)
  }

  fun testGetLatestKnownLangFromProjectAndFile() {
    val virtualFile = myFixture.addFileToProject("LangModeHelper.svelte", """
      <script lang="ts">
        export let value: string;
      </script>
    """.trimIndent()).virtualFile

    // Test the helper method used by SyntaxHighlighterFactory
    val langMode = SvelteLangMode.getLatestKnownLang(project, virtualFile)
    assertEquals(SvelteLangMode.HAS_TS, langMode)
  }

  fun testGetLatestKnownLangNullProject() {
    val langMode = SvelteLangMode.getLatestKnownLang(null, null)
    assertEquals(SvelteLangMode.DEFAULT, langMode)
  }

  fun testCanonicalAttrValue() {
    assertEquals("ts", SvelteLangMode.HAS_TS.canonicalAttrValue)
    assertEquals("js", SvelteLangMode.NO_TS.canonicalAttrValue)
    assertEquals("js", SvelteLangMode.PENDING.canonicalAttrValue)
  }

  fun testFromAttrValue() {
    assertEquals(SvelteLangMode.HAS_TS, SvelteLangMode.fromAttrValue("ts"))
    assertEquals(SvelteLangMode.HAS_TS, SvelteLangMode.fromAttrValue("typescript"))
    assertEquals(SvelteLangMode.NO_TS, SvelteLangMode.fromAttrValue("js"))
    assertEquals(SvelteLangMode.NO_TS, SvelteLangMode.fromAttrValue("javascript"))
    assertEquals(SvelteLangMode.NO_TS, SvelteLangMode.fromAttrValue(null))
    assertEquals(SvelteLangMode.NO_TS, SvelteLangMode.fromAttrValue("unknown"))
  }


}
