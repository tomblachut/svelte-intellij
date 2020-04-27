package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder.Marker
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.psi.SvelteElementTypes
import dev.blachut.svelte.lang.psi.SvelteTagElementTypes

sealed class IncompleteBlock {
    abstract val tagLevel: Int

    abstract fun isMatchingInnerTag(token: IElementType): Boolean
    abstract fun isMatchingEndTag(token: IElementType): Boolean

    abstract fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker)
    abstract fun handleEndTag(resultMarker: Marker)
    abstract fun handleMissingEndTag(errorMarker: Marker)

    companion object {
        fun create(tagLevel: Int, token: IElementType, resultMarker: Marker, fragmentMarker: Marker): IncompleteBlock {
            val innerMarker = resultMarker.precede()
            val outerMarker = innerMarker.precede()
            return when (token) {
                SvelteTagElementTypes.IF_START -> IncompleteIfBlock(tagLevel, outerMarker, innerMarker, fragmentMarker)
                SvelteTagElementTypes.EACH_START -> IncompleteEachBlock(tagLevel, outerMarker, innerMarker, fragmentMarker)
                SvelteTagElementTypes.AWAIT_START -> IncompleteAwaitBlock(tagLevel, outerMarker, innerMarker, fragmentMarker)
                else -> throw IllegalArgumentException("Expected start tag token")
            }
        }
    }
}

data class IncompleteIfBlock(
    override val tagLevel: Int,
    private val outerMarker: Marker,
    private var innerMarker: Marker,
    private var fragmentMarker: Marker
) : IncompleteBlock() {
    private var lastInnerElement = SvelteElementTypes.IF_TRUE_BRANCH

    override fun isMatchingInnerTag(token: IElementType) = token === SvelteTagElementTypes.ELSE_CLAUSE
    override fun isMatchingEndTag(token: IElementType) = token === SvelteTagElementTypes.IF_END

    override fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
        innerMarker.doneBefore(lastInnerElement, resultMarker)
        lastInnerElement = SvelteElementTypes.IF_ELSE_BRANCH
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
    override val tagLevel: Int,
    private val outerMarker: Marker,
    private var innerMarker: Marker,
    private var fragmentMarker: Marker
) : IncompleteBlock() {
    private var lastInnerElement = SvelteElementTypes.EACH_LOOP_BRANCH

    override fun isMatchingInnerTag(token: IElementType) = token === SvelteTagElementTypes.ELSE_CLAUSE
    override fun isMatchingEndTag(token: IElementType) = token === SvelteTagElementTypes.EACH_END

    override fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
        innerMarker.doneBefore(lastInnerElement, resultMarker)
        lastInnerElement = SvelteElementTypes.EACH_ELSE_BRANCH
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
    override val tagLevel: Int,
    private val outerMarker: Marker,
    private var innerMarker: Marker,
    private var fragmentMarker: Marker
) : IncompleteBlock() {
    private var lastInnerElement = SvelteElementTypes.AWAIT_MAIN_BRANCH

    override fun isMatchingInnerTag(token: IElementType) =
        token === SvelteTagElementTypes.THEN_CLAUSE || token === SvelteTagElementTypes.CATCH_CLAUSE

    override fun isMatchingEndTag(token: IElementType) = token === SvelteTagElementTypes.AWAIT_END

    override fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker) {
        fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
        innerMarker.doneBefore(lastInnerElement, resultMarker)
        lastInnerElement = when (token) {
            SvelteTagElementTypes.THEN_CLAUSE -> SvelteElementTypes.AWAIT_THEN_BRANCH
            SvelteTagElementTypes.CATCH_CLAUSE -> SvelteElementTypes.AWAIT_CATCH_BRANCH
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
