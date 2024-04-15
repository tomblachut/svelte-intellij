package dev.blachut.svelte.lang.psi.blocks

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil

class SvelteSnippetPrimaryBranch(node: ASTNode) : SveltePrimaryBranch(node) {
  override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
    val parameterList = PsiTreeUtil.findChildOfType(tag, JSFunction::class.java)?.parameterList ?: return true
    return visitParameters(parameterList, processor)
  }
}
