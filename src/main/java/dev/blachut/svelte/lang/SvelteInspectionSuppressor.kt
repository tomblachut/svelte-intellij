package dev.blachut.svelte.lang

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.psi.PsiElement
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.lang.javascript.psi.JSLabeledStatement
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

class SvelteInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (element.containingFile is SvelteHtmlFile) {
            if (toolId == "UnnecessaryLabelJS") {
                return element.textMatches("$")
            }
            if (toolId == "BadExpressionStatementJS") {
                return true
            }
        }

        return false
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> = emptyArray()
}
