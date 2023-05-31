package dev.blachut.svelte.lang.psi

import com.intellij.psi.PsiElement

interface SvelteVisitor {
  fun visitInitialTag(tag: SvelteInitialTag) {
    visitElement(tag)
  }

  fun visitLazyElement(element: SvelteJSLazyPsiElement) {
    visitElement(element)
  }

  fun visitElement(element: PsiElement)
}
