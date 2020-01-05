package dev.blachut.svelte.lang.parsing.top

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.LayeredLexer
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlHighlightingLexer
import dev.blachut.svelte.lang.psi.SvelteTypes

class SvelteHighlightingLexer(jsLanguageLevel: JSLanguageLevel) : LayeredLexer(SvelteLexer()) {
    init {
        registerLayer(SvelteHtmlHighlightingLexer(jsLanguageLevel), SvelteTypes.HTML_FRAGMENT)
    }
}
