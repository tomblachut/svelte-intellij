package dev.blachut.svelte.lang

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.psi.PsiElement
import com.intellij.codeInspection.SuppressQuickFix
import dev.blachut.svelte.lang.psi.SvelteHtmlFile

class SvelteJSInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (element.containingFile is SvelteHtmlFile) {
            if (toolId == "UnnecessaryLabelJS") {
                return element.textMatches("$")
            }
        }

        return false
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> = emptyArray()
}
