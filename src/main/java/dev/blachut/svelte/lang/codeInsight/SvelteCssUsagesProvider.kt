package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider
import com.intellij.psi.util.parentOfType
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute
import dev.blachut.svelte.lang.psi.SvelteJSLazyPsiElement

internal class SvelteCssUsagesProvider : CssClassOrIdReferenceBasedUsagesProvider() {
  override fun acceptElement(candidate: PsiElement): Boolean {
    if (candidate is SvelteHtmlAttribute) return true
    if (candidate is JSLiteralExpression || candidate is JSProperty) {
      return candidate.parentOfType<SvelteJSLazyPsiElement>() != null
    }
    return false
  }
}
