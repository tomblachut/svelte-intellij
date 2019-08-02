package dev.blachut.svelte.lang

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.psi.PsiElement

class SvelteJsInspectionSuppressor : InspectionSuppressor {
    override fun getSuppressActions(element: PsiElement?, inspectionId: String): Array<SuppressQuickFix> {
        return SuppressQuickFix.EMPTY_ARRAY
    }

    override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
        if (element.parent is JSLabeledStatement) {
            return inspectionId == "UnnecessaryLabelJS" && element.text == "$"
        }
        return false
    }

}
