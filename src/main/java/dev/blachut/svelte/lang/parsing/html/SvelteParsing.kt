package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.Stack
import dev.blachut.svelte.lang.psi.SvelteBlockLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteTypes

class SvelteParsing(val builder: PsiBuilder) {
    private val incompleteBlocks = Stack<IncompleteBlock>()

    fun isSvelteTagStart(token: IElementType): Boolean {
        return token === SvelteTypes.START_MUSTACHE_TEMP
    }

    fun parseSvelteTag() {
        val (resultToken, resultMarker) = SvelteManualParsing.parseLazyBlock(builder)

        if (startTokens.contains(resultToken)) {
            val startMarker = resultMarker.precede()
            val incompleteBlock = IncompleteBlock.create(resultToken, startMarker)
            incompleteBlocks.push(incompleteBlock)
        } else if (endTokens.contains(resultToken)) {
            if (!incompleteBlocks.empty() && incompleteBlocks.peek().isMatchingEndTag(resultToken)) {
                val incompleteBlock = incompleteBlocks.pop()
                incompleteBlock.handleEndTag()
            } else {
                resultMarker.precede().errorBefore("unexpected end token", resultMarker)
            }
        }
    }

    fun reportMissingEndSvelteTags() {
        while (!incompleteBlocks.empty()) {
            val incompleteBlock = incompleteBlocks.pop()
//            incompleteBlock.startMarker.done(ATTRIBUTE_EXPRESSION)
            incompleteBlock.startMarker.drop()

            builder.error("not closed tag")
        }
    }
}

val startTokens = TokenSet.create(SvelteBlockLazyElementTypes.IF_START, SvelteBlockLazyElementTypes.EACH_START, SvelteBlockLazyElementTypes.AWAIT_START)
val endTokens = TokenSet.create(SvelteBlockLazyElementTypes.IF_END, SvelteBlockLazyElementTypes.EACH_END, SvelteBlockLazyElementTypes.AWAIT_END)
