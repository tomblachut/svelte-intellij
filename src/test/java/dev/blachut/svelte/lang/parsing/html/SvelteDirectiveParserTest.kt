package dev.blachut.svelte.lang.parsing.html

import com.intellij.openapi.util.TextRange
import dev.blachut.svelte.lang.directives.SvelteDirectiveTypes
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil.Directive
import dev.blachut.svelte.lang.directives.SvelteDirectiveUtil.DirectiveSegment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SvelteDirectiveParserTest {
  @Test
  fun testNonDirectiveClass() {
    val result = SvelteDirectiveParser.parse("class")
    assertEquals(result, null)
  }

  @Test
  fun testOn() {
    assertEquals(
      SvelteDirectiveParser.parse("on:click|once|capture"),
      Directive(
        SvelteDirectiveTypes.ON,
        listOf(DirectiveSegment("click", TextRange(3, 8))),
        listOf(DirectiveSegment("once", TextRange(9, 13)), DirectiveSegment("capture", TextRange(14, 21)))
      )
    )
  }

  @Test
  fun testClass() {
    assertEquals(
      SvelteDirectiveParser.parse("class:foo"),
      Directive(
        SvelteDirectiveTypes.CLASS,
        listOf(DirectiveSegment("foo", TextRange(6, 9))),
        listOf()
      )
    )
  }

  @Test
  fun testUse() {
    assertEquals(
      SvelteDirectiveParser.parse("use:foo.bar"),
      Directive(
        SvelteDirectiveTypes.USE,
        listOf(DirectiveSegment("foo", TextRange(4, 7)), DirectiveSegment("bar", TextRange(8, 11))),
        listOf()
      )
    )
  }
}
