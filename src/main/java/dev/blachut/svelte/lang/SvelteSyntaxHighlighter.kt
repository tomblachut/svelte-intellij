package dev.blachut.svelte.lang

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.psi.SvelteTypes

internal class SvelteSyntaxHighlighter(private val jsLanguageLevel: JSLanguageLevel) : HtmlFileHighlighter() {
    override fun getHighlightingLexer(): Lexer {
        return SvelteHighlightingLexer(jsLanguageLevel)
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            SvelteTypes.START_PAREN,
            SvelteTypes.END_PAREN -> PARENS_KEYS

            SvelteTypes.START_MUSTACHE,
            SvelteTypes.END_MUSTACHE,
            SvelteTypes.COMMA,
            SvelteTypes.IF,
            SvelteTypes.ELSE_IF,
            SvelteTypes.END_IF,
            SvelteTypes.AWAIT,
            SvelteTypes.THEN,
            SvelteTypes.AWAIT_THEN,
            SvelteTypes.CATCH,
            SvelteTypes.AWAIT_END,
            SvelteTypes.EACH,
            SvelteTypes.AS,
            SvelteTypes.END_EACH,
            SvelteTypes.ELSE -> KEY_KEYS

            else -> super.getTokenHighlights(tokenType)
        }
    }

    companion object {
        //        private val MUSTACHES = createTextAttributesKey("SVELTE_MUSTACHES", DefaultLanguageHighlighterColors.BRACES)
        private val KEY = createTextAttributesKey("SVELTE_KEY", DefaultLanguageHighlighterColors.KEYWORD)
        private val PARENS = createTextAttributesKey("SVELTE_PARENS", DefaultLanguageHighlighterColors.PARENTHESES)
//        val COMMENT = createTextAttributesKey("SVELTE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)

        //        private val MUSTACHES_KEYS = arrayOf(MUSTACHES)
        private val KEY_KEYS = arrayOf(KEY)
        private val PARENS_KEYS = arrayOf(PARENS)
    }
}