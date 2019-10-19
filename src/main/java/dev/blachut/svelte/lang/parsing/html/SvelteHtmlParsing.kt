package dev.blachut.svelte.lang.parsing.html

import com.intellij.codeInsight.daemon.XmlErrorMessages
import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteTypes

/**
 * Due to the design of HtmlParsing, SvelteHtmlParsing remaps SvelteTypes.START_MUSTACHE to XmlTokenType.XML_NAME
 * so that code enters overridable parseAttribute() path.
 * This is a lossy process, but required to make {shorthand} attributes work without copying whole HtmlParsing file.
 * Remapping also ensures that lexer and other token-based components do not care about HtmlParsing limitation.
 *
 * After checking if XmlTokenType.XML_NAME is in fact '{' token is remapped back to SvelteTypes.START_MUSTACHE.
 */
class SvelteHtmlParsing(builder: PsiBuilder) : HtmlParsing(builder) {
    init {
        builder.setTokenTypeRemapper { source, _, _, _ ->
            return@setTokenTypeRemapper if (source === SvelteTypes.START_MUSTACHE) XmlTokenType.XML_NAME else source
        }
    }

    override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
        // Inspired by Vue plugin. Svelte tags must be closed explicitly
        if (isSvelteComponentTag(originalTagName)) {
            return false
        }
        return super.isSingleTag(tagName, originalTagName)
    }

    override fun parseAttribute() {
        assert(token() === XmlTokenType.XML_NAME)
        val att = mark()

        if (isRemappedStartMustache()) {
            parseAttributeExpression()
        } else {
            advance()
            if (token() === XmlTokenType.XML_EQ) {
                advance()
                parseAttributeValue()
            }
        }

        att.done(XmlElementType.XML_ATTRIBUTE)
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
                } else if (isRemappedStartMustache()) {
                    parseAttributeExpression()
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
            while (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN || isRemappedStartMustache()) {
                if (isRemappedStartMustache()) {
                    parseAttributeExpression()
                } else {
                    advance()
                }
            }
        }

        attValue.done(XmlElementType.XML_ATTRIBUTE_VALUE)
    }

    private fun isRemappedStartMustache(): Boolean {
        return token() === XmlTokenType.XML_NAME && builder.originalText[builder.currentOffset] == '{'
    }

    private fun parseAttributeExpression() {
        val expressionMarker = mark()
        // Remap must happen AFTER placing marker
        builder.remapCurrentToken(SvelteTypes.START_MUSTACHE)
        advance() // {
        advanceCode()
        advance() // }
        expressionMarker.done(SvelteJSElementTypes.ATTRIBUTE_EXPRESSION)
    }

    private fun advanceCode() {
        val marker = builder.mark()
        // Guard against empty expressions
        if (token() === SvelteTypes.CODE_FRAGMENT) advance()
        marker.collapse(SvelteJSLazyElementTypes.EXPRESSION)
    }
}
