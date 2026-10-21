package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightModuleFixture
import com.intellij.testFramework.fixtures.CompletionAutoPopupTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import dev.blachut.svelte.lang.SvelteTestScenario
import dev.blachut.svelte.lang.SvelteTestScriptLang
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class SvelteAutoPopupTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val myFixture by codeInsightModuleFixture(projectFixture)

  private val lookup: LookupImpl? get() = CompletionAutoPopupTestUtil.getLookup(myFixture)

  private fun type(s: String) = CompletionAutoPopupTestUtil.type(myFixture, s)

  private fun runTest(block: () -> Unit) = CompletionAutoPopupTestUtil.runWithAutoPopupEnabled<Throwable>(myFixture, block)

  @Test
  fun testAfterPrefix(): Unit = runTest {
    myFixture.configureByText("Example.svelte", """<div on<caret>>""")
    type(":")
    assertNotNull(lookup)
    assertTrue(myFixture.lookupElementStrings!!.containsAll(listOf("click")))
  }

  @Test
  fun testOnModifiers(): Unit = runTest {
    myFixture.configureByText("Example.svelte", """<div on:click<caret>>""")
    type("|")
    assertNotNull(lookup)
    type("p")
    assertTrue(myFixture.lookupElementStrings!!.containsAll(listOf("nonpassive", "stopPropagation")))
  }

  @Test
  fun testSvelteExpression(): Unit = runTest {
    myFixture.configureByText("Example.svelte", """text <caret>""")
    type("{")
    assertNotNull(lookup)
    assertTrue(myFixture.lookupElementStrings!!.containsAll(listOf("#if", "@html", "true")))
  }

  @Test
  fun testSvelteExpression2(): Unit = runTest {
    myFixture.configureByText("Example.svelte", """{<caret>}""")
    type("#")
    assertNotNull(lookup)
    assertTrue(myFixture.lookupElementStrings!!.containsAll(listOf("#if", "true")))
    assertFalse(myFixture.lookupElementStrings!!.contains("@html"))
  }

  @Test
  fun testStoreSubscriptionJS(): Unit = runTest { storeSubscription.perform(SvelteTestScriptLang.JS) }

  @Test
  fun testStoreSubscriptionTS(): Unit = runTest { storeSubscription.perform(SvelteTestScriptLang.TS) }

  private val storeSubscription = SvelteTestScenario { langExt, _ ->
    myFixture.configureByText("Example.svelte", """
      <script lang="$langExt">
        import { writable } from 'svelte/store';

        const count = writable(5);
      </script>

      <section>{<caret>}</section>
    """.trimIndent())
    type("$")
    assertNotNull(lookup)
    assertTrue(myFixture.lookupElementStrings!!.containsAll(listOf("count")))
  }
}
