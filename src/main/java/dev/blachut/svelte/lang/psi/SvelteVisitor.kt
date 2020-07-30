package dev.blachut.svelte.lang.psi

import com.intellij.psi.PsiElementVisitor

interface SvelteVisitor {
    fun visitInitialTag(tag: SvelteInitialTag) {
        (this as PsiElementVisitor).visitElement(tag)
    }

    fun visitLazyElement(element: SvelteJSLazyPsiElement) {
        (this as PsiElementVisitor).visitElement(element)
    }
}
