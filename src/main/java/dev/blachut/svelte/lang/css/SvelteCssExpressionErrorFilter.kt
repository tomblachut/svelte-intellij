package dev.blachut.svelte.lang.css

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.css.CssElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue

class SvelteCssExpressionErrorFilter : HighlightErrorFilter() {
  override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
    return !isSvelteExpressionSpecialCase(element)
  }

  private fun isSvelteExpressionSpecialCase(element: PsiErrorElement): Boolean {
    if (element.parent !is CssElement) return false
    val ancestor = element.parentOfType<XmlAttributeValue>() ?: return false
    // make sure we have an expression embedded TODO Replace ASTWrapperPsiElement with dedicated expression type
    return PsiTreeUtil.getChildOfType(ancestor, ASTWrapperPsiElement::class.java) != null
  }
}
