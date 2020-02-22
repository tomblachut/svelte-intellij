package dev.blachut.svelte.lang.parsing.html

import com.intellij.codeInsight.daemon.XmlErrorMessages
import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.isTokenAfterWhiteSpace
import dev.blachut.svelte.lang.psi.SVELTE_HTML_TAG
import dev.blachut.svelte.lang.psi.SvelteElementTypes
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

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
            return@setTokenTypeRemapper if (source === SvelteTokenTypes.START_MUSTACHE) XmlTokenType.XML_NAME else source
        }
    }

    private val svelteParsing = SvelteParsing(builder)

    override fun getHtmlTagElementType(): IElementType {
        return SVELTE_HTML_TAG
    }

    override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
        // Inspired by Vue plugin. Svelte tags must be closed explicitly
        if (isSvelteComponentTag(originalTagName)) {
            return false
        }
        return super.isSingleTag(tagName, originalTagName)
    }

    override fun parseTag() {
        super.parseTag()
        svelteParsing.reportMissingEndSvelteTags()
    }

    override fun hasCustomTagContent(): Boolean {
        return svelteParsing.isSvelteTagStart(token())
    }

    override fun parseCustomTagContent(xmlText: PsiBuilder.Marker?): PsiBuilder.Marker? {
        terminateText(xmlText)
        svelteParsing.parseSvelteTag()
        return null
    }

    override fun hasCustomTopLevelContent(): Boolean {
        return hasCustomTagContent()
    }

    override fun parseCustomTopLevelContent(error: PsiBuilder.Marker?): PsiBuilder.Marker? {
        flushError(error)
        svelteParsing.parseSvelteTag()

        if (builder.tokenType == null || builder.tokenType === XmlTokenType.XML_REAL_WHITE_SPACE && builder.lookAhead(1) == null) {
            // noop when at eof, ensures error is placed at the last character
            builder.advanceLexer()
            svelteParsing.reportMissingEndSvelteTags()
        }

        return null
    }

    override fun parseAttribute() {
        assert(token() === XmlTokenType.XML_NAME)
        val att = mark()

        if (isRemappedStartMustache()) {
            parseAttributeExpression(SvelteJSLazyElementTypes.SPREAD_OR_SHORTHAND)
        } else {
            val elementType = when (builder.tokenText!!.startsWith("let:", true)) {
                true -> SvelteJSLazyElementTypes.ATTRIBUTE_PARAMETER
                false -> SvelteJSLazyElementTypes.ATTRIBUTE_EXPRESSION
            }
            advance()
            if (token() === XmlTokenType.XML_EQ) {
                advance()
                parseAttributeValue(elementType)
            }
        }

        att.done(XmlElementType.XML_ATTRIBUTE)
    }

    private fun parseAttributeValue(elementType: IElementType) {
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
                    parseAttributeExpression(elementType)
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
                    parseAttributeExpression(elementType)
                } else {
                    advance()
                }

                // Guard against adjacent shorthand or spread attributes ambiguity
                if (builder.isTokenAfterWhiteSpace()) {
                    break
                }
            }
        }

        attValue.done(XmlElementType.XML_ATTRIBUTE_VALUE)
    }

    private fun isRemappedStartMustache(): Boolean {
        return token() === XmlTokenType.XML_NAME && builder.originalText[builder.currentOffset] == '{'
    }

    private fun parseAttributeExpression(elementType: IElementType) {
        val expressionMarker = mark()
        // Remap must happen AFTER placing marker
        builder.remapCurrentToken(SvelteTokenTypes.START_MUSTACHE)
        advance() // {
        advanceCode(elementType)
        advance() // }
        expressionMarker.done(SvelteElementTypes.ATTRIBUTE_EXPRESSION)
    }

    private fun advanceCode(elementType: IElementType) {
        val marker = builder.mark()
        // Guard against empty expressions
        if (token() === SvelteTokenTypes.CODE_FRAGMENT) advance()
        marker.collapse(elementType)
    }
}
