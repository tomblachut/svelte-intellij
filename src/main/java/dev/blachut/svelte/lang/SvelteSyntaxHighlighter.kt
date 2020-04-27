package dev.blachut.svelte.lang

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlHighlightingLexer
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

internal class SvelteSyntaxHighlighter(private val jsLanguageLevel: JSLanguageLevel) : HtmlFileHighlighter() {
    override fun getHighlightingLexer(): Lexer {
        return SvelteHtmlHighlightingLexer(jsLanguageLevel)
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            JSTokenTypes.SHARP,
            JSTokenTypes.COLON,
            JSTokenTypes.DIV,
            JSTokenTypes.AT,

            SvelteTokenTypes.EACH_KEYWORD,
            SvelteTokenTypes.THEN_KEYWORD,

            SvelteTokenTypes.START_MUSTACHE,
            SvelteTokenTypes.START_MUSTACHE_TEMP,
            SvelteTokenTypes.END_MUSTACHE -> KEYS
            else -> super.getTokenHighlights(tokenType)
        }
    }

    companion object {
        private val KEY = createTextAttributesKey("SVELTE_KEY", DefaultLanguageHighlighterColors.KEYWORD)
        private val KEYS = arrayOf(KEY)
    }
}
