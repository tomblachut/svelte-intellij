package dev.blachut.svelte.lang

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class SvelteJsInspectionSuppressor : InspectionSuppressor {
    override fun getSuppressActions(element: PsiElement?, inspectionId: String): Array<SuppressQuickFix> {
        return SuppressQuickFix.EMPTY_ARRAY
    }

    override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
        if (inspectionId == "UnnecessaryLabelJS") {
            return element.parent is JSLabeledStatement && element.text == "$"
        }
        if (inspectionId == "JSUnusedAssignment") {
            return PsiTreeUtil.findFirstParent(element) { parent -> parent is JSLabeledStatement && parent.text.startsWith("$:") } != null
        }
        return false
    }
}
