package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder.Marker
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.psi.SvelteBlockLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteElementTypes

sealed class IncompleteBlock {
    abstract val outerMarker: Marker

    abstract fun isMatchingInnerTag(token: IElementType): Boolean
    abstract fun isMatchingEndTag(token: IElementType): Boolean

    abstract fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker)
    abstract fun handleEndTag(resultMarker: Marker)
    abstract fun handleMissingEndTag(errorMarker: Marker)

    companion object {
        fun create(token: IElementType, resultMarker: Marker, fragmentMarker: Marker): IncompleteBlock {
            val innerMarker = resultMarker.precede()
            val outerMarker = innerMarker.precede()
            return when (token) {
                SvelteBlockLazyElementTypes.IF_START -> IncompleteIfBlock(outerMarker, innerMarker, fragmentMarker)
                SvelteBlockLazyElementTypes.EACH_START -> IncompleteEachBlock(outerMarker, innerMarker, fragmentMarker)
                SvelteBlockLazyElementTypes.AWAIT_START -> IncompleteAwaitBlock(outerMarker, innerMarker, fragmentMarker)
                else -> throw IllegalArgumentException("Expected start tag token")
            }
        }
    }
}

data class IncompleteIfBlock(
    override val outerMarker: Marker,
    var innerMarker: Marker,
    var fragmentMarker: Marker
) : IncompleteBlock() {
    private var lastInnerElement = SvelteElementTypes.IF_TRUE_BLOCK

    override fun isMatchingInnerTag(token: IElementType) = token === SvelteBlockLazyElementTypes.ELSE_CLAUSE
    override fun isMatchingEndTag(token: IElementType) = token === SvelteBlockLazyElementTypes.IF_END

    override fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
        innerMarker.doneBefore(lastInnerElement, resultMarker)
        lastInnerElement = SvelteElementTypes.IF_ELSE_BLOCK
        innerMarker = resultMarker.precede()
        fragmentMarker = nextFragmentMarker
    }

    override fun handleEndTag(resultMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
        innerMarker.doneBefore(lastInnerElement, resultMarker)
        outerMarker.done(SvelteElementTypes.IF_BLOCK)
    }

    override fun handleMissingEndTag(errorMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, errorMarker)
        innerMarker.doneBefore(lastInnerElement, errorMarker)
        errorMarker.error("missing {/if} tag")
        outerMarker.done(SvelteElementTypes.IF_BLOCK)
    }
}

data class IncompleteEachBlock(
    override val outerMarker: Marker,
    var innerMarker: Marker,
    var fragmentMarker: Marker
) : IncompleteBlock() {
    private var lastInnerElement = SvelteElementTypes.EACH_LOOP_BLOCK

    override fun isMatchingInnerTag(token: IElementType) = token === SvelteBlockLazyElementTypes.ELSE_CLAUSE
    override fun isMatchingEndTag(token: IElementType) = token === SvelteBlockLazyElementTypes.EACH_END

    override fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
        innerMarker.doneBefore(lastInnerElement, resultMarker)
        lastInnerElement = SvelteElementTypes.EACH_ELSE_BLOCK
        innerMarker = resultMarker.precede()
        fragmentMarker = nextFragmentMarker
    }

    override fun handleEndTag(resultMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
        innerMarker.doneBefore(lastInnerElement, resultMarker)
        outerMarker.done(SvelteElementTypes.EACH_BLOCK)
    }

    override fun handleMissingEndTag(errorMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, errorMarker)
        innerMarker.doneBefore(lastInnerElement, errorMarker)
        errorMarker.error("missing {/each} tag")
        outerMarker.done(SvelteElementTypes.IF_BLOCK)
    }
}

data class IncompleteAwaitBlock(
    override val outerMarker: Marker,
    var innerMarker: Marker,
    var fragmentMarker: Marker
) : IncompleteBlock() {
    private var lastInnerElement = SvelteElementTypes.AWAIT_MAIN_BLOCK

    override fun isMatchingInnerTag(token: IElementType) =
        token === SvelteBlockLazyElementTypes.THEN_CLAUSE || token === SvelteBlockLazyElementTypes.CATCH_CLAUSE

    override fun isMatchingEndTag(token: IElementType) = token === SvelteBlockLazyElementTypes.AWAIT_END

    override fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
        innerMarker.doneBefore(lastInnerElement, resultMarker)
        lastInnerElement = when (token) {
            SvelteBlockLazyElementTypes.THEN_CLAUSE -> SvelteElementTypes.AWAIT_THEN_BLOCK
            SvelteBlockLazyElementTypes.CATCH_CLAUSE -> SvelteElementTypes.AWAIT_CATCH_BLOCK
            else -> throw IllegalArgumentException("Expected await block inner clause")
        }
        innerMarker = resultMarker.precede()
        fragmentMarker = nextFragmentMarker
    }

    override fun handleEndTag(resultMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
        innerMarker.doneBefore(lastInnerElement, resultMarker)
        outerMarker.done(SvelteElementTypes.AWAIT_BLOCK)
    }

    override fun handleMissingEndTag(errorMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, errorMarker)
        innerMarker.doneBefore(lastInnerElement, errorMarker)
        errorMarker.error("missing {/await} tag")
        outerMarker.done(SvelteElementTypes.AWAIT_BLOCK)
    }
}
