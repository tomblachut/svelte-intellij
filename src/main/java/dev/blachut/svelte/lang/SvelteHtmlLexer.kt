package dev.blachut.svelte.lang

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.HtmlLexer
import com.intellij.lexer._HtmlLexer
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.html.SvelteHandledLexer
import dev.blachut.svelte.lang.html.SvelteLangAttributeHandler
import dev.blachut.svelte.lang.html.SvelteTagClosedHandler

class SvelteHtmlLexer(private val languageLevel: JSLanguageLevel) : HtmlLexer(), SvelteHandledLexer {
    init {
        registerHandler(XmlTokenType.XML_NAME, SvelteLangAttributeHandler())
        registerHandler(XmlTokenType.XML_TAG_END, SvelteTagClosedHandler())
    }

    override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? =
        findScriptContentProviderSvelte(mimeType, { super.findScriptContentProvider(mimeType) }, languageLevel)

    override fun getStyleLanguage(): Language? = styleViaLang(ourDefaultStyleLanguage) ?: super.getStyleLanguage()

    override fun seenScript(): Boolean = seenScript
    override fun seenStyle(): Boolean = seenStyle
    override fun seenTag(): Boolean = seenTag
    override fun seenAttribute(): Boolean = seenAttribute
    override fun getScriptType(): String? = scriptType
    override fun getStyleType(): String? = styleType
    override fun inTagState(): Boolean = (state and HtmlHighlightingLexer.BASE_STATE_MASK) == _HtmlLexer.START_TAG_NAME

    override fun setSeenScriptType() {
        seenContentType = true
    }

    override fun setSeenScript() {
        seenScript = true
    }

    override fun setSeenStyleType() {
        seenStylesheetType = true
    }

    override fun setSeenTag(tag: Boolean) {
        seenTag = tag
    }

    override fun setSeenAttribute(attribute: Boolean) {
        seenAttribute = attribute
    }
}
