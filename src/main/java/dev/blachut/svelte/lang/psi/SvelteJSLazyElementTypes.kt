package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JavaScriptParser
import dev.blachut.svelte.lang.isTokenAfterWhiteSpace

object SvelteJSLazyElementTypes {
    val ATTRIBUTE_PARAMETER = object : SvelteJSLazyElementType("PARAMETER") {
        override val noTokensErrorMessage = "parameter expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            parseAtModifiersError(builder)
            parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
        }
    }

    val ATTRIBUTE_EXPRESSION = object : SvelteJSLazyElementType("EXPRESSION") {
        override val noTokensErrorMessage = "expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            parseAtModifiersError(builder)
            parser.expressionParser.parseExpression()
        }
    }

    val CONTENT_EXPRESSION = object : SvelteJSLazyElementType("EXPRESSION") {
        override val noTokensErrorMessage = "expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            // for now trailing { and } belong to this token
            builder.advanceLexer() // {
            parseAtModifiers(builder)
            parser.expressionParser.parseExpression()
            builder.advanceLexer() // }
        }
    }

    val SPREAD_OR_SHORTHAND = object : SvelteJSLazyElementType("SPREAD_OR_SHORTHAND") {
        override val noTokensErrorMessage = "shorthand attribute or spread expression expected"

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

    private val allowedAtModifiers = setOf("html", "debug")

    private fun parseAtModifiers(builder: PsiBuilder) {
        if (builder.tokenType === JSTokenTypes.AT) {
            builder.advanceLexer()

            if (builder.isTokenAfterWhiteSpace()) {
                builder.error("whitespace is not allowed after @")
            }

            if (builder.tokenType === JSTokenTypes.IDENTIFIER && allowedAtModifiers.contains(builder.tokenText)) {
                builder.advanceLexer()
            } else {
                val errorMarker = builder.mark()
                builder.advanceLexer()
                errorMarker.error("expected html or debug")
            }
        }
    }

    private fun parseAtModifiersError(builder: PsiBuilder) {
        if (builder.tokenType === JSTokenTypes.AT) {
            val errorMarker = builder.mark()
            builder.advanceLexer()

            if (builder.tokenType === JSTokenTypes.IDENTIFIER && allowedAtModifiers.contains(builder.tokenText)) {
                builder.advanceLexer()
            }

            errorMarker.error("@-modifiers are not allowed here")
        }
    }
}
