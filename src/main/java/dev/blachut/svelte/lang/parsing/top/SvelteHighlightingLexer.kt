package dev.blachut.svelte.lang.parsing.top

import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.LayeredLexer
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlHighlightingLexer
import dev.blachut.svelte.lang.psi.SvelteTypes.*

class SvelteHighlightingLexer(jsLanguageLevel: JSLanguageLevel) : LayeredLexer(SvelteLexer()) {
    init {
        registerLayer(SvelteHtmlHighlightingLexer(jsLanguageLevel), HTML_FRAGMENT)
        //        registerSelfStoppingLayer(JavaScriptHighlightingLexer(jsLanguageLevel.dialect.optionHolder), arrayOf(START_MUSTACHE), arrayOf(END_MUSTACHE))
        registerSelfStoppingLayer(JavaScriptHighlightingLexer(jsLanguageLevel.dialect.optionHolder), arrayOf(HASH, COLON, SLASH, AT, CODE_FRAGMENT), arrayOf(END_MUSTACHE))
    }
}
