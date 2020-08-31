package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.HtmlLexer
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteJSLanguage

class SvelteHtmlLexer : HtmlLexer(InnerSvelteHtmlLexer(), false) {
    private val helper = SvelteHtmlLexerHelper(object : SvelteHtmlLexerHandle {

        override var seenTag: Boolean
            get() = this@SvelteHtmlLexer.seenTag
            set(value) {
                this@SvelteHtmlLexer.seenTag = value
            }

        override var seenContentType: Boolean
            get() = this@SvelteHtmlLexer.seenContentType
            set(value) {
                this@SvelteHtmlLexer.seenContentType = value
            }

        override var seenStyleType: Boolean
            get() = this@SvelteHtmlLexer.seenStylesheetType
            set(value) {
                this@SvelteHtmlLexer.seenStylesheetType = value
            }

        override val seenStyle: Boolean get() = this@SvelteHtmlLexer.seenStyle
        override val seenScript: Boolean get() = this@SvelteHtmlLexer.seenScript
        override val styleType: String? get() = this@SvelteHtmlLexer.styleType
        override val inTagState: Boolean get() = (state and HtmlHighlightingLexer.BASE_STATE_MASK) == _SvelteHtmlLexer.START_TAG_NAME

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

    override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? {
        return getSvelteScriptContentProvider(mimeType)
    }

    override fun getStyleLanguage(): Language? =
        helper.styleViaLang(CSSLanguage.INSTANCE) ?: super.getStyleLanguage()

    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }

    companion object {
        fun getSvelteScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? {
            if (mimeType == "ts" || mimeType == "typescript") {
                // todo here we should use language extension similar to SvelteJSLanguage, instead of the original lang
                return LanguageHtmlScriptContentProvider.getScriptContentProvider(JavaScriptSupportLoader.TYPESCRIPT)
            }

            return LanguageHtmlScriptContentProvider.getScriptContentProvider(SvelteJSLanguage.INSTANCE)
        }
    }
}
