package dev.blachut.svelte.lang.parsing.html

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.lexerFixture
import com.intellij.testFramework.junit5.fixture.TestFixtures
import org.junit.jupiter.api.Test

@TestFixtures
class SvelteDirectiveLexerTest {
  private val lexer by lexerFixture("") { SvelteDirectiveLexer() }

  @Test
  fun testOn() = lexer.doTest("on:click|once|capture", """
    JS:IDENTIFIER ('on:')
    JS:IDENTIFIER ('click')
    JS:OR ('|')
    XML_NAME ('once')
    JS:OR ('|')
    XML_NAME ('capture')
  """.trimIndent())

  @Test
  fun testLet() = lexer.doTest("let:slottedVariable", """
    JS:IDENTIFIER ('let:')
    JS:IDENTIFIER ('slottedVariable')
  """.trimIndent())

  @Test
  fun testUseDotted() = lexer.doTest("use:obj.action", """
    JS:IDENTIFIER ('use:')
    JS:IDENTIFIER ('obj')
    JS:DOT ('.')
    JS:IDENTIFIER ('action')
  """.trimIndent())

  @Test
  fun testOutDottedWithModifiers() = lexer.doTest("out:obj.fade|local", """
    JS:IDENTIFIER ('out:')
    JS:IDENTIFIER ('obj')
    JS:DOT ('.')
    JS:IDENTIFIER ('fade')
    JS:OR ('|')
    XML_NAME ('local')
  """.trimIndent())
}
