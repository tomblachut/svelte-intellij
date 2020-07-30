package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import dev.blachut.svelte.lang.psi.blocks.*

object SvelteElementTypes {
    val IF_BLOCK = SvelteElementType("IF_BLOCK")
    val IF_TRUE_BRANCH = SvelteElementType("IF_TRUE_BRANCH")
    val IF_ELSE_BRANCH = SvelteElementType("IF_ELSE_BRANCH")

    val EACH_BLOCK = SvelteElementType("EACH_BLOCK")
    val EACH_LOOP_BRANCH = SvelteElementType("EACH_LOOP_BRANCH")
    val EACH_ELSE_BRANCH = SvelteElementType("EACH_ELSE_BRANCH")

    val AWAIT_BLOCK = SvelteElementType("AWAIT_BLOCK")
    val AWAIT_MAIN_BRANCH = SvelteElementType("AWAIT_PENDING_BRANCH")
    val AWAIT_THEN_BRANCH = SvelteElementType("AWAIT_THEN_BRANCH")
    val AWAIT_CATCH_BRANCH = SvelteElementType("AWAIT_CATCH_BRANCH")

    val FRAGMENT = SvelteElementType("FRAGMENT")

    // TODO remove this element
    val ATTRIBUTE_EXPRESSION = SvelteElementType("ATTRIBUTE_EXPRESSION")

    val BRANCHES = TokenSet.create(IF_TRUE_BRANCH, IF_ELSE_BRANCH, EACH_LOOP_BRANCH, EACH_ELSE_BRANCH, AWAIT_MAIN_BRANCH, AWAIT_THEN_BRANCH, AWAIT_CATCH_BRANCH)

    fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            IF_BLOCK -> SvelteIfBlock(node)
            IF_TRUE_BRANCH -> SvelteIfPrimaryBranch(node)
            IF_ELSE_BRANCH -> SvelteIfElseBranch(node)

            EACH_BLOCK -> SvelteEachBlock(node)
            EACH_LOOP_BRANCH -> SvelteEachPrimaryBranch(node)
            EACH_ELSE_BRANCH -> SvelteEachElseBranch(node)

            AWAIT_BLOCK -> SvelteAwaitBlock(node)
            AWAIT_MAIN_BRANCH -> SvelteAwaitPrimaryBranch(node)
            AWAIT_THEN_BRANCH -> SvelteAwaitThenBranch(node)
            AWAIT_CATCH_BRANCH -> SvelteAwaitCatchBranch(node)

            FRAGMENT -> SvelteFragment(node)

            ATTRIBUTE_EXPRESSION -> SveltePsiElement(node)

            SvelteTagElementTypes.IF_END,
            SvelteTagElementTypes.EACH_END,
            SvelteTagElementTypes.AWAIT_END -> SvelteEndTag(node)

            else -> throw IllegalArgumentException("Unknown element type: ${node.elementType}")
        }
    }
}
