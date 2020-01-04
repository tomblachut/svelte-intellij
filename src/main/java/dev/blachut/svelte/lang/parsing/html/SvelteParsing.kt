package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.Stack
import dev.blachut.svelte.lang.psi.SvelteBlockLazyElementTypes
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes
import dev.blachut.svelte.lang.psi.SvelteTypes

class SvelteParsing(val builder: PsiBuilder) {
    private val svelteTagTokens = Stack<IElementType>()
    private val svelteTagMarkers = Stack<PsiBuilder.Marker>()

    fun isSvelteTagStart(token: IElementType): Boolean {
        return token === SvelteTypes.START_MUSTACHE_TEMP
    }

    fun parseSvelteTag() {
        val tempMarker = builder.mark()
        val resultToken = SvelteManualParsing.parseLazyBlock(builder)

        if (startTokens.contains(resultToken)) {
            svelteTagMarkers.push(tempMarker)
            svelteTagTokens.push(resultToken)
        } else if (endTokens.contains(resultToken)) {
            if (!svelteTagTokens.empty() && isMatchingEndTag(svelteTagTokens.peek(), resultToken)) {
                tempMarker.drop()
                svelteTagTokens.pop()
                val startMarker = svelteTagMarkers.pop()
                startMarker.done(SvelteJSElementTypes.ATTRIBUTE_EXPRESSION)
            } else {
                tempMarker.precede().errorBefore("unexpected end token", tempMarker)
                tempMarker.drop()
            }
        } else {
            tempMarker.drop()
        }
    }

    fun reportMissingEndSvelteTags() {
        while (!svelteTagTokens.empty()) {
            svelteTagTokens.pop()
            val marker = svelteTagMarkers.pop()
//            marker.done(ATTRIBUTE_EXPRESSION)
            marker.drop()

            builder.error("not closed tag")
        }
    }

    private fun isMatchingEndTag(startTag: IElementType, endTag: IElementType): Boolean {
        return (startTag === SvelteBlockLazyElementTypes.IF_START && endTag === SvelteBlockLazyElementTypes.IF_END)
            || (startTag === SvelteBlockLazyElementTypes.EACH_START && endTag === SvelteBlockLazyElementTypes.EACH_END)
            || (startTag === SvelteBlockLazyElementTypes.AWAIT_START && endTag === SvelteBlockLazyElementTypes.AWAIT_END)
    }
}

val startTokens = TokenSet.create(SvelteBlockLazyElementTypes.IF_START, SvelteBlockLazyElementTypes.EACH_START, SvelteBlockLazyElementTypes.AWAIT_START)
val endTokens = TokenSet.create(SvelteBlockLazyElementTypes.IF_END, SvelteBlockLazyElementTypes.EACH_END, SvelteBlockLazyElementTypes.AWAIT_END)
