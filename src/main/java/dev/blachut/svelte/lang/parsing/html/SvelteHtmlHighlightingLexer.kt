package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.LayeredLexer
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteHtmlHighlightingLexer : LayeredLexer(BaseSvelteHtmlHighlightingLexer()) {
    init {
        registerLayer(JavaScriptHighlightingLexer(JSLanguageLevel.ES6.dialect.optionHolder), SvelteTokenTypes.CODE_FRAGMENT)
    }
}

// TODO Merge with SvelteHtmlHighlightingLexer by handling code fragments internally
private open class BaseSvelteHtmlHighlightingLexer : HtmlHighlightingLexer(InnerSvelteHtmlLexer(), false, null) {
    private val helper = SvelteHtmlLexerHelper(object : SvelteHtmlLexerHandle {
        override var seenTag: Boolean
            get() = this@BaseSvelteHtmlHighlightingLexer.seenTag
            set(value) {
                this@BaseSvelteHtmlHighlightingLexer.seenTag = value
            }

        override var seenStyleType: Boolean
            get() = this@BaseSvelteHtmlHighlightingLexer.seenStylesheetType
            set(value) {
                this@BaseSvelteHtmlHighlightingLexer.seenStylesheetType = value
            }

        override var seenContentType: Boolean
            get() = this@BaseSvelteHtmlHighlightingLexer.seenContentType
            set(value) {
                this@BaseSvelteHtmlHighlightingLexer.seenContentType = value
            }
        override val seenScript: Boolean get() = this@BaseSvelteHtmlHighlightingLexer.seenScript
        override val seenStyle: Boolean get() = this@BaseSvelteHtmlHighlightingLexer.seenStyle
        override val styleType: String? get() = this@BaseSvelteHtmlHighlightingLexer.styleType
        override val inTagState: Boolean get() = (state and BASE_STATE_MASK) == _SvelteHtmlLexer.START_TAG_NAME

        override fun registerHandler(elementType: IElementType, value: TokenHandler) {
            this@BaseSvelteHtmlHighlightingLexer.registerHandler(elementType, value)
        }
    })

    override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? {
        return SvelteHtmlLexer.getSvelteScriptContentProvider(mimeType)
    }

    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }

    override fun getStyleLanguage(): Language? =
        helper.styleViaLang(CSSLanguage.INSTANCE) ?: super.getStyleLanguage()
}
