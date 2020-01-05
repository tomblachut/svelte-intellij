package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.parsing.html.psi.*

object SvelteElementTypes {
    val IF_BLOCK: IElementType = SvelteElementType("IF_BLOCK")
    val IF_TRUE_BLOCK: IElementType = SvelteElementType("IF_TRUE_BLOCK")
    val IF_ELSE_BLOCK: IElementType = SvelteElementType("IF_ELSE_BLOCK")

    val EACH_BLOCK: IElementType = SvelteElementType("EACH_BLOCK")
    val EACH_LOOP_BLOCK: IElementType = SvelteElementType("EACH_LOOP_BLOCK")
    val EACH_ELSE_BLOCK: IElementType = SvelteElementType("EACH_ELSE_BLOCK")

    val AWAIT_BLOCK: IElementType = SvelteElementType("AWAIT_BLOCK")
    val AWAIT_MAIN_BLOCK: IElementType = SvelteElementType("AWAIT_PENDING_BLOCK")
    val AWAIT_THEN_BLOCK: IElementType = SvelteElementType("AWAIT_THEN_BLOCK")
    val AWAIT_CATCH_BLOCK: IElementType = SvelteElementType("AWAIT_CATCH_BLOCK")

//    val INTERPOLATION: IElementType = SvelteElementType("INTERPOLATION")
//    val EXPRESSION: IElementType = SvelteElementType("EXPRESSION")
//    val KEY_EXPRESSION: IElementType = SvelteElementType("KEY_EXPRESSION")
//    val PARAMETER: IElementType = SvelteElementType("PARAMETER")

    fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            IF_BLOCK -> SvelteIfBlock(node)
            IF_TRUE_BLOCK -> SvelteIfTrueBlock(node)
            IF_ELSE_BLOCK -> SvelteIfElseBlock(node)

            EACH_BLOCK -> SvelteEachBlock(node)
            EACH_LOOP_BLOCK -> SvelteEachLoopBlock(node)
            EACH_ELSE_BLOCK -> SvelteEachElseBlock(node)

            AWAIT_BLOCK -> SvelteAwaitBlock(node)
            AWAIT_MAIN_BLOCK -> SvelteAwaitMainBlock(node)
            AWAIT_THEN_BLOCK -> SvelteAwaitThenBlock(node)
            AWAIT_CATCH_BLOCK -> SvelteAwaitCatchBlock(node)

            else -> throw IllegalArgumentException("Unknown element type: ${node.elementType}")
        }
    }
}
