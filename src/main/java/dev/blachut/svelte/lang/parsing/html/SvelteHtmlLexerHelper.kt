package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.Language
import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer
import com.intellij.psi.xml.XmlTokenType

// Adapted from org.jetbrains.vuejs.lang.html.lexer.VueLexerHelper
class SvelteHtmlLexerHelper(private val handle: SvelteHtmlLexerHandle) {

    init {
        handle.registerHandler(XmlTokenType.XML_NAME, SvelteLangAttributeHandler())
    }

    fun styleViaLang(default: Language?): Language? = styleViaLang(default, handle.styleType)

    /*
     * Somehow forces IDEA to set lang attribute value into styleType property
     * of SvelteHtmlLexer (property is used here through handle, cause it's protected in Lexer),
     * though directly don't set anything to it. Must be done somewhere in IDEA intestines
     */
    inner class SvelteLangAttributeHandler : BaseHtmlLexer.TokenHandler {
        override fun handleElement(lexer: Lexer) {
            if (!handle.seenTag && !handle.inTagState
                && handle.seenStyle
                && "lang" == lexer.tokenText
            ) {
                handle.seenStyleType = true
            }
        }
    }

    companion object {
        fun styleViaLang(default: Language?, style: String?): Language? = when {
            // Don't know why this strange PostCSS hack is needed
            style == null -> Language.findLanguageByID("PostCSS")
            default != null -> default.dialects
                .firstOrNull { style.equals(it.id, ignoreCase = true) }
            else -> null
        }
    }
}
