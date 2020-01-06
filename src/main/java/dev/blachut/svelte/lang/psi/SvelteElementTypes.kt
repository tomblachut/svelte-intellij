package dev.blachut.svelte.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.parsing.html.psi.*

object SvelteElementTypes {
    val IF_BLOCK = SvelteElementType("IF_BLOCK")
    val IF_TRUE_BLOCK = SvelteElementType("IF_TRUE_BLOCK")
    val IF_ELSE_BLOCK = SvelteElementType("IF_ELSE_BLOCK")

    val EACH_BLOCK = SvelteElementType("EACH_BLOCK")
    val EACH_LOOP_BLOCK = SvelteElementType("EACH_LOOP_BLOCK")
    val EACH_ELSE_BLOCK = SvelteElementType("EACH_ELSE_BLOCK")

    val AWAIT_BLOCK = SvelteElementType("AWAIT_BLOCK")
    val AWAIT_MAIN_BLOCK = SvelteElementType("AWAIT_PENDING_BLOCK")
    val AWAIT_THEN_BLOCK = SvelteElementType("AWAIT_THEN_BLOCK")
    val AWAIT_CATCH_BLOCK = SvelteElementType("AWAIT_CATCH_BLOCK")

    val FRAGMENT = SvelteElementType("FRAGMENT")

    val ATTRIBUTE_EXPRESSION = SvelteElementType("ATTRIBUTE_EXPRESSION")

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

            FRAGMENT -> SvelteFragment(node)

            ATTRIBUTE_EXPRESSION -> ASTWrapperPsiElement(node)

            SvelteBlockLazyElementTypes.IF_END,
            SvelteBlockLazyElementTypes.EACH_END,
            SvelteBlockLazyElementTypes.AWAIT_END -> SvelteEndTag(node)

            else -> throw IllegalArgumentException("Unknown element type: ${node.elementType}")
        }
    }
}
