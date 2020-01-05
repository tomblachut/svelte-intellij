package dev.blachut.svelte.lang.parsing.top

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.LayeredLexer
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlHighlightingLexer
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteHighlightingLexer(jsLanguageLevel: JSLanguageLevel) : LayeredLexer(SvelteLexer()) {
    init {
        registerLayer(SvelteHtmlHighlightingLexer(jsLanguageLevel), SvelteTokenTypes.HTML_FRAGMENT)
    }
}
