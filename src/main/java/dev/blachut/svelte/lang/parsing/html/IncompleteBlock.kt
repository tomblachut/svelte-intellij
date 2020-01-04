package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.psi.SvelteBlockLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

sealed class IncompleteBlock {
    abstract val startMarker: PsiBuilder.Marker

    abstract fun isMatchingEndTag(token: IElementType): Boolean

    fun handleEndTag() {
        startMarker.done(SvelteJSElementTypes.ATTRIBUTE_EXPRESSION)
    }

    companion object {
        fun create(token: IElementType, startMarker: PsiBuilder.Marker): IncompleteBlock {
            return when (token) {
                SvelteBlockLazyElementTypes.IF_START -> IncompleteIfBlock(startMarker)
                SvelteBlockLazyElementTypes.EACH_START -> IncompleteEachBlock(startMarker)
                SvelteBlockLazyElementTypes.AWAIT_START -> IncompleteAwaitBlock(startMarker)
                else -> throw IllegalArgumentException("Expected start tag token")
            }
        }
    }
}

data class IncompleteIfBlock(override val startMarker: PsiBuilder.Marker) : IncompleteBlock() {
    override fun isMatchingEndTag(token: IElementType) = token === SvelteBlockLazyElementTypes.IF_END
}

data class IncompleteEachBlock(override val startMarker: PsiBuilder.Marker) : IncompleteBlock() {
    override fun isMatchingEndTag(token: IElementType) = token === SvelteBlockLazyElementTypes.EACH_END
}

data class IncompleteAwaitBlock(override val startMarker: PsiBuilder.Marker) : IncompleteBlock() {
    override fun isMatchingEndTag(token: IElementType) = token === SvelteBlockLazyElementTypes.AWAIT_END
}
