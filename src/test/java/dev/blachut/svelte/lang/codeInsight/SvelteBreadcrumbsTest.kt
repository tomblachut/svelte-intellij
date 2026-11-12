package dev.blachut.svelte.lang.codeInsight

import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class SvelteBreadcrumbsTest {
  companion object {
    private val projectFixture = svelteProjectFixture()
  }

  private val myFixture by svelteCodeInsightFixture(projectFixture)

  @Test
  fun testSvelteBlock() {
    myFixture.configureByText("Sticky.svelte", """
      <div>
      	<svelte:element this="section">
      		{#if true && true}
      			<Bar>
      				<span>Hello<caret></span>
      			</Bar>
      		{:else}
      			<div>sorry</div>
      		{/if}
      	</svelte:element>
      </div>
    """.trimIndent())

    val texts = runReadActionBlocking {
      val breadcrumbs = myFixture.breadcrumbsAtCaret
      breadcrumbs.map { it.text }
    }
    assertEquals(listOf("div", "svelte:element", "{#if}", "Bar", "span"), texts)
  }
}