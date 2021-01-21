package dev.blachut.svelte.lang.css

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.css.CssBundle
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttributeValue

class SvelteCssExpressionErrorFilter : HighlightErrorFilter() {
    override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
        return !isSvelteExpressionSpecialCase(element)
    }

    private fun isSvelteExpressionSpecialCase(element: PsiErrorElement): Boolean {
        if (element.errorDescription != CssBundle.message("a.term.expected")) return false
        val parent = PsiTreeUtil.getParentOfType(element, XmlAttributeValue::class.java) ?: return false
        // TODO Replace ASTWrapperPsiElement with dedicated expression type
        return PsiTreeUtil.getChildOfType(parent, ASTWrapperPsiElement::class.java) != null
    }
}
