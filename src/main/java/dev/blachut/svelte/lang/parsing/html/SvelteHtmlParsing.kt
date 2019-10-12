package dev.blachut.svelte.lang.parsing.html

import com.intellij.codeInsight.daemon.XmlErrorMessages
import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteTypes

class SvelteHtmlParsing(builder: PsiBuilder) : HtmlParsing(builder) {
    override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
        // Inspired by Vue plugin. Svelte tags must be closed explicitly
        if (isSvelteComponentTag(originalTagName)) {
            return false
        }
        return super.isSingleTag(tagName, originalTagName)
    }

    override fun parseAttributeValue() {
        val attValue = mark()
        if (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER) {
            while (true) {
                val tt = token()
                if (tt == null
                    || tt === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER
                    || tt === XmlTokenType.XML_END_TAG_START
                    || tt === XmlTokenType.XML_EMPTY_ELEMENT_END
                    || tt === XmlTokenType.XML_START_TAG_START) {
                    break
                }

                if (tt === XmlTokenType.XML_BAD_CHARACTER) {
                    val error = mark()
                    advance()
                    error.error(XmlErrorMessages.message("unescaped.ampersand.or.nonterminated.character.entity.reference"))
                } else if (tt === XmlTokenType.XML_ENTITY_REF_TOKEN) {
                    parseReference()
                } else if (tt === SvelteTypes.CODE_FRAGMENT) {
                    markCode()
                } else {
                    advance()
                }
            }

            if (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
                advance()
            } else {
                error(XmlErrorMessages.message("xml.parsing.unclosed.attribute.value"))
            }
        } else {
            // Unquoted attr value. Unlike unmodified IntelliJ HTML this isn't necessary single token
            while (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
                || token() === SvelteTypes.CODE_FRAGMENT
                || token() === SvelteTypes.START_MUSTACHE
                || token() === SvelteTypes.END_MUSTACHE) {
                if (token() === SvelteTypes.CODE_FRAGMENT) {
                    markCode()
                } else {
                    advance()
                }
            }
        }

        attValue.done(XmlElementType.XML_ATTRIBUTE_VALUE)
    }

    private fun markCode() {
        val marker = builder.mark()
        advance()
        marker.collapse(SvelteJSLazyElementTypes.EXPRESSION)
    }
}
