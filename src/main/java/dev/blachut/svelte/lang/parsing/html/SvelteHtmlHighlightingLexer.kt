package dev.blachut.svelte.lang.parsing.html

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.HtmlScriptStyleEmbeddedContentProvider
import com.intellij.lexer.LayeredLexer
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteJSLanguage
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteHtmlHighlightingLexer : HtmlHighlightingLexer(InnerSvelteHtmlLexer(), false, null) {

    override fun acceptEmbeddedContentProvider(provider: HtmlEmbeddedContentProvider): Boolean =
        provider::class != HtmlScriptStyleEmbeddedContentProvider::class

    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }

}
