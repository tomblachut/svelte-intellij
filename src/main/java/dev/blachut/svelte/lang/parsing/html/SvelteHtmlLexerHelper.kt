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

    inner class SvelteLangAttributeHandler : BaseHtmlLexer.TokenHandler {
        override fun handleElement(lexer: Lexer) {

            if (handle.seenScript && !handle.seenTag) {
                handle.seenContentType = false
                if ("lang" == lexer.tokenText) {
                    handle.seenContentType = true
                    return
                }
            }

            // Shoehorn lang attribute to behave the same as type attribute for lexing purposes
            if (!handle.seenTag && !handle.inTagState && handle.seenStyle && "lang" == lexer.tokenText) {
                handle.seenStyleType = true
            }
        }
    }

    companion object {
        fun styleViaLang(default: Language?, langId: String?): Language? = when {
            // Don't know why this strange PostCSS hack is needed
            langId == null -> Language.findLanguageByID("PostCSS")
            default != null -> default.dialects.firstOrNull { langId.equals(it.id, ignoreCase = true) }
            else -> null
        }
    }
}
