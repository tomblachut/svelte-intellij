package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.Stack
import dev.blachut.svelte.lang.psi.SvelteBlockLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteTokenTypes

class SvelteParsing(val builder: PsiBuilder) {
    private val incompleteBlocks = Stack<IncompleteBlock>()

    fun isSvelteTagStart(token: IElementType): Boolean {
        return token === SvelteTokenTypes.START_MUSTACHE_TEMP
    }

    fun parseSvelteTag() {
        val (resultToken, resultMarker) = SvelteManualParsing.parseLazyBlock(builder)

        if (startTokens.contains(resultToken)) {
            val incompleteBlock = IncompleteBlock.create(resultToken, resultMarker, builder.mark())
            incompleteBlocks.push(incompleteBlock)
        } else if (innerTokens.contains(resultToken)) {
            if (!incompleteBlocks.empty() && incompleteBlocks.peek().isMatchingInnerTag(resultToken)) {
                val incompleteBlock = incompleteBlocks.peek()
                incompleteBlock.handleInnerTag(resultToken, resultMarker, builder.mark())
            } else {
                resultMarker.precede().errorBefore("unexpected inner tag", resultMarker)
            }
        } else if (endTokens.contains(resultToken)) {
            if (!incompleteBlocks.empty() && incompleteBlocks.peek().isMatchingEndTag(resultToken)) {
                val incompleteBlock = incompleteBlocks.pop()
                incompleteBlock.handleEndTag(resultMarker)
            } else {
                resultMarker.precede().errorBefore("unexpected end token", resultMarker)
            }
        }
    }

    fun reportMissingEndSvelteTags() {
        while (!incompleteBlocks.empty()) {
            val incompleteBlock = incompleteBlocks.pop()
            incompleteBlock.handleMissingEndTag(builder.mark())
        }
    }
}

val startTokens = TokenSet.create(SvelteBlockLazyElementTypes.IF_START, SvelteBlockLazyElementTypes.EACH_START, SvelteBlockLazyElementTypes.AWAIT_START)
val innerTokens = TokenSet.create(SvelteBlockLazyElementTypes.ELSE_CLAUSE, SvelteBlockLazyElementTypes.THEN_CLAUSE, SvelteBlockLazyElementTypes.CATCH_CLAUSE)
val endTokens = TokenSet.create(SvelteBlockLazyElementTypes.IF_END, SvelteBlockLazyElementTypes.EACH_END, SvelteBlockLazyElementTypes.AWAIT_END)
