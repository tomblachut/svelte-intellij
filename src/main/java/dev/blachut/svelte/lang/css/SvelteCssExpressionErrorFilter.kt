package dev.blachut.svelte.lang.css

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.css.CssElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import dev.blachut.svelte.lang.compatibility.hasChildMarkupExpression

class SvelteCssExpressionErrorFilter : HighlightErrorFilter() {
  override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
    return !isSvelteExpressionSpecialCase(element)
  }

  private fun isSvelteExpressionSpecialCase(element: PsiErrorElement): Boolean {
    if (element.parent !is CssElement) return false
    val ancestor = element.parentOfType<XmlAttributeValue>() ?: return false
    return hasChildMarkupExpression(ancestor)
  }
}