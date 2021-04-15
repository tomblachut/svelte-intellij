package dev.blachut.svelte.lang.parsing.html

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.lexer.HtmlLexer
import com.intellij.lexer.HtmlScriptStyleEmbeddedContentProvider

class SvelteHtmlLexer : HtmlLexer(InnerSvelteHtmlLexer(), false) {

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        // TODO Verify if those masks don't clash with ones used in BaseHtmlLexer.initState
        val baseState = initialState and 0xffff
        val nestingLevel = initialState shr 16
        (delegate as InnerSvelteHtmlLexer).flexLexer.bracesNestingLevel = nestingLevel
        super.start(buffer, startOffset, endOffset, baseState)
    }

    override fun getState(): Int {
        val nestingLevel = (delegate as InnerSvelteHtmlLexer).flexLexer.bracesNestingLevel
        return (nestingLevel shl 16) or (super.getState() and 0xffff)
    }

    override fun acceptEmbeddedContentProvider(provider: HtmlEmbeddedContentProvider): Boolean =
        provider::class != HtmlScriptStyleEmbeddedContentProvider::class

    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }
}
