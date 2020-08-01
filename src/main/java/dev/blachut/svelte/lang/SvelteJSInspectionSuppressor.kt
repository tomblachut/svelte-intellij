package dev.blachut.svelte.lang

import com.intellij.lang.javascript.inspections.JSDefaultInspectionSuppressor
import com.intellij.psi.PsiElement

object SvelteJSInspectionSuppressor : JSDefaultInspectionSuppressor() {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (toolId == "UnnecessaryLabelJS") {
            return element.textMatches("$")
        }

        return super.isSuppressedFor(element, toolId)
    }
}
