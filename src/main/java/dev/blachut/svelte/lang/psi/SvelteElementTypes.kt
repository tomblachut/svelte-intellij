package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.parsing.html.psi.SvelteAwaitBlock
import dev.blachut.svelte.lang.parsing.html.psi.SvelteEachBlock
import dev.blachut.svelte.lang.parsing.html.psi.SvelteIfBlock

object SvelteElementTypes {
    val IF_BLOCK: IElementType = SvelteElementType("IF_BLOCK")
    val EACH_BLOCK: IElementType = SvelteElementType("EACH_BLOCK")
    val AWAIT_BLOCK: IElementType = SvelteElementType("AWAIT_BLOCK")

    fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            IF_BLOCK -> SvelteIfBlock(node)
            EACH_BLOCK -> SvelteEachBlock(node)
            AWAIT_BLOCK -> SvelteAwaitBlock(node)
            else -> throw IllegalArgumentException("Unknown element type: ${node.elementType}")
        }
    }
}
