package dev.blachut.svelte.lang.parsing.html.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor


class SvelteAwaitPrimaryBranch(node: ASTNode) : SveltePrimaryBranch(node) {
    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        return processParameterDeclarations(processor, state, lastParent, place)
    }
}
