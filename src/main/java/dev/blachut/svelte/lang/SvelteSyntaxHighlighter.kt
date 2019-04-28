package dev.blachut.svelte.lang

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.Lexer

internal class SvelteSyntaxHighlighter(private val jsLanguageLevel: JSLanguageLevel) : HtmlFileHighlighter() {
    override fun getHighlightingLexer(): Lexer {
        return SvelteHighlightingLexer(jsLanguageLevel)
    }
}