package dev.blachut.svelte.lang

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer._HtmlLexer
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.html.SvelteHandledLexer
import dev.blachut.svelte.lang.html.SvelteLangAttributeHandler
import dev.blachut.svelte.lang.html.SvelteTagClosedHandler

class SvelteHtmlHighlightingLexer(private val languageLevel: JSLanguageLevel) : HtmlHighlightingLexer(), SvelteHandledLexer {

    init {
        registerHandler(XmlTokenType.XML_NAME, SvelteLangAttributeHandler())
        registerHandler(XmlTokenType.XML_NAME, SvelteTemplateTagHandler())
        registerHandler(XmlTokenType.XML_TAG_END, SvelteTagClosedHandler())
    }

    override fun getTokenType(): IElementType? {
        val type = super.getTokenType()
        if (type == XmlTokenType.TAG_WHITE_SPACE && baseState() == 0) return XmlTokenType.XML_REAL_WHITE_SPACE
        return type
    }

    override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? =
        findScriptContentProviderVue(mimeType, { super.findScriptContentProvider(mimeType) }, languageLevel)

    override fun getStyleLanguage(): Language? {
        return styleViaLang(ourDefaultStyleLanguage) ?: super.getStyleLanguage()
    }

    override fun seenScript(): Boolean = seenScript
    override fun seenStyle(): Boolean = seenStyle
    override fun seenTag(): Boolean = seenTag
    override fun seenAttribute(): Boolean = seenAttribute
    override fun getScriptType(): String? = scriptType
    override fun getStyleType(): String? = styleType
    override fun inTagState(): Boolean = baseState() == _HtmlLexer.START_TAG_NAME

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

    private fun baseState() = state and BASE_STATE_MASK
}
