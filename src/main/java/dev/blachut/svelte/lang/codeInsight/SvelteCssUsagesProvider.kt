package dev.blachut.svelte.lang.codeInsight

import com.intellij.psi.PsiElement
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

private class SvelteCssUsagesProvider : CssClassOrIdReferenceBasedUsagesProvider() {
  override fun acceptElement(candidate: PsiElement): Boolean {
    return candidate is SvelteHtmlAttribute
  }
}
