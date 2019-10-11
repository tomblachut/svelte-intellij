package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.*
import dev.blachut.svelte.lang.psi.SvelteTypes

class SvelteHtmlHighlightingLexer(jsLanguageLevel: JSLanguageLevel) : LayeredLexer(BaseSvelteHtmlHighlightingLexer()) {
    init {
        registerLayer(JavaScriptHighlightingLexer(jsLanguageLevel.dialect.optionHolder), SvelteTypes.CODE_FRAGMENT)
    }
}

private open class BaseSvelteHtmlHighlightingLexer : HtmlHighlightingLexer(BaseSvelteHtmlLexer(), false, null) {
    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }
}
