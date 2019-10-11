package dev.blachut.svelte.lang.parsing.html

import com.intellij.lexer.*

class SvelteHtmlHighlightingLexer : HtmlHighlightingLexer(BaseSvelteHtmlLexer(), false, null) {
    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }
}
