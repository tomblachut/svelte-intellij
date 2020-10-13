package dev.blachut.svelte.lang

import com.intellij.lang.javascript.inspections.JSDefaultInspectionSuppressor
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil

object SvelteJSInspectionSuppressor : JSDefaultInspectionSuppressor() {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (toolId == "UnnecessaryLabelJS") {
            return element.textMatches(SvelteReactiveDeclarationsUtil.REACTIVE_LABEL)
        }

        return super.isSuppressedFor(element, toolId)
    }
}
