package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JavaScriptParser
import dev.blachut.svelte.lang.isTokenAfterWhiteSpace

object SvelteJSLazyElementTypes {
    val ATTRIBUTE_PARAMETER = object : SvelteJSLazyElementType("ATTRIBUTE_PARAMETER") {
        override val noTokensErrorMessage = "Parameter expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            parseAtModifiersError(builder)
            parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
        }
    }

    val ATTRIBUTE_EXPRESSION = object : SvelteJSLazyElementType("ATTRIBUTE_EXPRESSION") {
        override val noTokensErrorMessage = "Expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            parseAtModifiersError(builder)
            parser.expressionParser.parseExpression()
        }
    }

    /**
     * Text, html and debug expressions
     */
    val CONTENT_EXPRESSION = object : SvelteJSLazyElementType("CONTENT_EXPRESSION") {
        override val noTokensErrorMessage = "Expression expected"
        override val assumeExternalBraces = false // for now trailing { and } belong to this token

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            parseAtModifiers(builder)
            parser.expressionParser.parseExpression()
        }
    }

    val SPREAD_OR_SHORTHAND = object : SvelteJSLazyElementType("SPREAD_OR_SHORTHAND") {
        override val noTokensErrorMessage = "Shorthand attribute or spread expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            parseAtModifiersError(builder)
            if (builder.tokenType === JSTokenTypes.DOT_DOT_DOT) {
                val marker = builder.mark()
                builder.advanceLexer()
                parseAtModifiersError(builder)
                parser.expressionParser.parseAssignmentExpression(false)
                marker.done(JSElementTypes.SPREAD_EXPRESSION)
            } else {
                parser.expressionParser.parseAssignmentExpression(false)
            }
        }
    }

    private fun parseAtModifiers(builder: PsiBuilder) {
        val unexpectedTokens = setOf(JSTokenTypes.SHARP, JSTokenTypes.COLON, JSTokenTypes.DIV)

        if (builder.tokenType === JSTokenTypes.AT) {
            builder.advanceLexer()

            if (builder.isTokenAfterWhiteSpace()) {
                builder.error("Whitespace is not allowed after @")
            }

            if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "dev/blachut/svelte/lang/parsing/html") {
                builder.remapCurrentToken(SvelteTokenTypes.HTML_KEYWORD)
                builder.advanceLexer()
            } else if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "debug") {
                builder.remapCurrentToken(SvelteTokenTypes.DEBUG_KEYWORD)
                builder.advanceLexer()
            } else {
                val errorMarker = builder.mark()
                builder.advanceLexer()
                errorMarker.error("Expected html or debug")
            }
        } else if (unexpectedTokens.contains(builder.tokenType)) {
            builder.advanceLexer()

            if (builder.isTokenAfterWhiteSpace()) {
                builder.error("Whitespace is not allowed here")
            }
            val errorMarker = builder.mark()
            builder.advanceLexer()
            errorMarker.error("Invalid block name")
        }
    }

    private fun parseAtModifiersError(builder: PsiBuilder) {
        if (builder.tokenType === JSTokenTypes.AT) {
            val errorMarker = builder.mark()
            builder.advanceLexer()

            // copied from parseAtModifiers above
            if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "dev/blachut/svelte/lang/parsing/html") {
                builder.remapCurrentToken(SvelteTokenTypes.HTML_KEYWORD)
                builder.advanceLexer()
            } else if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "debug") {
                builder.remapCurrentToken(SvelteTokenTypes.DEBUG_KEYWORD)
                builder.advanceLexer()
            }

            errorMarker.error("@-modifiers are not allowed here")
        }
    }
}
