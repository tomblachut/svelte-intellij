package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.util.containers.Stack
import dev.blachut.svelte.lang.psi.SvelteTagElementTypes

class SvelteParsing(
    private val builder: PsiBuilder,
    private val flushHtmlTags: (beforeMarker: PsiBuilder.Marker, targetTagLevel: Int) -> Unit
) {
    val blockLevel get() = if (incompleteBlocks.empty()) 0 else incompleteBlocks.peek().tagLevel

    private val incompleteBlocks = Stack<IncompleteBlock>()

    fun parseSvelteTag(currentTagLevel: Int) {
        val (resultToken, resultMarker) = SvelteTagParsing.parseTag(builder)

        if (SvelteTagElementTypes.START_TAGS.contains(resultToken)) {
            val incompleteBlock = IncompleteBlock.create(currentTagLevel, resultToken, resultMarker, builder.mark())
            incompleteBlocks.push(incompleteBlock)
        } else if (SvelteTagElementTypes.INNER_TAGS.contains(resultToken)) {
            if (!incompleteBlocks.empty() && incompleteBlocks.peek().isMatchingInnerTag(resultToken)) {
                val incompleteBlock = incompleteBlocks.peek()

                flushHtmlTags(resultMarker, incompleteBlock.tagLevel)
                incompleteBlock.handleInnerTag(resultToken, resultMarker, builder.mark())
            } else {
                resultMarker.precede().errorBefore("unexpected inner tag", resultMarker)
            }
        } else if (SvelteTagElementTypes.END_TAGS.contains(resultToken)) {
            if (!incompleteBlocks.empty() && incompleteBlocks.peek().isMatchingEndTag(resultToken)) {
                val incompleteBlock = incompleteBlocks.pop()

                flushHtmlTags(resultMarker, incompleteBlock.tagLevel)
                incompleteBlock.handleEndTag(resultMarker)
            } else {
                resultMarker.precede().errorBefore("unexpected end token", resultMarker)
            }
        }
    }

    fun flushSvelteTags() {
        while (!incompleteBlocks.empty()) {
            val incompleteBlock = incompleteBlocks.pop()
            incompleteBlock.handleMissingEndTag(builder.mark())
        }
    }
}
