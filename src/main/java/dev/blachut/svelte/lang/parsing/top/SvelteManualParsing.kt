package dev.blachut.svelte.lang.parsing.top

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementType
import dev.blachut.svelte.lang.psi.SvelteBlockLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteTypes

@Suppress("UNUSED_PARAMETER")
object SvelteManualParsing {
    @JvmStatic
    fun parseExpression(builder: PsiBuilder, level: Int) = collapseCode(builder, SvelteJSLazyElementTypes.EXPRESSION)

    @JvmStatic
    fun parseParameter(builder: PsiBuilder, level: Int) = collapseCode(builder, SvelteJSLazyElementTypes.PARAMETER)

    @JvmStatic
    fun parseLazy(builder: PsiBuilder, level: Int): Boolean {
        return parseLazyBlock(builder, level)
//        if (builder.tokenType != SvelteTypes.START_MUSTACHE) return false
//        return finishLazyBlock(builder, builder.mark(), SvelteTypes.CODE_FRAGMENT)
    }


    @JvmStatic
    fun parseLazyBlock(builder: PsiBuilder, level: Int): Boolean {
        if (builder.tokenType != SvelteTypes.START_MUSTACHE) return false

        val marker = builder.mark()
        builder.advanceLexer()

        if (builder.tokenType == SvelteTypes.TEMP_PREFIX) {
            builder.advanceLexer()
        }

        if (builder.tokenType == SvelteTypes.HASH) {
            builder.advanceLexer()
            val token = when (builder.tokenType) {
                SvelteTypes.LAZY_IF -> SvelteBlockLazyElementTypes.IF_START
                SvelteTypes.LAZY_EACH -> SvelteBlockLazyElementTypes.EACH_START
                SvelteTypes.LAZY_AWAIT -> SvelteBlockLazyElementTypes.AWAIT_START
                else -> null
            }
            if (token != null) return finishLazyBlock(builder, marker, token)
        } else if (builder.tokenType == SvelteTypes.COLON) {
            builder.advanceLexer()
            val token = when (builder.tokenType) {
                SvelteTypes.LAZY_ELSE -> SvelteBlockLazyElementTypes.ELSE_CLAUSE
                SvelteTypes.LAZY_THEN -> SvelteBlockLazyElementTypes.THEN_CLAUSE
                SvelteTypes.LAZY_CATCH -> SvelteBlockLazyElementTypes.CATCH_CLAUSE
                else -> null
            }
            if (token != null) return finishLazyBlock(builder, marker, token)

        } else if (builder.tokenType == SvelteTypes.SLASH) {
            builder.advanceLexer()
            val token = when (builder.tokenType) {
                SvelteTypes.LAZY_IF -> SvelteBlockLazyElementTypes.IF_END
                SvelteTypes.LAZY_EACH -> SvelteBlockLazyElementTypes.EACH_END
                SvelteTypes.LAZY_AWAIT -> SvelteBlockLazyElementTypes.AWAIT_END
                else -> null
            }
            if (token != null) return finishBlock(builder, marker, token)
        }

        return finishLazyBlock(builder, marker, SvelteBlockLazyElementTypes.EXPR)
    }

    private fun finishBlock(builder: PsiBuilder, marker: PsiBuilder.Marker, endToken: IElementType): Boolean {
        while (builder.tokenType != SvelteTypes.END_MUSTACHE) {
            builder.advanceLexer()
        }
        builder.advanceLexer()

        marker.done(endToken)
        return true
    }

    private fun finishLazyBlock(builder: PsiBuilder, marker: PsiBuilder.Marker, endToken: IElementType): Boolean {
        while (builder.tokenType != SvelteTypes.END_MUSTACHE) {
            builder.advanceLexer()
        }
        builder.advanceLexer()

        marker.collapse(endToken)
        return true
    }

    private fun collapseCode(builder: PsiBuilder, lazyElementType: ILazyParseableElementType): Boolean {
        val marker = builder.mark()
        if (builder.tokenType == SvelteTypes.CODE_FRAGMENT) {
            builder.advanceLexer()
        }
        marker.collapse(lazyElementType)
        return true
    }
}
