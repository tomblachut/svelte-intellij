package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.fixtures.CompletionAutoPopupTestCase
import dev.blachut.svelte.lang.SvelteTestScenario
import dev.blachut.svelte.lang.doTestWithLangFromTestNameSuffix
import junit.framework.TestCase

class SvelteAutoPopupTest : CompletionAutoPopupTestCase() {
  fun testAfterPrefix() {
    myFixture.configureByText("Example.svelte", """<div on<caret>>""")
    type(":")
    TestCase.assertNotNull(lookup)
    assertContainsElements(myFixture.lookupElementStrings!!, listOf("click"))
  }

  fun testOnModifiers() {
    myFixture.configureByText("Example.svelte", """<div on:click<caret>>""")
    type("|")
    TestCase.assertNotNull(lookup)
    type("p")
    assertContainsElements(myFixture.lookupElementStrings!!, listOf("nonpassive", "stopPropagation"))
  }

  fun testSvelteExpression() {
    myFixture.configureByText("Example.svelte", """text <caret>""")
    type("{")
    TestCase.assertNotNull(lookup)
    assertContainsElements(myFixture.lookupElementStrings!!, listOf("#if", "@html", "true"))
  }

  fun testSvelteExpression2() {
    myFixture.configureByText("Example.svelte", """{<caret>}""")
    type("#")
    TestCase.assertNotNull(lookup)
    assertContainsElements(myFixture.lookupElementStrings!!, listOf("#if", "true"))
    assertDoesntContain(myFixture.lookupElementStrings!!, listOf("@html"))
  }

  fun testStoreSubscriptionJS() = doTestWithLangFromTestNameSuffix(storeSubscription)
  fun testStoreSubscriptionTS() = doTestWithLangFromTestNameSuffix(storeSubscription)

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
    assertContainsElements(myFixture.lookupElementStrings!!, listOf("count"))
  }
}
