package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

class SvelteDirectiveLexerTest : LexerTestCase() {
  override fun getDirPath(): String = throw NotImplementedError()

  override fun createLexer(): Lexer {
    return SvelteDirectiveLexer()
  }

  fun testOn() = doTest("on:click|once|capture", """
    JS:IDENTIFIER ('on:')
    JS:IDENTIFIER ('click')
    JS:OR ('|')
    XML_NAME ('once')
    JS:OR ('|')
    XML_NAME ('capture')
  """.trimIndent())

  fun testLet() = doTest("let:slottedVariable", """
    JS:IDENTIFIER ('let:')
    JS:IDENTIFIER ('slottedVariable')
  """.trimIndent())

  fun testUseDotted() = doTest("use:obj.action", """
    JS:IDENTIFIER ('use:')
    JS:IDENTIFIER ('obj')
    JS:DOT ('.')
    JS:IDENTIFIER ('action')
  """.trimIndent())

  fun testOutDottedWithModifiers() = doTest("out:obj.fade|local", """
    JS:IDENTIFIER ('out:')
    JS:IDENTIFIER ('obj')
    JS:DOT ('.')
    JS:IDENTIFIER ('fade')
    JS:OR ('|')
    XML_NAME ('local')
  """.trimIndent())
}
