package dev.blachut.svelte.lang

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteJSReferenceExpression

class SvelteInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (element.containingFile is SvelteHtmlFile) {
            if (toolId == "UnnecessaryLabelJS") {
                return element.textMatches(SvelteReactiveDeclarationsUtil.REACTIVE_LABEL)
            }
            if (toolId == "BadExpressionStatementJS") {
                return true
            }
            if (toolId == "JSConstantReassignment") {
                val parent = element.parent
                if (parent is SvelteJSReferenceExpression && parent.isSubscribedReference) {
                    // TODO check if store is writable
                    return true
                }
            }
        }

        return false
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> = emptyArray()
}
