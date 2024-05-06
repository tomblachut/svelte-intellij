package dev.blachut.svelte.lang.psi.blocks

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

class SvelteSnippetBlock(node: ASTNode) : SvelteBlock(node, "snippet") {
  override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
    return primaryBranch.processDeclarations(processor, state, lastParent, place)
  }
}
