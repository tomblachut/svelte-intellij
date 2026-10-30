package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.javascript.JSFindUsagesFixture.Companion.jsFindUsagesFixture
import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.SvelteTestModule
import dev.blachut.svelte.lang.SvelteTestScenario
import dev.blachut.svelte.lang.configureBundledSvelte
import dev.blachut.svelte.lang.configureSvelteDependencies
import dev.blachut.svelte.lang.getSvelteTestDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class SvelteFindUsagesTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsFindUsagesFixture(projectFixture, JSFixtureOptions(getSvelteTestDataPath(), "js"))

  @Test
  fun testStoreImportedJS(): Unit = storeImported.perform(js.getTestName(false))

  @Test
  fun testStoreImportedTS(): Unit = storeImported.perform(js.getTestName(false))

  private val storeImported = SvelteTestScenario { langExt, _ ->
    js.fixture.configureBundledSvelte()
    val count = "\$count" // to trick Kotlin
    js.fixture.configureByText("Foo.svelte", """
      <script lang="$langExt" module>
        import { count } from "./stores";

        count.set(5);
        console.log($count.toFixed()); // bug, todo prevent resolve
      </script>
      
      <script lang="$langExt">
        count.set(5);
        console.log($count.toFixed());
        
        $: $count.toFixed();
        
	      console.log({ $count }); // the highlight is missing, todo fix
      </script>

      <main>{$count}</main>
      <section>{count}</section>
    """.trimIndent())
    js.fixture.configureByText("stores.$langExt", """
      import { writable } from "svelte/store";
    
      export const <caret>count = writable(5);
    """.trimIndent())
    doTest()
  }

  private fun doTest() {
    val usages = js.doFindUsages()
    assertEquals(8, usages.size) // 8 + 1 false positive - 1 false negative
  }

  /**
   * WEB-57512: Find Usages passes a project-wide [com.intellij.psi.search.GlobalSearchScope]. The
   * template usage must be found on that path too. The unused-imports check covers only the
   * `<script>`-scoped path.
   */
  @Test
  fun testPackageComponentUsages() {
    js.fixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5, SvelteTestModule.SVELTE_FA_4)
    js.fixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import <caret>Fa from 'svelte-fa';
      </script>

      <Fa />
    """.trimIndent())
    assertEquals(1, js.doFindUsages().size)
  }

  /**
   * The name-match fallback serves the import only. The tag `<Fa />` belongs to the import, so a
   * search for the same-named `{#each}` variable must not report it.
   */
  @Test
  fun testEachVariableUsagesExcludeTag() {
    js.fixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5, SvelteTestModule.SVELTE_FA_4)
    js.fixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import Fa from 'svelte-fa';
        const items: string[] = [];
      </script>

      <Fa />

      {#each items as <caret>Fa}
        <span>{Fa}</span>
      {/each}
    """.trimIndent())
    assertEquals(1, js.doFindUsages().size)
  }

  /** ReferencesSearch must keep its results inside the scope the caller passed. */
  @Test
  fun testImportSearchRespectsCallerScope() {
    js.fixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5, SvelteTestModule.SVELTE_FA_4)
    js.fixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import <caret>Fa from 'svelte-fa';
      </script>

      <Fa />
    """.trimIndent())
    val binding = timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.elementAtCaret } }
    val importDeclaration = PsiTreeUtil.getParentOfType(binding, ES6ImportDeclaration::class.java)!!
    val references = runReadActionBlocking { ReferencesSearch.search(binding, LocalSearchScope(importDeclaration)).findAll() }
    assertTrue(references.isEmpty())
  }

  @Test
  fun testFindUsagesOfCssClassIncludesSvelteElement() {
    js.fixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-class"></svelte:element>
      <style>
        .my-cl<caret>ass { color: red; }
      </style>
    """.trimIndent())
    val usages = js.fixture.findUsages(timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.elementAtCaret } })
    val svelteElementUsages = runReadActionBlocking {
      usages.filter { usage -> PsiTreeUtil.getParentOfType(usage.element, XmlTag::class.java)?.name == "svelte:element" }
    }
    assertEquals(1, svelteElementUsages.size, "Expected exactly one usage inside <svelte:element>")
  }

  @Test
  fun testFindUsagesOfCssClassInMultiClassAttribute() {
    js.fixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-class other-class"></svelte:element>
      <style>
        .my-cl<caret>ass { color: red; }
        .other-class { color: blue; }
      </style>
    """.trimIndent())
    val usages = js.fixture.findUsages(timeoutRunBlocking { withContext(Dispatchers.EDT) { js.fixture.elementAtCaret } })
    val svelteElementUsages = runReadActionBlocking {
      usages.filter { usage -> PsiTreeUtil.getParentOfType(usage.element, XmlTag::class.java)?.name == "svelte:element" }
    }
    assertEquals(1, svelteElementUsages.size, "Expected exactly one usage of .my-class (not .other-class)")
  }
}