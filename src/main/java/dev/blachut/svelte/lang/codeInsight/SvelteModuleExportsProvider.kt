package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.ecmascript6.resolve.JSModuleElementsProcessor
import com.intellij.lang.ecmascript6.resolve.JSModuleExportsProvider
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent

private class SvelteModuleExportsProvider : JSModuleExportsProvider {
  override fun processExports(scope: PsiElement, processor: JSModuleElementsProcessor, weak: Boolean): Boolean {
    return true
  }

  override fun getAdditionalScopes(scope: PsiElement, visited: MutableCollection<PsiElement>): Collection<PsiElement> {
    if (scope !is SvelteHtmlFile) return emptyList()

    return listOfNotNull(getJsEmbeddedContent(scope.moduleScript))
  }
}