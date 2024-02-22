package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SvelteBreadcrumbsTest : BasePlatformTestCase() {
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

    val breadcrumbs = myFixture.breadcrumbsAtCaret
    val texts = breadcrumbs.map { it.text }
    UsefulTestCase.assertOrderedEquals(texts, listOf("div", "svelte:element", "{#if}", "Bar", "span"))
  }
}