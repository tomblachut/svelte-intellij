package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.ILazyParseableElementType

@Suppress("UNUSED_PARAMETER")
object SvelteManualParsing {
    @JvmStatic fun parseExpression(builder: PsiBuilder, level: Int) = collapseCode(builder, SvelteJSLazyElementTypes.EXPRESSION)

    @JvmStatic fun parseParameter(builder: PsiBuilder, level: Int) = collapseCode(builder, SvelteJSLazyElementTypes.PARAMETER)

    private fun collapseCode(builder: PsiBuilder, lazyElementType: ILazyParseableElementType): Boolean {
        val marker = builder.mark()
        if (builder.tokenType == SvelteTypes.CODE_FRAGMENT) {
            builder.advanceLexer()
        }
        marker.collapse(lazyElementType)
        return true
    }
}
