package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.Language
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.HtmlLexer
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.parsing.js.SvelteJSScriptContentProvider

class SvelteHtmlLexer : HtmlLexer(InnerSvelteHtmlLexer(), false) {

    private val helper = SvelteHtmlLexerHelper(object : SvelteHtmlLexerHandle {

        override var seenTag: Boolean
            get() = this@SvelteHtmlLexer.seenTag
            set(value) {
                this@SvelteHtmlLexer.seenTag = value
            }

        override var seenStyleType: Boolean
            get() = this@SvelteHtmlLexer.seenStylesheetType
            set(value) {
                this@SvelteHtmlLexer.seenStylesheetType = value
            }

        override val seenStyle: Boolean = this@SvelteHtmlLexer.seenStyle
        override val styleType: String? = this@SvelteHtmlLexer.styleType
        override val inTagState: Boolean =
            (state and HtmlHighlightingLexer.BASE_STATE_MASK) == _SvelteHtmlLexer.START_TAG_NAME

        override fun registerHandler(elementType: IElementType, value: TokenHandler) {
            this@SvelteHtmlLexer.registerHandler(elementType, value)
        }
    })

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

    override fun findScriptContentProvider(mimeType: String?) = SvelteJSScriptContentProvider

    override fun getStyleLanguage(): Language? =
        helper.styleViaLang(ourDefaultStyleLanguage) ?: super.getStyleLanguage()

    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }
}
