package dev.blachut.svelte.lang

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [SvelteLangMode] detection and persistence.
 */
@TestApplication
class SvelteLangModeTest {
  companion object {
    private val projectFixture = svelteProjectFixture()
  }

  private val myFixture by svelteCodeInsightFixture(projectFixture)

  @Test
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
    assertEquals(SvelteLangMode.NO_TS, runReadActionBlocking { svelteFile.langMode })
  }

  @Test
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
    assertEquals(SvelteLangMode.HAS_TS, runReadActionBlocking { svelteFile.langMode })
  }

  @Test
  fun testLangModeDetectionTypeScriptLong() {
    val file = myFixture.configureByText("Test.svelte", """
      <script lang="typescript">
        let count: number = 0;
      </script>
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.HAS_TS, runReadActionBlocking { svelteFile.langMode })
  }

  @Test
  fun testLangModeDetectionJavaScriptExplicit() {
    val file = myFixture.configureByText("Test.svelte", """
      <script lang="js">
        let count = 0;
      </script>
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.NO_TS, runReadActionBlocking { svelteFile.langMode })
  }

  @Test
  fun testGetLatestKnownLangFromProjectAndFile() {
    val virtualFile = myFixture.addFileToProject("LangModeHelper.svelte", """
      <script lang="ts">
        export let value: string;
      </script>
    """.trimIndent()).virtualFile

    // Test the helper method used by SyntaxHighlighterFactory
    val langMode = runReadActionBlocking { SvelteLangMode.getLatestKnownLang(myFixture.project, virtualFile) }
    assertEquals(SvelteLangMode.HAS_TS, langMode)
  }

  @Test
  fun testGetLatestKnownLangNullProject() {
    val langMode = SvelteLangMode.getLatestKnownLang(null, null)
    assertEquals(SvelteLangMode.DEFAULT, langMode)
  }

  @Test
  fun testCanonicalAttrValue() {
    assertEquals("ts", SvelteLangMode.HAS_TS.canonicalAttrValue)
    assertEquals("js", SvelteLangMode.NO_TS.canonicalAttrValue)
    assertEquals("js", SvelteLangMode.PENDING.canonicalAttrValue)
  }

  @Test
  fun testFromAttrValue() {
    assertEquals(SvelteLangMode.HAS_TS, SvelteLangMode.fromAttrValue("ts"))
    assertEquals(SvelteLangMode.HAS_TS, SvelteLangMode.fromAttrValue("typescript"))
    assertEquals(SvelteLangMode.NO_TS, SvelteLangMode.fromAttrValue("js"))
    assertEquals(SvelteLangMode.NO_TS, SvelteLangMode.fromAttrValue("javascript"))
    assertEquals(SvelteLangMode.NO_TS, SvelteLangMode.fromAttrValue(null))
    assertEquals(SvelteLangMode.NO_TS, SvelteLangMode.fromAttrValue("unknown"))
  }

  // region Multiple script tags tests

  @Test
  fun testMultipleScriptsModuleJsInstanceTs() {
    val file = myFixture.configureByText("Test.svelte", """
      <script context="module">
        export const MODULE_CONST = 'module';
      </script>

      <script lang="ts">
        let count: number = 0;
      </script>

      {#if count > 0}
        <p>Yes</p>
      {/if}
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.HAS_TS, runReadActionBlocking { svelteFile.langMode }, "Any TS script should make the whole file TS")
  }

  @Test
  fun testMultipleScriptsModuleTsInstanceJs() {
    val file = myFixture.configureByText("Test.svelte", """
      <script context="module" lang="ts">
        export const MODULE_CONST: string = 'module';
      </script>

      <script>
        let count = 0;
      </script>

      {#if count > 0}
        <p>Yes</p>
      {/if}
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.HAS_TS, runReadActionBlocking { svelteFile.langMode }, "Any TS script should make the whole file TS")
  }

  @Test
  fun testMultipleScriptsBothJs() {
    val file = myFixture.configureByText("Test.svelte", """
      <script context="module">
        export const MODULE_CONST = 'module';
      </script>

      <script>
        let count = 0;
      </script>

      {#if count > 0}
        <p>Yes</p>
      {/if}
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.NO_TS, runReadActionBlocking { svelteFile.langMode }, "Two JS scripts should stay NO_TS")
  }

  @Test
  fun testMultipleScriptsBothTs() {
    val file = myFixture.configureByText("Test.svelte", """
      <script context="module" lang="ts">
        export const MODULE_CONST: string = 'module';
      </script>

      <script lang="ts">
        let count: number = 0;
      </script>

      {#if count > 0}
        <p>Yes</p>
      {/if}
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.HAS_TS, runReadActionBlocking { svelteFile.langMode })
  }

  // endregion

  // region Reparse on lang attribute change

  @Test
  fun testLangModeChangesFromJsToTsOnTyping() {
    myFixture.configureByText("Test.svelte", """
      <script lang="<caret>js">
        let count = 0;
      </script>

      {#if count > 0}
        <p>Yes</p>
      {/if}
    """.trimIndent())

    assertEquals(SvelteLangMode.NO_TS, runReadActionBlocking { (myFixture.file as SvelteHtmlFile).langMode })

    // Select "js" and replace with "ts" — avoids broken intermediate HTML from unmatched quotes
    runInEdtAndWait {
      myFixture.editor.selectionModel.setSelection(
        myFixture.caretOffset,
        myFixture.caretOffset + 2
      )
    }
    myFixture.type("ts")
    runInEdtAndWait { PsiDocumentManager.getInstance(myFixture.project).commitAllDocuments() }

    assertEquals(SvelteLangMode.HAS_TS, runReadActionBlocking { (myFixture.file as SvelteHtmlFile).langMode })
  }

  @Test
  fun testLangModeChangesFromTsToJsOnTyping() {
    myFixture.configureByText("Test.svelte", """
      <script lang="<caret>ts">
        let count = 0;
      </script>

      {#if count > 0}
        <p>Yes</p>
      {/if}
    """.trimIndent())

    assertEquals(SvelteLangMode.HAS_TS, runReadActionBlocking { (myFixture.file as SvelteHtmlFile).langMode })

    runInEdtAndWait {
      myFixture.editor.selectionModel.setSelection(
        myFixture.caretOffset,
        myFixture.caretOffset + 2
      )
    }
    myFixture.type("js")
    runInEdtAndWait { PsiDocumentManager.getInstance(myFixture.project).commitAllDocuments() }

    assertEquals(SvelteLangMode.NO_TS, runReadActionBlocking { (myFixture.file as SvelteHtmlFile).langMode })
  }

  // endregion

  // region Highlighting transition tests

  @Test
  fun testKeywordHighlightingInJavaScriptMode() {
    val file = myFixture.configureByText("Test.svelte", """
      <script>
        let <symbolName descr="identifiers//local variable">items</symbolName> = [];
      </script>

      {#each <symbolName descr="identifiers//local variable">items</symbolName> <info descr="null">as</info> <symbolName descr="identifiers//parameter">item</symbolName>}
        <p>{<symbolName descr="identifiers//parameter">item</symbolName>}</p>
      {/each}
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.NO_TS, runReadActionBlocking { svelteFile.langMode })

    JSTestUtils.checkHighlightingWithSymbolNames(myFixture, false, false, true)
  }

  @Test
  fun testKeywordHighlightingInTypeScriptMode() {
    // Use `satisfies` - a TypeScript-only keyword - to verify TS highlighting works in markup expressions
    val file = myFixture.configureByText("Test.svelte", """
      <script lang="ts">
        <info descr="null">type</info> <symbolName descr="types//type alias">Item</symbolName> = { <symbolName descr="TypeScript property signature">name</symbolName>: <info descr="null">string</info> };
        let <symbolName descr="identifiers//local variable">items</symbolName>: <symbolName descr="types//type alias">Item</symbolName>[] = [];
      </script>

      {#each <symbolName descr="identifiers//local variable">items</symbolName> <info descr="null">as</info> <symbolName descr="identifiers//parameter">item</symbolName>}
        {#if <symbolName descr="identifiers//parameter">item</symbolName> <info descr="null">satisfies</info> <symbolName descr="types//type alias">Item</symbolName>}
          <p>{<symbolName descr="identifiers//parameter">item</symbolName>.<symbolName descr="instance field">name</symbolName>}</p>
        {/if}
      {/each}
    """.trimIndent())

    val svelteFile = file as SvelteHtmlFile
    assertEquals(SvelteLangMode.HAS_TS, runReadActionBlocking { svelteFile.langMode })

    JSTestUtils.checkHighlightingWithSymbolNames(myFixture, false, false, true)
  }

  // endregion
}
