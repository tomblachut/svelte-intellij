package dev.blachut.svelte.lang.parsing.html

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.HtmlScriptStyleEmbeddedContentProvider

class SvelteHtmlHighlightingLexer : HtmlHighlightingLexer(SvelteHtmlBaseLexer(), false, null) {

    override fun acceptEmbeddedContentProvider(provider: HtmlEmbeddedContentProvider): Boolean =
        provider::class != HtmlScriptStyleEmbeddedContentProvider::class

    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }

}
