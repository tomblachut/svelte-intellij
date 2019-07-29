package dev.blachut.svelte.lang

import com.intellij.codeInspection.XmlSuppressionProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class SvelteSuppressionProvider : XmlSuppressionProvider() {
    override fun suppressForTag(element: PsiElement, inspectionId: String) = Unit

    override fun suppressForFile(element: PsiElement, inspectionId: String) = Unit

    override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
        return inspectionId == "XmlUnboundNsPrefix" || inspectionId == "BadExpressionStatementJS"
    }

    override fun isProviderAvailable(file: PsiFile): Boolean {
        return file.viewProvider is SvelteFileViewProvider
    }
}
