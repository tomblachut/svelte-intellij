package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptCompileTimeType
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.JSCompletionPlaceFilter
import com.intellij.lang.javascript.psi.resolve.JSCompletionPlaceFilterProvider
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.psi.SvelteHtmlTag

class SvelteHtmlTagCompletionPlaceFilterProvider : JSCompletionPlaceFilterProvider {
  override fun forPlace(place: PsiElement): JSCompletionPlaceFilter? {
    return if (place is SvelteHtmlTag) SvelteHtmlTagCompletionPlaceFilter else null
  }
}

private object SvelteHtmlTagCompletionPlaceFilter : JSCompletionPlaceFilter() {
  override fun isAcceptable(expanded: JSPsiElementBase): Boolean {
    // SvelteHtmlFile case is assumed to be permitted by the other overload

    if (expanded is TypeScriptCompileTimeType) return false // ensures we don't generate broken import type statements & excludes interface
    if (expanded is JSClass) {

      return expanded.name?.startsWith("SvelteComponent") != true
             && checkClassHierarchyHasSvelteComponent(expanded)
    }
    return false
  }

  private fun checkClassHierarchyHasSvelteComponent(jsClass: JSClass): Boolean {
    return !JSClassUtils.processClassesInHierarchy(jsClass, false) { parentCandidate, _, _ ->
      val parentCandidateName = parentCandidate.name
      parentCandidateName == null || !parentCandidateName.startsWith("SvelteComponent")
    }
  }
}
