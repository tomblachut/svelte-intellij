package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementType
import dev.blachut.svelte.lang.isTokenAfterWhiteSpace
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteTagElementTypes
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

object SvelteTagParsing {
    fun parseNotAllowedWhitespace(builder: PsiBuilder, precedingSymbol: String) {
        if (builder.isTokenAfterWhiteSpace()) {
            builder.error("whitespace is not allowed after $precedingSymbol")
        }
    }

    fun parseLazyBlock(builder: PsiBuilder): Pair<IElementType, PsiBuilder.Marker> {
        val marker = builder.mark()
        builder.remapCurrentToken(JSTokenTypes.LBRACE)
        builder.advanceLexer()

        if (builder.tokenType == JSTokenTypes.SHARP) {
            builder.advanceLexer()
            val token = when (builder.tokenType) {
                SvelteTokenTypes.IF_KEYWORD -> SvelteTagElementTypes.IF_START
                SvelteTokenTypes.EACH_KEYWORD -> SvelteTagElementTypes.EACH_START
                SvelteTokenTypes.AWAIT_KEYWORD -> SvelteTagElementTypes.AWAIT_START
                else -> null
            }
            if (token != null) return finishBlock(builder, marker, token)
        } else if (builder.tokenType == JSTokenTypes.COLON) {
            builder.advanceLexer()
            val token = when (builder.tokenType) {
                SvelteTokenTypes.ELSE_KEYWORD -> SvelteTagElementTypes.ELSE_CLAUSE
                SvelteTokenTypes.THEN_KEYWORD -> SvelteTagElementTypes.THEN_CLAUSE
                SvelteTokenTypes.CATCH_KEYWORD -> SvelteTagElementTypes.CATCH_CLAUSE
                else -> null
            }
            if (token != null) return finishBlock(builder, marker, token)
        } else if (builder.tokenType == JSTokenTypes.DIV) {
            builder.advanceLexer()
            parseNotAllowedWhitespace(builder, "/")

            val token = when (builder.tokenType) {
                SvelteTokenTypes.IF_KEYWORD -> SvelteTagElementTypes.IF_END
                SvelteTokenTypes.EACH_KEYWORD -> SvelteTagElementTypes.EACH_END
                SvelteTokenTypes.AWAIT_KEYWORD -> SvelteTagElementTypes.AWAIT_END
                else -> null
            }
            if (token != null) return finishBlock(builder, marker, token)
        }

        return finishBlock(builder, marker, SvelteJSLazyElementTypes.CONTENT_EXPRESSION)
    }

    private fun finishBlock(builder: PsiBuilder, marker: PsiBuilder.Marker, endToken: IElementType): Pair<IElementType, PsiBuilder.Marker> {
        while (!builder.eof() && builder.tokenType !== SvelteTokenTypes.END_MUSTACHE) {
            builder.advanceLexer()
        }
        if (builder.tokenType === SvelteTokenTypes.END_MUSTACHE) {
            builder.remapCurrentToken(JSTokenTypes.RBRACE)
            builder.advanceLexer()
        } else {
            builder.error("missing }")
        }

        if (endToken is ILazyParseableElementType) {
            marker.collapse(endToken)
        } else {
            marker.done(endToken)
        }

        return Pair(endToken, marker)
    }
}
