package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementType
import dev.blachut.svelte.lang.psi.SvelteBlockLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

object SvelteManualParsing {
    fun parseLazyBlock(builder: PsiBuilder): Pair<IElementType, PsiBuilder.Marker> {
        val marker = builder.mark()
        builder.remapCurrentToken(JSTokenTypes.LBRACE)
        builder.advanceLexer()

        if (builder.tokenType == SvelteTokenTypes.TEMP_PREFIX) {
            builder.remapCurrentToken(TokenType.WHITE_SPACE)
            builder.advanceLexer()
        }

        if (builder.tokenType == SvelteTokenTypes.HASH) {
            builder.advanceLexer()
            val token = when (builder.tokenType) {
                SvelteTokenTypes.LAZY_IF -> SvelteBlockLazyElementTypes.IF_START
                SvelteTokenTypes.LAZY_EACH -> SvelteBlockLazyElementTypes.EACH_START
                SvelteTokenTypes.LAZY_AWAIT -> SvelteBlockLazyElementTypes.AWAIT_START
                else -> null
            }
            if (token != null) return finishBlock(builder, marker, token)
        } else if (builder.tokenType == SvelteTokenTypes.COLON) {
            builder.advanceLexer()
            val token = when (builder.tokenType) {
                SvelteTokenTypes.LAZY_ELSE -> SvelteBlockLazyElementTypes.ELSE_CLAUSE
                SvelteTokenTypes.LAZY_THEN -> SvelteBlockLazyElementTypes.THEN_CLAUSE
                SvelteTokenTypes.LAZY_CATCH -> SvelteBlockLazyElementTypes.CATCH_CLAUSE
                else -> null
            }
            if (token != null) return finishBlock(builder, marker, token)
        } else if (builder.tokenType == SvelteTokenTypes.SLASH) {
            builder.remapCurrentToken(JSTokenTypes.DIV)
            builder.advanceLexer()
            val token = when (builder.tokenType) {
                SvelteTokenTypes.LAZY_IF -> {
                    builder.remapCurrentToken(JSTokenTypes.IF_KEYWORD)
                    SvelteBlockLazyElementTypes.IF_END
                }
                SvelteTokenTypes.LAZY_EACH -> {
                    builder.remapCurrentToken(JSTokenTypes.IDENTIFIER)
                    SvelteBlockLazyElementTypes.EACH_END
                }
                SvelteTokenTypes.LAZY_AWAIT -> {
                    builder.remapCurrentToken(JSTokenTypes.AWAIT_KEYWORD)
                    SvelteBlockLazyElementTypes.AWAIT_END
                }
                else -> null
            }
            if (token != null) return finishBlock(builder, marker, token)
        }

        return finishBlock(builder, marker, SvelteJSLazyElementTypes.EXPRESSION)
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
