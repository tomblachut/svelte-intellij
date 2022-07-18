package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lexer.LayeredLexer
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

/**
 * Used to differentiate #if as Svelte tag prefix, and ES private identifier.
 * Too powerful, relies on input starting with `{`, could be refactored and inlined.
 */
class SvelteJSExpressionLexer(assumeExternalBraces: Boolean) : LayeredLexer(SvelteHtmlBaseLexer(assumeExternalBraces)) {
    init {
        registerLayer(JSFlexAdapter(DialectOptionHolder.JS_WITH_JSX), SvelteTokenTypes.CODE_FRAGMENT)
    }
}
