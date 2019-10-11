package dev.blachut.svelte.lang.parsing.top

import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.LayeredLexer
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlHighlightingLexer
import dev.blachut.svelte.lang.psi.SvelteTypes.CODE_FRAGMENT
import dev.blachut.svelte.lang.psi.SvelteTypes.HTML_FRAGMENT

class SvelteHighlightingLexer(jsLanguageLevel: JSLanguageLevel) : LayeredLexer(SvelteLexer()) {
    init {
        registerLayer(SvelteHtmlHighlightingLexer(), HTML_FRAGMENT)
        registerLayer(JavaScriptHighlightingLexer(jsLanguageLevel.dialect.optionHolder), CODE_FRAGMENT)
    }
}
