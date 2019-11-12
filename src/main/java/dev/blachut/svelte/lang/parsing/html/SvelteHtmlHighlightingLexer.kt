package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.Language
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.LayeredLexer
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.parsing.js.SvelteJSScriptContentProvider
import dev.blachut.svelte.lang.psi.SvelteTypes

class SvelteHtmlHighlightingLexer(jsLanguageLevel: JSLanguageLevel) : LayeredLexer(BaseSvelteHtmlHighlightingLexer()) {
    init {
        registerLayer(JavaScriptHighlightingLexer(jsLanguageLevel.dialect.optionHolder), SvelteTypes.CODE_FRAGMENT)
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

        override val seenStyle: Boolean get() = this@BaseSvelteHtmlHighlightingLexer.seenStyle
        override val styleType: String? get() = this@BaseSvelteHtmlHighlightingLexer.styleType
        override val inTagState: Boolean get() = (state and BASE_STATE_MASK) == _SvelteHtmlLexer.START_TAG_NAME

        override fun registerHandler(elementType: IElementType, value: TokenHandler) {
            this@BaseSvelteHtmlHighlightingLexer.registerHandler(elementType, value)
        }
    })

    override fun findScriptContentProvider(mimeType: String?) = SvelteJSScriptContentProvider

    override fun isHtmlTagState(state: Int): Boolean {
        return state == _SvelteHtmlLexer.START_TAG_NAME || state == _SvelteHtmlLexer.END_TAG_NAME
    }

    override fun getStyleLanguage(): Language? =
        helper.styleViaLang(ourDefaultStyleLanguage) ?: super.getStyleLanguage()
}
